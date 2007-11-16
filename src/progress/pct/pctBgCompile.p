/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

&GLOBAL-DEFINE NO_ERROR "OK:"
&GLOBAL-DEFINE MESSAGE "OK:~~n&1"
&GLOBAL-DEFINE SEPARATOR '|'

DEFINE TEMP-TABLE CRCList NO-UNDO
  FIELD ttTable AS CHARACTER
  FIELD ttCRC   AS CHARACTER
  INDEX ttCRC-PK IS PRIMARY UNIQUE ttTable.
DEFINE TEMP-TABLE TimeStamps NO-UNDO
  FIELD ttFile     AS CHARACTER CASE-SENSITIVE
  FIELD ttFullPath AS CHARACTER CASE-SENSITIVE
  FIELD ttMod      AS INTEGER
  INDEX PK-TimeStamps IS PRIMARY UNIQUE ttFile.
DEFINE TEMP-TABLE ttXref NO-UNDO
    FIELD xProcName   AS CHARACTER
    FIELD xFileName   AS CHARACTER
    FIELD xLineNumber AS INTEGER
    FIELD xRefType    AS CHARACTER
    FIELD xObjID      AS CHARACTER FORMAT "X(50)"
    index typ is primary xRefType.
DEFINE TEMP-TABLE ttDirs NO-UNDO
    FIELD baseDir AS CHARACTER
    FIELD dirName AS CHARACTER.

FUNCTION getTimeStampDF RETURN INTEGER (INPUT d AS CHARACTER, INPUT f AS CHARACTER) FORWARD.
FUNCTION getTimeStampF RETURN INTEGER (INPUT f AS CHARACTER) FORWARD.
FUNCTION getDate RETURNS INTEGER (INPUT dt AS DATE, INPUT tm AS INTEGER) FORWARD.
FUNCTION CheckIncludes RETURNS LOGICAL  (INPUT f AS CHARACTER, INPUT TS AS INTEGER) FORWARD.
FUNCTION CheckCRC RETURNS LOGICAL (INPUT f AS CHARACTER) FORWARD.
FUNCTION FileExists RETURNS LOGICAL (INPUT f AS CHARACTER) FORWARD.
FUNCTION createDir RETURNS LOGICAL (INPUT base AS CHARACTER, INPUT d AS CHARACTER) FORWARD.
FUNCTION getCompileErrors RETURNS CHARACTER (pcInit AS CHAR, pcFile AS CHAR, piRow AS INT, piColumn AS INT, pcMsg AS CHAR) FORWARD.

/** Named streams */
DEFINE STREAM sXref.
DEFINE STREAM sIncludes.
DEFINE STREAM sCRC.

/** Parameters from ANT call */
DEFINE VARIABLE MinSize   AS LOGICAL    NO-UNDO INITIAL FALSE.
DEFINE VARIABLE MD5       AS LOGICAL    NO-UNDO INITIAL FALSE.
DEFINE VARIABLE ForceComp AS LOGICAL    NO-UNDO INITIAL FALSE.
DEFINE VARIABLE NoComp    AS LOGICAL    NO-UNDO INITIAL FALSE.
DEFINE VARIABLE RunList   AS LOGICAL    NO-UNDO INITIAL FALSE.
DEFINE VARIABLE lXCode    AS LOGICAL    NO-UNDO INITIAL FALSE.
DEFINE VARIABLE XCodeKey  AS CHARACTER  NO-UNDO INITIAL ?.

PROCEDURE getCRC.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

	/* Gets CRC list */
	DEFINE VARIABLE h AS HANDLE     NO-UNDO.
	h = TEMP-TABLE CRCList:HANDLE.
	RUN pct/pctCRC.p (INPUT-OUTPUT TABLE-HANDLE h) NO-ERROR.
	IF (RETURN-VALUE NE '0') THEN
	    RETURN SUBSTITUTE({&MESSAGE}, RETURN-VALUE).

    RETURN {&NO_ERROR}.
END PROCEDURE.

PROCEDURE setRunList.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

	ASSIGN RunList = TRUE.
    RETURN {&NO_ERROR}.

END PROCEDURE.

PROCEDURE setMinSize.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

	ASSIGN MinSize = TRUE.
    RETURN {&NO_ERROR}.

END PROCEDURE.

PROCEDURE setMD5.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

	ASSIGN MD5 = TRUE.
    RETURN {&NO_ERROR}.

