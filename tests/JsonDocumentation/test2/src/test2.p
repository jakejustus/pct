/**
 * # Purpose
 * The application factorys main purpose is to manage the life-cycle of
 *
 * @author              Robert Edwards
 * @param ipcMainInput1 Are we at the start of the request.
 *                      Multiline parameter comment... cool
 * @deprecated 1.0.0 Please use something else.
 * @internal
 */

USING Progress.Lang.Class.

DEFINE INPUT PARAMETER ipcMainInput1 AS CHARACTER 	NO-UNDO.

DEFINE TEMP-TABLE tt1 NO-UNDO FIELD fdl1 AS CHARACTER.
DEFINE TEMP-TABLE tt2 NO-UNDO FIELD fdl1 AS CHARACTER.
DEFINE DATASET ds1 FOR tt1, tt2.

/**
 * testProcedure comment
 * This is two paragraphs. _markdown_ is also supported here. Lorem ipsum dolor sit amet,
 * consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
 * Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
 *
 *     /* This is a comment */
 *     DEFINE VARIABLE cResult    AS CHARACTER NO-UNDO.
 *     RUN testProcedure IN TARGET-PROCEDURE (INPUT "test", OUTPUT cResult).
 *
 * @param ipcInput1 salkdjsaklds.
 *                  Multiline parameter comment... cool
 * @param opcOutput1 someOutput.
 *                   Multiline parameter comment... cool
 */
PROCEDURE testProcedure:
	DEFINE INPUT  PARAMETER ipcInput1  AS CHARACTER 	NO-UNDO.
	DEFINE OUTPUT PARAMETER opcOutput1 AS CHARACTER 	NO-UNDO.

END PROCEDURE.

/*
 testProcedure2 comment
 This is two paragraphs. _markdown_ is also supported here. Lorem ipsum dolor sit amet,
 consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
 Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
 Should be ABLDoc format
 
 @param ipcInput1 salkdjsaklds.
                  Multiline parameter comment... cool
 @param opcOutput1 someOutput.
                   Multiline parameter comment... cool
 */
PROCEDURE testProcedure2:
  DEFINE INPUT  PARAMETER ipcInput1  AS CHARACTER NO-UNDO.
  DEFINE OUTPUT PARAMETER opcOutput1 AS CHARACTER NO-UNDO.

END PROCEDURE.

/**
 * testFunction comment
 * This is two paragraphs. _markdown_ is also supported here. Lorem ipsum dolor sit amet,
 * consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
 * Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
 *
 *     /* This is a comment */
 *     DEFINE VARIABLE cResult    AS CHARACTER NO-UNDO.
 *     cResult = testFunction (INPUT "test").
 *
 * @param ipcInput1 Are we at the start of the request.
 *                  Multiline parameter comment... cool
 * @return         Returns something if successful and something else if not
 */
FUNCTION testFunction RETURNS CHARACTER
  (INPUT ipcInput1  AS CHARACTER):

END FUNCTION.

