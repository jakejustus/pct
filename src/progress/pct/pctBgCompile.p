/**
 * Copyright 2005-2019 Riverside Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

&GLOBAL-DEFINE SEPARATOR '|'
&GLOBAL-DEFINE SEPARATOR2 '*'

DEFINE VARIABLE hComp    AS HANDLE NO-UNDO.
DEFINE VARIABLE hSrcProc AS HANDLE NO-UNDO.
DEFINE VARIABLE lStopOnErr AS LOGICAL NO-UNDO.

RUN pct/compile.p PERSISTENT SET hComp.
ASSIGN hSrcProc = SOURCE-PROCEDURE.

PROCEDURE setOptions:
    DEFINE INPUT  PARAMETER ipPrm AS CHARACTER   NO-UNDO.
    DEFINE OUTPUT PARAMETER opOK  AS LOGICAL     NO-UNDO.
    DEFINE OUTPUT PARAMETER opMsg AS CHARACTER NO-UNDO.

    /* Defines compilation option -- This is a ';' separated string containing */
    /* runList (LOG), minSize (LOG), md5 (LOG), xcode (LOG), xcodekey (CHAR), forceCompil (LOG), noCompil (LOG), keepXref (LOG), multiComp (LOG), streamIO (LOG), lV6Frame (LOG), outputDir (CHAR), preprocess (LOG), preprocessDir (CHAR), listing (LOG), debugListing (LOG), debugListingDir (CHAR), reqFullKW (LOG), reqFullNames (LOG), reqFldQual (LOG), callbackClass (CHAR) */
    RUN setOption IN hComp ('RUNLIST', IF ENTRY(1, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('XCODE', IF ENTRY(4, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('FORCECOMPILE', IF ENTRY(6, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('NOCOMPILE', IF ENTRY(7, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('KEEPXREF', IF ENTRY(8, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('MULTICOMPILE', IF ENTRY(11, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('RELATIVE', IF ENTRY(14, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    IF (ENTRY(15, ipPrm, ';') GT "") THEN
      RUN setOption IN hComp ('OUTPUTDIR', ENTRY(15, ipPrm, ';')).
    RUN setOption IN hComp ('PREPROCESS', IF ENTRY(16, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('PREPROCESSDIR', ENTRY(17, ipPrm, ';')).
    RUN setOption IN hComp ('LISTING', IF ENTRY(18, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('DEBUGLISTING', IF ENTRY(19, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('DEBUGLISTINGDIR', ENTRY(20, ipPrm, ';')).
    RUN setOption IN hComp ('IGNOREDINCLUDES', ENTRY(21, ipPrm, ';')).
    RUN setOption IN hComp ('XMLXREF', IF ENTRY(22, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('STRINGXREF', IF ENTRY(23, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('APPENDSTRINGXREF', IF ENTRY(24, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('LISTINGSOURCE', ENTRY(26, ipPrm, ';')).
    RUN setOption IN hComp ('NOPARSE', IF ENTRY(27, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    ASSIGN lStopOnErr = ENTRY(28, ipPrm, ';') EQ 'true'.
    RUN setOption IN hComp ('FLATTENDBG', IF ENTRY(29, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('PCTDIR', ENTRY(30, ipPrm, ';')).
    RUN setOption IN hComp ('FILELIST', ENTRY(31, ipPrm, ';')).
    RUN setOption IN hComp ('FULLKW', IF ENTRY(32, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('FULLNAMES', IF ENTRY(33, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('FIELDQLF', IF ENTRY(34, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').
    RUN setOption IN hComp ('CALLBACKCLASS', ENTRY(35, ipPrm, ';')).
    RUN setOption IN hComp ('OUTPUTTYPE', ENTRY(36, ipPrm, ';')).
    RUN setOption IN hComp ('RETURNVALUES', IF ENTRY(37, ipPrm, ';') EQ 'true' THEN '1' ELSE '0').

    RUN setOption IN hComp ('TEMPOUTPUT', ENTRY(38, ipPrm, ';')).
    
    RUN initModule IN hComp.

    ASSIGN opOk = TRUE.

END PROCEDURE.

PROCEDURE pctCompile:
    DEFINE INPUT  PARAMETER ipPrm AS CHARACTER   NO-UNDO.
    DEFINE OUTPUT PARAMETER opOK  AS LOGICAL     NO-UNDO INITIAL FALSE.
    DEFINE OUTPUT PARAMETER opMsg AS CHARACTER NO-UNDO.

    /* Input parameter is a star-separated list of compilation units */
    /* Each compilation unit is a pipe-separated list of infos : */
    /*  -> Fileset directory - Full pathname (CHAR) */
    /*  -> Input file directory - relative path */
    /*  -> Input file to compile - just the file name (CHAR) */
    /*  -> Target file name (CHAR) */

  DEFINE VARIABLE zz        AS INTEGER     NO-UNDO.
  DEFINE VARIABLE compOK    AS INTEGER     NO-UNDO.
  DEFINE VARIABLE compNotOK AS INTEGER     NO-UNDO.
  DEFINE VARIABLE skipped   AS INTEGER     NO-UNDO.
  DEFINE VARIABLE cc        AS CHARACTER   NO-UNDO.

  DEFINE VARIABLE lErr AS LOGICAL NO-UNDO.
  DEFINE VARIABLE opComp AS INTEGER NO-UNDO.

  FileLoop:
  DO zz = 1 TO NUM-ENTRIES(ipPrm, {&SEPARATOR2}):
    ASSIGN cc = ENTRY(zz, ipPrm, {&SEPARATOR2}).

    RUN compileXref IN hComp
      (ENTRY(1, cc, {&SEPARATOR}),
       ENTRY(2, cc, {&SEPARATOR}),
       ENTRY(3, cc, {&SEPARATOR}),
       OUTPUT lErr,
       OUTPUT opComp).

    IF lErr EQ FALSE THEN DO:
      ASSIGN compOK = compOK + 1
             skipped = skipped + (IF opComp EQ 0 THEN 1 ELSE 0).
    END.
    ELSE DO:
      ASSIGN compNotOk = compNotOK + 1.
      IF lStopOnErr THEN LEAVE FileLoop.
    END.
  END.

  ASSIGN opOK = (compNotOk EQ 0)
         opMsg = STRING(compOK) + "/" + STRING(compNotOk) + "/" + STRING(skipped).

  RUN printErrorsWarningsJson IN hComp (INPUT compOK, INPUT compNotOk).

END PROCEDURE.

PROCEDURE logError.
  DEFINE INPUT PARAMETER ipMsg AS CHARACTER NO-UNDO.

  RUN logError IN hSrcProc (ipMsg).

END PROCEDURE.

PROCEDURE logWarning.
  DEFINE INPUT PARAMETER ipMsg AS CHARACTER NO-UNDO.

  RUN logWarning IN hSrcProc (ipMsg).

END PROCEDURE.

PROCEDURE logInfo.
  DEFINE INPUT PARAMETER ipMsg AS CHARACTER NO-UNDO.

  RUN logInfo IN hSrcProc (ipMsg).

END PROCEDURE.

PROCEDURE logVerbose.
  DEFINE INPUT PARAMETER ipMsg AS CHARACTER NO-UNDO.

  RUN logVerbose IN hSrcProc (ipMsg).

END PROCEDURE.

PROCEDURE logDebug.
  DEFINE INPUT PARAMETER ipMsg AS CHARACTER NO-UNDO.

  RUN logDebug IN hSrcProc (ipMsg).

END PROCEDURE.