END PROCEDURE.

PROCEDURE setXCode.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

	ASSIGN lXCode = TRUE.
    RETURN {&NO_ERROR}.

END PROCEDURE.

PROCEDURE setXCodeKey.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

    ASSIGN XCodeKey = cPrm.
    RETURN {&NO_ERROR}.

END PROCEDURE.

PROCEDURE setForceCompilation.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

    ASSIGN ForceComp = TRUE.
    RETURN {&NO_ERROR}.

END PROCEDURE.

PROCEDURE setNoCompile.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

    ASSIGN NoComp = TRUE.
    RETURN {&NO_ERROR}.

END PROCEDURE.

/* Return value of this procedure follows this pattern :
 *  OK:
 *  -1 if not recompiled, 0 if compilation successful, or the numbers of compilation problems
 *  Zero to n lines of compiler output
 */
PROCEDURE pctCompile.
    DEFINE INPUT PARAM cPrm AS CHARACTER  NO-UNDO.

    /* Input parameter is a pipe-delimited list consisting of these entries :
     *   -> Input File : Full pathname of file to compile
     *   -> Output directory : Full pathname of directory in which to put compiled file
     *   -> Debug listing file : Full pathname of debug listing result file
     *      If empty, don't create a debug listing
     *   -> Preprocessor file : Full pathname of preprocessor result file
     *      If empty, don't create a preprocessor file
     *   -> Listing file : Full pathname of listing file
     *      If empty, don't create a listing file
     *   -> PCT base : Root file name for PCT temp files, i.e. .crc and .inc files
     *   -> Target file : if file name is different from what Progress generates
     *      If empty, keep generated file as is
     */

	DEFINE VARIABLE inputFile   AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE outputDir   AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE dbgList     AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE prepro      AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE listingFile AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE xrefFile    AS CHARACTER  NO-UNDO.
    DEFINE VARIABLE pctDir      AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE targetFile  AS CHARACTER  NO-UNDO.
	DEFINE VARIABLE retval      AS CHARACTER  NO-UNDO.
	
	DEFINE VARIABLE Recompile AS LOGICAL NO-UNDO.
	
    DEFINE VARIABLE FileExt   AS CHARACTER   NO-UNDO.
    DEFINE VARIABLE RCodeName AS CHARACTER   NO-UNDO.
    DEFINE VARIABLE RCodeTS   AS INTEGER     NO-UNDO.
    DEFINE VARIABLE ProcTS    AS INTEGER     NO-UNDO.
    DEFINE VARIABLE zz        AS INTEGER     NO-UNDO.
    DEFINE VARIABLE errMsgs   AS CHARACTER   NO-UNDO.
    DEFINE VARIABLE cBase     AS CHARACTER   NO-UNDO.
    DEFINE VARIABLE cFile     AS CHARACTER   NO-UNDO.

    /* Extracts path from parameter */
	ASSIGN inputFile   = ENTRY(1, cPrm, {&SEPARATOR})
	       outputDir   = ENTRY(2, cPrm, {&SEPARATOR})
	       dbgList     = ENTRY(3, cPrm, {&SEPARATOR})
	       prepro      = ENTRY(4, cPrm, {&SEPARATOR})
	       listingFile = ENTRY(5, cPrm, {&SEPARATOR})
	       xrefFile    = ENTRY(6, cPrm, {&SEPARATOR})
	       pctDir      = ENTRY(7, cPrm, {&SEPARATOR})
	       targetFile  = ENTRY(8, cPrm, {&SEPARATOR}).

    /* Empty paths are set to NULL */
    ASSIGN dbgList     = (IF dbgList EQ '' THEN ? ELSE dbgList)
           prepro      = (IF prepro EQ '' THEN ? ELSE prepro)
           listingFile = (IF listingFile EQ '' THEN ? ELSE listingFile)
           xrefFile    = (IF xrefFile EQ '' THEN ? ELSE xrefFile)
           targetFile  = (IF targetFile EQ '' THEN ? ELSE targetFile).

    IF (ForceComp OR lXCode) THEN DO:
        ASSIGN Recompile = TRUE.
    END.
    ELSE DO:
        /* Checking .r file exists */
        RUN adecomm/_osprefx.p(inputFile, OUTPUT cBase, OUTPUT cFile).
        RUN adecomm/_osfext.p(cFile, OUTPUT FileExt).
        ASSIGN RCodeName = SUBSTRING(cFile, 1, R-INDEX(cFile, FileExt) - 1) + '.r':U.
        ASSIGN RCodeTS = getTimeStampDF(outputDir, RCodeName).
        Recompile = (RCodeTS EQ ?).
        IF (NOT Recompile) THEN DO:
            /* Checking .r timestamp is prior procedure timestamp */
            ASSIGN ProcTS = getTimeStampF(inputFile).
            Recompile = (ProcTS GT RCodeTS).
            IF (NOT Recompile) THEN DO:
                /* Checking included files */
                Recompile = CheckIncludes(PCTDir + '.inc', RCodeTS).
                IF (NOT Recompile) THEN DO:
                    /* Checking CRC */
                    Recompile = CheckCRC(PCTDir + '.crc').
                END.
            END.
        END.
	END.
    
    /* FIXME Gestion de l'attribut noCompile */

    IF Recompile THEN DO:
        IF lXCode THEN DO:
            IF (XCodeKey NE ?) THEN
                COMPILE VALUE(inputFile) SAVE INTO VALUE(outputDir) MIN-SIZE=MinSize GENERATE-MD5=MD5 XCODE XCodeKey NO-ERROR.
            ELSE
                COMPILE VALUE(inputFile) SAVE INTO VALUE(outputDir) MIN-SIZE=MinSize GENERATE-MD5=MD5 NO-ERROR.
        END.
        ELSE
            COMPILE VALUE(inputFile) SAVE INTO VALUE(outputDir) DEBUG-LIST VALUE(dbgList) PREPROCESS VALUE(prepro) LISTING VALUE(listingFile) MIN-SIZE=MinSize GENERATE-MD5=MD5 XREF VALUE(xreffile) APPEND=FALSE NO-ERROR.
        IF COMPILER:ERROR THEN DO:
            ASSIGN retVal = STRING(ERROR-STATUS:NUM-MESSAGES) + "~n".
            DO zz = 1 TO ERROR-STATUS:NUM-MESSAGES:
                ASSIGN errMsgs = errMsgs + ERROR-STATUS:GET-MESSAGE(zz) + '~n':U.
            END.
            ASSIGN retVal = retVal + getCompileErrors(inputFile, SEARCH(COMPILER:FILE-NAME), COMPILER:ERROR-ROW, COMPILER:ERROR-COLUMN, errMsgs).
        END.
        ELSE DO:
            IF (targetFile NE ?) THEN DO:
                OS-COPY VALUE(outputDir + "/" + RCodeName) VALUE(targetFile).
                OS-DELETE VALUE(outputDir + "/" + RCodeName).
            END.
            ASSIGN retVal = "0~n".
            RUN ImportXref (INPUT xreffile, INPUT pctdir) NO-ERROR.
            OS-DELETE VALUE(xrefFile).
        END.
        RETURN SUBSTITUTE({&MESSAGE}, retVal).
    END.
    ELSE DO:
        RETURN SUBSTITUTE({&MESSAGE}, "-1").
    END.
        	
