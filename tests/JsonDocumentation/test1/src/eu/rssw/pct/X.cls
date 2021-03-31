using Progress.Lang.*.

{ eu/rssw/pct/inc.i }

/* Class comment
 * <a href="test">Some text in an anchor</a>*/
 
class eu.rssw.pct.X serializable use-widget-pool:
  /* Public property foo1 */
  define public property foo1 as char get. set.
  /* Public property foo2 */
  define protected property foo2 as char get. set.
  define private property foo3 as class eu.rssw.pct.X get. set.
  define property foo4 as char get.
  define public static property foo5 as char get.

  define temp-table tt1 no-undo
    field fld1 as char
    field fld2 as int
    field fld3 as dec
    field fld4 as logical
    index idx1 is primary unique fld1 fld2.
  define temp-table tt3 no-undo like tt1
    field fld5 like tt1.fld1.

{ eu/rssw/pct/inc2.i }
  define dataset ds1 for tt1.
  define dataset ds2 for tt2.
  define dataset ds3 for tt1, tt2.
{ eu/rssw/pct/inc3.i }

  define public event isSomething signature void (piCustNum as integer). 
  
  define public event MyEvent void (sender AS Progress.Lang.Object, e AS System.EventArgs).

  constructor public X () :

  end  constructor.

  constructor public X( foo as int, bar as char ):
  
  end constructor.
  
  
  method public void method1():
  
  end method.
  
  /* 
   * Comment for method 2
   * @return void
   */
  method public void method4() :
    /* My second comment */
  end method.
  
end class.
