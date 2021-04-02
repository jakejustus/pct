 
 /*------------------------------------------------------------------------
    File        : StaticCommentClass
    Purpose     : Purpose of StaticCommentClass
    Syntax      : 
    Description : Description of StaticCommentClass
    Author(s)   : Han Solo
    Created     : Tue Dec 31 18:03:51 CET 2019
    Notes       : 
  ----------------------------------------------------------------------*/

USING Progress.Lang.*.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS other.StaticCommentClass: 

	/*------------------------------------------------------------------------------
     Purpose: Purpose of the constructor
     Notes: Notes of constructor  
            Notes line 2
    ------------------------------------------------------------------------------*/
        
    CONSTRUCTOR STATIC StaticCommentClass (  ):
        
    END CONSTRUCTOR.
    
    /*------------------------------------------------------------------------------
     Purpose: Purpose of ttPrivStatic
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE PRIVATE STATIC TEMP-TABLE ttPrivStatic NO-UNDO
        FIELD champ1 AS CHARACTER 
        FIELD champ2 AS CHARACTER
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /*------------------------------------------------------------------------------
     Purpose: Purpose of ttProtecStatic
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE PROTECTED STATIC TEMP-TABLE ttProtecStatic NO-UNDO
        FIELD champ1 AS CHARACTER 
        FIELD champ2 AS CHARACTER
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /*------------------------------------------------------------------------------
     Purpose: Purpose of ttStatic
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE STATIC TEMP-TABLE ttStatic NO-UNDO
        FIELD champ1 AS CHARACTER 
        FIELD champ2 AS CHARACTER
        INDEX i1 IS PRIMARY UNIQUE champ1.
        
    /*------------------------------------------------------------------------------
     Purpose: Purpose of Static Event1
     Notes:
    ------------------------------------------------------------------------------*/
    DEFINE PUBLIC STATIC EVENT Event1 
      SIGNATURE VOID (pParam AS CHARACTER).
    
    /*------------------------------------------------------------------------------
     Purpose: Purpose of Static Property1
     Notes: 
    ------------------------------------------------------------------------------*/
    DEFINE PUBLIC STATIC PROPERTY Property1 AS CHARACTER NO-UNDO 
        GET.
        SET. 

    /*------------------------------------------------------------------------------
     Purpose: Purpose of Static Method1
     Notes:
    ------------------------------------------------------------------------------*/

    METHOD PUBLIC STATIC VOID Method1(  ):
        
        RETURN.

    END METHOD.        
    

END CLASS.