END PROCEDURE.

FUNCTION CheckIncludes RETURNS LOGICAL (INPUT f AS CHARACTER, INPUT TS AS INTEGER).
    DEFINE VARIABLE IncFile     AS CHARACTER  NO-UNDO.
    DEFINE VARIABLE IncFullPath AS CHARACTER  NO-UNDO.
    DEFINE VARIABLE lReturn     AS LOGICAL    NO-UNDO INITIAL FALSE.

    INPUT STREAM sIncludes FROM VALUE (f).
    FileList:
    REPEAT:
        IMPORT STREAM sIncludes IncFile IncFullPath.
        FIND TimeStamps WHERE TimeStamps.ttFile EQ IncFile NO-LOCK NO-ERROR.
        IF (NOT AVAILABLE TimeStamps) THEN DO:
            CREATE TimeStamps.
            ASSIGN TimeStamps.ttFile = IncFile
                   TimeStamps.ttFullPath = SEARCH(IncFile).
            ASSIGN TimeStamps.ttMod = getTimeStampF(TimeStamps.ttFullPath).
        END.
        IF (TimeStamps.ttFullPath NE IncFullPath) OR (TS LT TimeStamps.ttMod) THEN DO:
            ASSIGN lReturn = TRUE.
            LEAVE FileList.
        END.
    END.
    INPUT STREAM sIncludes CLOSE.
    RETURN lReturn.

