 
 /*------------------------------------------------------------------------
    File        : PropertyComment
    Purpose     : Purpose of PropertyComment
    Syntax      : 
    Description : Description of PropertyComment
    Author(s)   : Han Solo
    Created     : Tue Dec 31 17:51:08 CET 2019
    Notes       : 
  ----------------------------------------------------------------------*/

USING Progress.Lang.*.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS other.PropertyComment: 
    
    
    /* A simple comment for property1 */
    DEFINE PUBLIC PROPERTY property1 AS CHARACTER NO-UNDO 
    GET.
    SET. 
    
    /* 
    A simple comment for property2 
    */
    DEFINE PUBLIC PROPERTY property2 AS CHARACTER NO-UNDO 
    GET.
    SET. 
    
    /*------------------------------------------------------------------------------
     Purpose: Purpose of property3
     Notes: Note of property3
    ------------------------------------------------------------------------------*/
    DEFINE PUBLIC PROPERTY property3 AS CHARACTER NO-UNDO 
    GET.
    SET. 
    
    /**
     * ## Purpose
     * The purpose of the property4
     */
    DEFINE PUBLIC PROPERTY property4 AS CHARACTER NO-UNDO 
    GET.
    SET. 

    /**
     * ## Purpose
     * The purpose of the property5
     * 
     * 
     * @author Han Solo
     * @internal
     * @deprecated 0.0.1 This is the deprecated message.
     */
    DEFINE PUBLIC PROPERTY property5 AS CHARACTER NO-UNDO 
    GET.
    SET. 
    
    /* 
    A simple comment for property6 
    @author Han Solo
    @internal
    @deprecated 0.0.1 This is the deprecated message.
    */
    DEFINE PUBLIC PROPERTY property6 AS CHARACTER NO-UNDO 
    GET.
    SET. 
    
    /*------------------------------------------------------------------------------
     Purpose: Purpose of property7
     Notes: Note of property7
     @author Han Solo
     @internal
     @deprecated 0.0.1 This is the deprecated message.
    ------------------------------------------------------------------------------*/
    DEFINE PUBLIC PROPERTY property7 AS CHARACTER NO-UNDO 
    GET.
    SET. 
    
    
    DEFINE PUBLIC PROPERTY property8 AS CHARACTER NO-UNDO 
    GET.
    PROTECTED SET.
    
    
    DEFINE PUBLIC PROPERTY property9 AS CHARACTER NO-UNDO 
    GET.
    
    /* Simple comment for property10 */
    DEFINE PUBLIC PROPERTY property10 AS CHARACTER NO-UNDO 
    PROTECTED GET.
    SET.
    
    /* Simple comment for property11 */
    DEFINE PUBLIC PROPERTY property11 AS CHARACTER NO-UNDO 
    SET.
    
    /**
     * ## Purpose
     * The purpose of the property12
     */
    DEFINE PUBLIC PROPERTY property12 AS CHARACTER NO-UNDO 
    PRIVATE GET.
    SET.


END CLASS.