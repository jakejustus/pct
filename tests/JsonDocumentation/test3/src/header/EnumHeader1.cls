
/*------------------------------------------------------------------------
    File        : EnumHeader1
    Purpose     : Purpose of EnumHeader1
    Syntax      : Syntax of EnumHeader1
    Description : Description of EnumHeader1
    Author(s)   : Han Solo
    Created     : Tue Dec 31 17:46:24 CET 2019
    Notes       : 
  ----------------------------------------------------------------------*/

ENUM header.EnumHeader1 FLAGS :  

    /* 
    Enum List (not parse)
    */
    DEFINE ENUM /* Comment HanSolo */
    			HanSolo = 1 
    			// Comment LukeSkywalker (not parse)
                AnakinSkywalker = 2 
                /* Comment Padme */
                Padme 
                /* Comment LukeSkywalker*/
                LukeSkywalker = Padme, AnakinSkywalker 
                .

END ENUM.