END FUNCTION.

FUNCTION CheckCRC RETURNS LOGICAL (INPUT f AS CHARACTER).
    DEFINE VARIABLE cTab AS CHARACTER  NO-UNDO.
    DEFINE VARIABLE cCRC AS CHARACTER  NO-UNDO.
    DEFINE VARIABLE lRet AS LOGICAL    NO-UNDO INITIAL FALSE.

    INPUT STREAM sCRC FROM VALUE(f).
    CRCList:
    REPEAT:
        IMPORT STREAM sCRC cTab cCRC.
        FIND CRCList WHERE CRCList.ttTable EQ cTab NO-LOCK NO-ERROR.
        IF (NOT AVAILABLE CRCList) THEN DO:
            ASSIGN lRet = TRUE.
            LEAVE CRCList.
        END.
        IF (CRCList.ttCRC NE cCRC) THEN DO:
            ASSIGN lRet = TRUE.
            LEAVE CRCList.
        END.
    END.
    INPUT STREAM sCRC CLOSE.
    RETURN lRet.

END FUNCTION.

FUNCTION getCompileErrors RETURNS CHARACTER (pcInit AS CHAR, pcFile AS CHAR, piRow AS INT, piColumn AS INT, pcMsg AS CHAR):
    
    DEFINE VARIABLE i AS INTEGER    NO-UNDO INITIAL 1.
    DEFINE VARIABLE c AS CHARACTER  NO-UNDO.
    DEFINE VARIABLE tmp AS CHARACTER   NO-UNDO.
    IF (pcInit EQ pcFile) THEN
        ASSIGN c = SUBSTITUTE("Error compiling file &1 at line &2 column &3~n", pcInit, piRow, piColumn).
    ELSE
        ASSIGN c = SUBSTITUTE("Error compiling file &1 in included file &4 at line &2 column &3~n", pcInit, piRow, piColumn, pcFile).
    INPUT STREAM sXref FROM VALUE((IF pcInit EQ pcFile THEN pcInit ELSE pcFile)).
    DO i = 1 TO piRow - 1:
        IMPORT STREAM sXref UNFORMATTED tmp.
    END.
    IMPORT STREAM sXref UNFORMATTED tmp.
    ASSIGN c = c + tmp + FILL('-':U, piColumn - 2) + '-^~n':U + pcMsg + '~n'.
    
    INPUT STREAM sXref CLOSE.
    RETURN c.

END FUNCTION.

