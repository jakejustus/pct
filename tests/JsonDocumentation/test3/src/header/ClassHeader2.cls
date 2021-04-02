/**
 * Copyright 2005-2019 Star Wars
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
 
/*------------------------------------------------------------------------
    File        : ClassHeader2
    Purpose     : Purpose of ClassHeader2
    Syntax      : Syntax of ClassHeader2
    Description : Description of ClassHeader2
    Author(s)   : Han Solo
    Created     : Mon Feb 04 18:10:02 CET 2013
    Notes       : 
  ----------------------------------------------------------------------*/

USING Progress.Lang.*.

BLOCK-LEVEL ON ERROR UNDO, THROW.

CLASS header.ClassHeader2: 
    
    /*------------------------------------------------------------------------------
     Purpose: Purpose of the constructor
     Notes: Notes of constructor  
            Notes line 2
    ------------------------------------------------------------------------------*/
        
    CONSTRUCTOR PUBLIC ClassHeader2 (  ):
        SUPER ().
        
    END CONSTRUCTOR.


END CLASS.