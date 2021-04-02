 
/*------------------------------------------------------------------------
    File        : ClassHeader3
    Purpose     : Purpose of ClassHeader3
    Syntax      : Syntax of ClassHeader3
    Description : Description of ClassHeader3
    Author(s)   : Han Solo
    Created     : Mon Feb 04 18:10:02 CET 2013
    Notes       : 
  ----------------------------------------------------------------------*/

/* Some comment useless */

USING Progress.Lang.*.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS header.ClassHeader3: 
    
    /*------------------------------------------------------------------------------
     Purpose: Purpose of the constructor
     Notes: Notes of constructor  
            Notes line 2
    ------------------------------------------------------------------------------*/
        
    CONSTRUCTOR PUBLIC ClassHeader3 (  ):
        SUPER ().
        
    END CONSTRUCTOR.


END CLASS.