PROCEDURE importXref.
    DEFINE INPUT  PARAMETER pcXref AS CHARACTER NO-UNDO.
    DEFINE INPUT  PARAMETER pcDir  AS CHARACTER NO-UNDO.

    DEFINE VARIABLE cSearch AS CHARACTER  NO-UNDO.

    EMPTY TEMP-TABLE ttXref.

    INPUT STREAM sXREF FROM VALUE (pcXref).
    REPEAT:
        CREATE ttXref.
        IMPORT STREAM sXREF ttXref.
        IF (ttXref.xRefType EQ 'INCLUDE':U) OR (RunList AND (ttXref.xRefType EQ 'RUN':U)) THEN
            ttXref.xObjID = ENTRY(1, TRIM(ttXref.xObjID), ' ':U).
        ELSE IF (LOOKUP(ttXref.xRefType, 'CREATE,REFERENCE,ACCESS,UPDATE,SEARCH':U) EQ 0) THEN
            DELETE ttXref.
    END.
    DELETE ttXref. /* ttXref is non-undo'able */
    INPUT STREAM sXREF CLOSE.

    OUTPUT TO VALUE (pcdir + '.inc':U).
    FOR EACH ttXref WHERE xRefType EQ 'INCLUDE':U NO-LOCK BREAK BY ttXref.xObjID:
    	IF FIRST-OF (ttXref.xObjID) THEN
            EXPORT ttXref.xObjID SEARCH(ttXref.xObjID).
    END.
    OUTPUT CLOSE.
    
    OUTPUT TO VALUE (pcdir +  '.crc':U).
    FOR EACH ttXref WHERE LOOKUP(ttXref.xRefType, 'CREATE,REFERENCE,ACCESS,UPDATE,SEARCH':U) NE 0 NO-LOCK BREAK BY ttXref.xObjID:
    	IF FIRST-OF (ttXref.xObjID) THEN DO:
            FIND CRCList WHERE CRCList.ttTable EQ ttXref.xObjID NO-LOCK NO-ERROR.
            IF (AVAILABLE CRCList) THEN DO:
                EXPORT CRCList.
            END.
        END.
    END.
    OUTPUT CLOSE.

    IF RunList THEN DO:
        OUTPUT TO VALUE (pcdir + '.run':U).
        FOR EACH ttXref WHERE xRefType EQ 'RUN':U AND ((ttXref.xObjID MATCHES '*~~.p') OR (ttXref.xObjID MATCHES '*~~.w')) NO-LOCK BREAK BY ttXref.xObjID:
            IF FIRST-OF (ttXref.xObjID) THEN DO:
                FIND TimeStamps WHERE TimeStamps.ttFile EQ ttXref.xObjID NO-LOCK NO-ERROR.
                IF (NOT AVAILABLE TimeStamps) THEN DO:
                	ASSIGN cSearch = SEARCH(SUBSTRING(ttXref.xObjID, 1, R-INDEX(ttXref.xObjID, '.')) + 'r').
                	IF (cSearch EQ ?) THEN
                        ASSIGN cSearch = SEARCH(ttXref.xObjID).
                    CREATE TimeStamps.
                    ASSIGN TimeStamps.ttFile = ttXref.xObjID
                           TimeStamps.ttFullPath = (IF cSearch EQ ? THEN 'NOT FOUND' ELSE cSearch).
                    ASSIGN TimeStamps.ttMod = getTimeStampF(TimeStamps.ttFullPath).
                END.
                EXPORT ttXref.xObjID TimeStamps.ttFullPath.
            END.
        END.
        OUTPUT CLOSE.
    END.

END PROCEDURE.

FUNCTION getTimeStampDF RETURNS INTEGER(INPUT d AS CHARACTER, INPUT f AS CHARACTER):
    RETURN getTimeStampF(d + '/':U + f).
END FUNCTION.

FUNCTION getTimeStampF RETURNS INTEGER(INPUT f AS CHARACTER):
    ASSIGN FILE-INFO:FILE-NAME = f.
    RETURN getDate(FILE-INFO:FILE-MOD-DATE, FILE-INFO:FILE-MOD-TIME).
END FUNCTION.

FUNCTION getDate RETURNS INTEGER (INPUT dt AS DATE, INPUT tm AS INTEGER):
    IF (dt EQ ?) OR (tm EQ ?) THEN RETURN ?.
    RETURN (INTEGER(dt) - INTEGER(DATE(1, 1, 1990))) * 86400 + tm.
END FUNCTION.

FUNCTION fileExists RETURNS LOGICAL (INPUT f AS CHARACTER):
    ASSIGN FILE-INFO:FILE-NAME = f.
    RETURN (FILE-INFO:FULL-PATHNAME NE ?).
END FUNCTION.

FUNCTION createDir RETURNS LOGICAL (INPUT base AS CHARACTER, INPUT d AS CHARACTER):
    DEFINE VARIABLE i AS INTEGER    NO-UNDO.
    DEFINE VARIABLE c AS CHARACTER  NO-UNDO.

    /* Asserts base is a writable directory */
    FIND ttDirs WHERE ttDirs.baseDir EQ base
                  AND ttDirs.dirName EQ d
                NO-LOCK NO-ERROR.
    IF (AVAILABLE ttDirs) THEN
        RETURN TRUE.

    ASSIGN d = REPLACE(d, '~\':U, '/':U).
    DO i = 1 TO NUM-ENTRIES(d, '/':U):
        ASSIGN c = c + '/':U + ENTRY(i, d, '/':U).
        FIND ttDirs WHERE ttDirs.baseDir EQ base
                      AND ttDirs.dirName EQ c
                    NO-LOCK NO-ERROR.
        IF (NOT AVAILABLE ttDirs) THEN DO:
            OS-CREATE-DIR VALUE(base + c).
            IF (OS-ERROR EQ 0) THEN DO:
                CREATE ttDirs.
                ASSIGN ttDirs.baseDir = base
                       ttDirs.dirName = c.
            END.
            ELSE DO:
                RETURN FALSE.
            END.
        END.
    END.
    RETURN TRUE.

END FUNCTION.