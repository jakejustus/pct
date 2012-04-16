/*********************************************************************
* Copyright (C) 2000 by Progress Software Corporation ("PSC"),       *
* 14 Oak Park, Bedford, MA 01730, and other contributors as listed   *
* below.  All Rights Reserved.                                       *
*                                                                    *
* The Initial Developer of the Original Code is PSC.  The Original   *
* Code is Progress IDE code released to open source December 1, 2000.*
*                                                                    *
* The contents of this file are subject to the Possenet Public       *
* License Version 1.0 (the "License"); you may not use this file     *
* except in compliance with the License.  A copy of the License is   *
* available as of the date of this notice at                         *
* http://www.possenet.org/license.html                               *
*                                                                    *
* Software distributed under the License is distributed on an "AS IS"*
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. You*
* should refer to the License for the specific language governing    *
* rights and limitations under the License.                          *
*                                                                    *
* Contributors:                                                      *
*                                                                    *
*********************************************************************/


/*--------------------------------------------------------------------   

File: prodict/dump_inc.p

Description:
    Batch-mode incremental .df maker 
    DICTDB  is the current database 
            (it's the first connected database, "master")
    DICTDB2 is the database chosen to compare against (second connected,
            (it's the second connected database, "slave")

Usage:
    In scripts, e.q.:
       #!/bin/sh
       DUMP_INC_DFFILE=/tmp/delta.df
       DUMP_INC_CODEPAGE=iso8859-2
       DUMP_INC_INDEXMODE=0
       DUMP_INC_RENAMEFILE=/tmp/master.rf
       DUMP_INC_DEBUG=2
       export DUMP_INC_DFFILE DUMP_INC_CODEPAGE DUMP_INC_INDEXMODE \
              DUMP_INC_RENAMEFILE DUMP_INC_DEBUG

       $DLC/bin/_progres -b -db master \
                            -db slave  \ 
                            -p prodict/dump_inc.p > /tmp/dump_inc.log
    
Environment Variables:
    DUMP_INC_DFFILE          : name of file to dump to
    DUMP_INC_CODEPAGE        : output codepage
    DUMP_INC_INDEXMODE       : index-mode for newly created indexes in exsting
                               tables: 0 = all indexes active
                                       1 = all unique indexes inactive
                                       2 = all indexes inactive
    DUMP_INC_RENAMEFILE      : name of the file with rename definitions
    DUMP_INC_DEBUG           : debug-level: 0 = debug off (only errors
                                                and important warnings)
                                            1 = all the above plus all warnings
                                            2 = all the above plus config info
    
History
    Gary C    01/06/21  This FILE created, author of the original idea
    vap       02/01/29  patched accordingly to changed specs

Code-page - support:
    code-page = ?,""          : default conversion (SESSION:STREAM)
    code-page = "<code-page>" : convert to <code-page>

    if not convertable to code-page try to convert to SESSION:STREAM

rename field support
  The rename-file parameter is used to identify tables, database fields
  and sequences that have changed names. The format of the file is a comma 
  seperated list that identifies the renamed object, its old name and the new 
  name. When an object is found missing, this file is checked to determine if
  it was renamed.  If no matching entry is found, then the object
  If rename-file is ? or "", then all missing objects are deleted.
  The rename-file has following format:
       T,<old-table-name>,<new-table-name>
       F,<table-name>,<old-field-name>,<new-field-name>
       S,<old-sequence-name>,<new-sequence-name>

--------------------------------------------------------------------*/        
/*h-*/

/* Definitions */ /*-------------------------------------------------------*/

&GLOBAL-DEFINE errFileName "incrdump.e"

&SCOPED-DEFINE VAR_PREFIX       DUMP_INC
&SCOPED-DEFINE DEFAULT_DF       delta.df
&SCOPED-DEFINE DEFAULT_INDEX    1

DEFINE VARIABLE rename-file  AS CHARACTER NO-UNDO.
DEFINE VARIABLE df-file-name AS CHARACTER NO-UNDO.
DEFINE VARIABLE code-page    AS CHARACTER NO-UNDO.
DEFINE VARIABLE index-mode   AS INTEGER   NO-UNDO INITIAL ?.
DEFINE VARIABLE debug-mode   AS INTEGER   NO-UNDO.

DEFINE VARIABLE foo          AS CHARACTER NO-UNDO.

DEFINE STREAM err-log. 

{ prodict/user/uservar102b.i NEW }
{ prodict/user/userhue102b.i NEW }
{ prodict/dictvar102b.i NEW }

/* LANGUAGE DEPENDENCIES START */ /*----------------------------------------*/
DEFINE VARIABLE new_lang AS CHARACTER EXTENT 06 NO-UNDO INITIAL [
  /*01*/ "ERROR: ~"&1~" only runs in batch mode." ,
  /*02*/ "ERROR: You must have at least 2 databases connected." ,
  /*03*/ ?  /* see below */ ,
  /*04*/ "Using default value of ~"&1~" for &2." ,
  /*05*/ ?  /* see below */ ,
  /*06*/ ?  /* see below */ 
]. 
new_lang[03] = "WARNING: Rename file ~"&1~" doesn~'t exist or is unreadable," +
               " ignoring.".
