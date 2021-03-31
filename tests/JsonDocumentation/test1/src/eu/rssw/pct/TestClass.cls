/**********************************************************************
 * Copyright (C) 2006-2013 by Consultingwerk Ltd. ("CW") -            *
 * www.consultingwerk.de and other contributors as listed             *
 * below.  All Rights Reserved.                                       *
 *                                                                    *
 *  Software is distributed on an "AS IS", WITHOUT WARRANTY OF ANY    *
 *   KIND, either express or implied.                                 *
 *                                                                    *
 *  Contributors:                                                     *
 *                                                                    *
 **********************************************************************/
/*------------------------------------------------------------------------
    File        : TestClass
    Purpose     : 
    Syntax      : 
    Description : 
    Author(s)   : Mike Fechner / Consultingwerk Ltd.
    Created     : Sat Jun 08 12:43:13 CEST 2013
    Notes       : 
  ----------------------------------------------------------------------*/

ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*       FROM PROPATH .

CLASS eu.rssw.pct.TestClass FINAL: 

    /*------------------------------------------------------------------------------
        Purpose: Constructor for the TestClass class
        Notes:   
    ------------------------------------------------------------------------------*/
	CONSTRUCTOR PUBLIC TestClass ():
		SUPER ().
		
	END CONSTRUCTOR.

    /*------------------------------------------------------------------------------
        Purpose: Constructor for the TestClass class
        Notes:   
        @param pcParam The comment for pcParam
    ------------------------------------------------------------------------------*/
    CONSTRUCTOR PUBLIC TestClass (pcParam AS CHARACTER):
        SUPER ().
        
    END CONSTRUCTOR.

    /*------------------------------------------------------------------------------
        Purpose: The is a test method with no annotation
        Notes:   
        @param pcParam The comment for pcParam
    ------------------------------------------------------------------------------*/
	METHOD PUBLIC FINAL VOID TestMethodNoAnnotation (pcParam AS CHARACTER):
		
		RETURN.

	END METHOD .

    /*------------------------------------------------------------------------------
        Purpose: The is a test method with an @Test. annotation
        Notes:   
        @param pcParam The comment for pcParam
    ------------------------------------------------------------------------------*/
    @Test.
    METHOD PUBLIC VOID TestMethodWithAnnotation (pcParam AS CHARACTER):
        
        RETURN.

    END METHOD .

END CLASS.
