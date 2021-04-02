/**
 * Base Class file
 * ===============================
 * This is to test the markdown support
 * 
 * > This is a blockquote with two _paragraphs_. Lorem ipsum dolor sit amet,
 * > consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
 * > Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
 * > 
 * > Donec sit amet nisl. Aliquam semper ipsum sit amet velit. Suspendisse
 * > id sem consectetuer libero luctus adipiscing.
 * 
 * * these are list items
 * * these are list items
 *
 * @author     Robert Edwards
 * @deprecated 0.0.1  Use {@link test Test} class instead.   
 */
CLASS base.class:

        /**
  	 * Base Key property to store the key
  	 * This is two paragraphs. _markdown_ is also supported here. Lorem ipsum dolor sit amet,
  	 * consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
  	 * Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
  	 *
  	 *     /* This is a comment */
  	 *     MESSAGE mipEnv:EnvironmentKey.
  	 * 
  	 * @internal
  	 * @return               Returns true if key is **successfully** registered
  	 */
  	DEFINE PUBLIC    PROPERTY BaseKey            AS LOGICAL    NO-UNDO GET. PRIVATE SET.

    /**
     * Configure this environment
     * This is a blockquote with two paragraphs. Lorem ipsum dolor sit amet,
     * consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
     * Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
     *
     * @param iplStart Are we at the start of the request.
     *                 Multiline parameter comment... cool
     * @param test     This is a test parameter comment
     * @return         Returns true if successful and false if not
     */
    METHOD PUBLIC LOGICAL configureEnvironment
      ( INPUT iplStart AS LOGICAL,
        INPUT ipcTest  AS CHARACTER ):

    END.

    /**
  	 * Register an application _Service_ {@link test}
  	 * This is a blockquote with two paragraphs. Lorem ipsum dolor sit amet,
  	 * consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
  	 * Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
  	 *     /* This is a comment */
  	 *     IF VALID-OBJECT(oEnv)
  	 *     THEN
  	 *         oEnv:registerApplicationService().
  	 * 
  	 * @internal
  	 * @deprecated 0.0.1     Use {@link base.class-method-configureEnvironment configureEnvironment} instead.
  	 * @param ipcServiceCode Service code for this application service
  	 *                       Multiline *parameter* comment... cool
  	 * @param ipoObject      The instance object of the service to register
  	 * @return               Returns true if application is **successfully** registered
  	 */
  	METHOD PROTECTED LOGICAL registerApplicationService
  	  (INPUT ipcServiceCode AS CHARACTER,
  	   INPUT ipoObject      AS Progress.Lang.Object):

  	END.

END CLASS.

