USING Progress.Json.ObjectModel.JsonArray.
USING Progress.Json.ObjectModel.JsonObject.
USING Progress.Json.ObjectModel.ObjectModelParser.

ROUTINE-LEVEL ON ERROR UNDO, THROW.
//BLOCK-LEVEL ON ERROR UNDO, THROW.
/* ^^^ these statements only affect the blocks in THIS procedure 
 * The ROUTINE-LEVEL and BLOCK-LEVEL statements only affect the function of 
 * blocks within this procedure.  Downstream do not inherit the behavior.  
 * see: -undothrow 1 or -undothrow 2 to affect session-level error-handling 
 */
 
DEFINE NEW SHARED VARIABLE pctVerbose AS LOGICAL NO-UNDO.
DEFINE VARIABLE noErrorOnQuit AS LOGICAL NO-UNDO.

DEFINE VARIABLE i AS INTEGER NO-UNDO INITIAL ?.

DEFINE TEMP-TABLE ttParams NO-UNDO
  FIELD key AS CHARACTER
  FIELD val AS CHARACTER.

FUNCTION getParameter RETURNS CHARACTER (k AS CHARACTER).
  FIND ttParams WHERE ttParams.key EQ k NO-LOCK NO-ERROR.
  RETURN (IF AVAILABLE ttParams THEN ttParams.val ELSE ?).
END FUNCTION.

DEFINE VARIABLE jsonParser AS CLASS ObjectModelParser NO-UNDO.
DEFINE VARIABLE configJson AS CLASS JsonObject NO-UNDO.

DEFINE VARIABLE ppEntries AS CLASS JsonArray NO-UNDO.
DEFINE VARIABLE dbEntries AS CLASS JsonArray NO-UNDO.
DEFINE VARIABLE dbEntry   AS CLASS JsonObject NO-UNDO.
DEFINE VARIABLE prmEntries AS CLASS JsonArray NO-UNDO.
DEFINE VARIABLE prmEntry   AS CLASS JsonObject NO-UNDO.
DEFINE VARIABLE outprmEntries AS CLASS JsonArray NO-UNDO.
DEFINE VARIABLE zz AS INTEGER     NO-UNDO.
DEFINE VARIABLE xx AS INTEGER     NO-UNDO.
DEFINE VARIABLE out1 AS CHARACTER   NO-UNDO.
DEFINE VARIABLE out2 AS CHARACTER   NO-UNDO.

ASSIGN jsonParser = NEW ObjectModelParser().
ASSIGN configJson = CAST(jsonParser:ParseFile(SESSION:PARAMETER), JsonObject).

ASSIGN pctVerbose = configJson:getLogical("verbose").

DEF VAR iNowDBS AS INT NO-UNDO.
DEF VAR lPFOnly AS LOGICAL NO-UNDO.

// DB connections + aliases
ASSIGN dbEntries = configJson:GetJsonArray("databases").
DO zz = 1 TO dbEntries:Length:
  ASSIGN dbEntry = dbEntries:GetJsonObject(zz).
         
  ASSIGN iNowDBS = NUM-DBS /* just count all dbs */ 
         lPFOnly = (INDEX(dbEntry:getCharacter("connect"),'-pf') GT 0 AND
                    INDEX(dbEntry:getCharacter("connect"),'-db') LE 0 
                   ). 
  
  IF pctVerbose THEN
    MESSAGE "Connecting to '" + dbEntry:getCharacter("connect") + "'".
  
  CONNECT VALUE(dbEntry:getCharacter("connect")) NO-ERROR.
  
  IF ERROR-STATUS:ERROR THEN DO:
    /* WARN that there were connection "messages" raised" */ 
    MESSAGE "EXCEPTIONS Raised connecting to '" + dbEntry:getCharacter("connect") + "'".
    DO i = 1 TO ERROR-STATUS:NUM-MESSAGES:
      MESSAGE ERROR-STATUS:GET-MESSAGE(i).
    END.
    
    /* Only fail if we did not increment the number of connected DBs 
     * OR only a PF is used -- can't tell if more than one db specified
     */
    IF (NUM-DBS LE iNowDBS) OR lPFOnly
    THEN
    DO:    
        RUN returnValue(14).
        QUIT.
    END.
    
    IF pctVerbose THEN
        MESSAGE 'Things seem okay. Continuing...'.        
  END.
  
  /* If Only a PF is used, it's tough to tell how many DBs and which ones should get ALIAS */
  /* disable for now: user should be warned -- if PF file is used, all aliases apply to first DB */
  /**
  IF lPFOnly and dbEntry:getJsonArray("aliases"):Length GT 0 then
     MESSAGE "PF File Used -- Ignoring aliases".
  ELSE
  **/     
  DO xx = 1 TO dbEntry:getJsonArray("aliases"):Length:
    IF pctVerbose THEN
      MESSAGE SUBSTITUTE("Creating alias &1 for database #&2 &3", dbEntry:getJsonArray("aliases"):getCharacter(xx),
                         zz, LDBNAME(zz)).
    CREATE ALIAS VALUE(dbEntry:getJsonArray("aliases"):getCharacter(xx)) FOR DATABASE VALUE(LDBNAME(zz)) NO-ERROR.
    /* TODO: could assume that each alias refers to an individual DB in the PF... increment for each alias ldbname(zz++) */
    IF ERROR-STATUS:ERROR THEN DO:
      MESSAGE SUBSTITUTE("Unable to create alias '&1' for database '&2'",
                         dbEntry:getJsonArray("aliases"):getCharacter(xx), LDBNAME(zz)).
      DO i = 1 TO ERROR-STATUS:NUM-MESSAGES:
        MESSAGE ERROR-STATUS:GET-MESSAGE(i).
      END.
      RUN returnValue(15).
      QUIT.
    END.
  END.
