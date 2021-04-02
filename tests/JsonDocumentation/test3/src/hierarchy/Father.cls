 
 /*------------------------------------------------------------------------
    File        : Father
    Purpose     : THE FATHER
    Syntax      : 
    Description : 
    Author(s)   : Han Solo
    Created     : Mon Dec 02 16:33:53 CET 2019
    Notes       : 
  ----------------------------------------------------------------------*/

USING Progress.Lang.*.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS hierarchy.Father: 

    /*------------------------------------------------------------------------------
     Purpose: Say Hello from Father
     Notes:
         @param pName Name to say hello from father
    ------------------------------------------------------------------------------*/

    METHOD PUBLIC VOID HelloWorld( INPUT pName AS CHARACTER  ):
        
        RETURN.

    END METHOD.
 
END CLASS.