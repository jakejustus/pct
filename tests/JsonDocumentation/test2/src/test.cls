/**
 * Test Class file
 * ===============================
 * This is to test the markdown support
 * 
 * Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
 * Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in 
 * reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in 
 * culpa qui officia deserunt mollit anim id est laborum.
 *
 *     DEFINE VARIABLE oTest AS test NO-UNDO.
 *     oTest = NEW test().
 *     MESSAGE oTest:BaseKey.
 *
 *
 * @author     Robert Edwards  
 */
CLASS test INHERITS base.class:
  
  DEFINE PUBLIC EVENT someEvent SIGNATURE VOID (sender AS Progress.Lang.Object, e AS CHARACTER).

  CONSTRUCTOR PRIVATE test ():
    
  END CONSTRUCTOR.

END CLASS.