END.

// PROPATH entries
ASSIGN ppEntries = configJson:GetJsonArray("propath").
DO zz = 1 TO ppEntries:Length:
    ASSIGN PROPATH = ppEntries:getCharacter(zz) + "," + PROPATH.
END.
IF pctVerbose THEN
  MESSAGE "PROPATH : " + PROPATH.

// Input parameters
ASSIGN prmEntries = configJson:GetJsonArray("parameters").
DO zz = 1 TO prmEntries:Length:
  ASSIGN prmEntry = prmEntries:GetJsonObject(zz).
  DO ON ERROR UNDO, LEAVE:
    CREATE ttParams.
    ASSIGN ttParams.key = prmEntry:getCharacter("name")
           ttParams.val = prmEntry:getCharacter("value").
  END.
END.

// Output parameters
ASSIGN outprmEntries = configJson:GetJsonArray("output").

IF configJson:getLogical("super") THEN DO:
  SESSION:ADD-SUPER-PROCEDURE(THIS-PROCEDURE).
END.

// Execute procedure
IF pctVerbose THEN
  MESSAGE "RUN " + configJson:getCharacter("procedure").

RunBlock:
DO ON ERROR UNDO, THROW
   ON QUIT UNDO, RETRY:
  IF RETRY THEN DO:
    MESSAGE "QUIT statement found".
    IF noErrorOnQuit THEN i = 0. ELSE i = 66.
    LEAVE RunBlock.
  END.
  
  /* Reset i */
  ASSIGN i = ?.
  
  /* Allow these calls to raise exceptions */
  IF (outprmEntries:Length EQ 0) THEN
    RUN VALUE(configJson:getCharacter("procedure")).
  ELSE IF (outprmEntries:Length EQ 1) THEN
    RUN VALUE(configJson:getCharacter("procedure")) (OUTPUT out1).
  ELSE IF (outprmEntries:Length EQ 2) THEN
    RUN VALUE(configJson:getCharacter("procedure")) (OUTPUT out1, OUTPUT out2).
      
  CATCH e AS Progress.Lang.Error :
     /* caught an error: consider dumping error stack */
     MESSAGE "CAUGHT AN ERROR: " VIEW-AS ALERT-BOX.
     DO i = 1 TO e:NumMessages:
        MESSAGE e:GetMessage(i).
     END.
     IF TYPE-OF(e,Progress.Lang.AppError) THEN
        MESSAGE subst("Returnval: &1",cast(e,Progress.Lang.AppError):ReturnValue) VIEW-AS ALERT-BOX.
        
     DELETE OBJECT e no-error.
     ASSIGN e = ?
            i = 1.
     /* Error-status:Error is reset to false at this point */            
  END CATCH. 
END.

IF ERROR-STATUS:ERROR THEN
  ASSIGN i = 1.
IF (i EQ ?) THEN
  ASSIGN i = INTEGER (ENTRY(1, RETURN-VALUE, " ")) NO-ERROR.
IF (i EQ ?) THEN
  ASSIGN i = 1.
RUN returnValue(i).

IF (outprmEntries:Length GE 1) THEN
  RUN writeOutputParam (out1, outprmEntries:getCharacter(1)).
IF (outprmEntries:Length GE 2) THEN
  RUN writeOutputParam (out2, outprmEntries:getCharacter(2)).

QUIT.    

PROCEDURE returnValue PRIVATE.
  DEFINE INPUT PARAMETER retVal AS INTEGER NO-UNDO.

  IF pctVerbose THEN
    MESSAGE SUBSTITUTE("Return value : &1", retVal).
  OUTPUT TO VALUE(configJson:getCharacter("returnValue")) CONVERT TARGET "utf-8".
  PUT UNFORMATTED retVal SKIP.
  OUTPUT CLOSE.

END PROCEDURE.

PROCEDURE writeOutputParam PRIVATE.
  DEFINE INPUT PARAMETER prm AS CHARACTER NO-UNDO.
  DEFINE INPUT PARAMETER outFile AS CHARACTER NO-UNDO.

  IF pctVerbose THEN
    MESSAGE SUBSTITUTE("OUTPUT PARAMETER : &1", prm).
  OUTPUT TO VALUE(outFile) CONVERT TARGET "utf-8".
  PUT UNFORMATTED prm SKIP.
  OUTPUT CLOSE.

END PROCEDURE.