new_lang[05] = "WARNING: ~"&1~" is not valid codepage. " +
               "Using default value of ~"&2~" instead.".
new_lang[06] = "WARNING: ~"&1~" is not valid ~"index-mode~" identifier. " +
               "Using default value of ~"&2~" instead.".
/* LANGUAGE DEPENDENCIES END */ /*-------------------------------------------*/

ASSIGN debug-mode   = INTEGER(DYNAMIC-FUNCTION('getParameter' IN SOURCE-PROCEDURE, INPUT 'DebugMode'))
       rename-file  = DYNAMIC-FUNCTION('getParameter' IN SOURCE-PROCEDURE, INPUT 'RenameFile')
       df-file-name = DYNAMIC-FUNCTION('getParameter' IN SOURCE-PROCEDURE, INPUT 'DFFileName')
       code-page    = DYNAMIC-FUNCTION('getParameter' IN SOURCE-PROCEDURE, INPUT 'CodePage')
       index-mode   = INTEGER(DYNAMIC-FUNCTION('getParameter' IN SOURCE-PROCEDURE, INPUT 'IndexMode')).

IF NUM-DBS LT 2 THEN DO:
  MESSAGE new_lang[02].
  RETURN.
END.  /* NUM-DBS LT 2 */


/* test, if `rename-file' exists */
IF rename-file NE "":U THEN DO:
  ASSIGN FILE-INFO:FILE-NAME = rename-file.
  IF FILE-INFO:FILE-TYPE MATCHES "*R*":U THEN.  /* this deals with the ? */
  ELSE DO:
    IF debug-mode GT 0 THEN
      MESSAGE SUBSTITUTE(new_lang[03], rename-file) SKIP.
    ASSIGN rename-file = "":U.
  END.
END.

/* setting the default value for df file */
IF df-file-name EQ "":U THEN DO:
  ASSIGN df-file-name = "{&DEFAULT_DF}":U.
  IF debug-mode GT 0 THEN
    MESSAGE SUBSTITUTE(new_lang[04], "{&DEFAULT_DF}":U, "delta file":U) SKIP.
END.

/* codepage checking */
IF code-page NE "":U THEN DO:
  ASSIGN foo = CODEPAGE-CONVERT("x":U, SESSION:CPSTREAM, code-page) NO-ERROR.
  IF ERROR-STATUS:ERROR OR ERROR-STATUS:NUM-MESSAGES GT 0 THEN DO:
    IF debug-mode GT 0 THEN
      MESSAGE SUBSTITUTE(new_lang[05], code-page, SESSION:CPSTREAM) SKIP.
    ASSIGN code-page = SESSION:CPSTREAM.
  END.  /* codepage error */
  ERROR-STATUS:ERROR = NO.
END.
ELSE DO:
  ASSIGN code-page = SESSION:CPSTREAM.
  IF debug-mode GT 0 THEN
    MESSAGE SUBSTITUTE(new_lang[04], code-page, "code page":U) SKIP.
END.  /* code-page EQ "":U */

/* index-mode checking */
IF index-mode NE ? THEN DO:
  IF NOT CAN-DO("0,1,2":U, STRING(index-mode)) THEN DO:
    IF debug-mode GT 0 THEN
      MESSAGE SUBSTITUTE(new_lang[06], STRING(index-mode), "{&DEFAULT_INDEX}":U) SKIP.
    ASSIGN index-mode = INTEGER("{&DEFAULT_INDEX}":U).
  END.
END.
ELSE DO:
  ASSIGN index-mode = INTEGER("{&DEFAULT_INDEX}":U).
  IF debug-mode GT 0 THEN
    MESSAGE SUBSTITUTE(new_lang[04], STRING(index-mode), "index mode":U) SKIP.
END.  /* index-mode EQ "":U */

/* user_env[19] will be changed BY _dmpincr.p */
ASSIGN user_env[19] = rename-file + ",":U + STRING(index-mode) + ",":U +
                      STRING(debug-mode)
       user_env[02] = df-file-name
       user_env[05] = code-page.

IF debug-mode GT 1 THEN DO:
  MESSAGE "DUMP_INC_DFFILE     = ":U df-file-name SKIP.
  MESSAGE "DUMP_INC_CODEPAGE   = ":U code-page SKIP.
  MESSAGE "DUMP_INC_INDEXMODE  = ":U index-mode SKIP.
  MESSAGE "DUMP_INC_RENAMEFILE = ":U rename-file SKIP.
  MESSAGE "DUMP_INC_DEBUG      = ":U debug-mode SKIP.
END.  /* debug-mode GT 1 */

IF debug-mode GT 0 THEN DO:
  MESSAGE "" SKIP.
END.

FIND FIRST DICTDB._Db NO-LOCK.
ASSIGN drec_db = RECID(DICTDB._Db).

DELETE ALIAS "DICTDB2":U.
CREATE ALIAS "DICTDB2":U FOR DATABASE VALUE(LDBNAME(2)).

RUN pct/_dmpincr102b.p.

RETURN '0'.