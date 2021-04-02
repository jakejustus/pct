 
 /*------------------------------------------------------------------------
    File        : TempTableComment
    Purpose     : 
    Syntax      : 
    Description : 
    Author(s)   : Han Solo
    Created     : Mon Jan 06 19:32:30 CET 2020
    Notes       : 
  ----------------------------------------------------------------------*/

USING Progress.Lang.*.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS other.TempTableComment: 

       
    /*------------------------------------------------------------------------------
     Purpose: Purpose of ttStatic
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE TEMP-TABLE ttStatic NO-UNDO
        FIELD champ1 AS CHARACTER 
        FIELD champ2 AS CHARACTER 
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /*------------------------------------------------------------------------------
     Purpose: Purpose of ttSample1
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE TEMP-TABLE ttSample1 NO-UNDO
        FIELD champ1 AS CHARACTER 
        FIELD champ2 AS CHARACTER 
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /*------------------------------------------------------------------------------
     Purpose: Purpose of ttSample2
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE TEMP-TABLE ttSample2 NO-UNDO
        FIELD champ1 AS CHARACTER 
        FIELD champ2 AS CHARACTER EXTENT 2
        FIELD champ3 AS CHARACTER FORMAT "X(3)"
        FIELD champ4 AS CHARACTER EXTENT 4 FORMAT "X(3)"
        INDEX i1 IS PRIMARY UNIQUE champ1.    
        
    /* Purpose of ttSample3 */
    DEFINE TEMP-TABLE ttSample3 NO-UNDO
        FIELD champ1 AS CHARACTER
        FIELD champ2 AS CHARACTER
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /* Purpose of ttSample4 */
    DEFINE TEMP-TABLE ttSample4 NO-UNDO
        FIELD champ1 AS CHARACTER
        FIELD champ2 AS CHARACTER
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /* Purpose of ttSample5 */
    DEFINE TEMP-TABLE ttSample5 NO-UNDO
        FIELD champ1 AS CHARACTER
        FIELD champ2 AS CHARACTER
        INDEX i1 IS PRIMARY UNIQUE champ1.
    
    
        
END CLASS.