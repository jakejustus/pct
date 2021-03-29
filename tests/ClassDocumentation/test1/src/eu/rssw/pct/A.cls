using Progress.Lang.*.

interface eu.rssw.pct.A:
  define temp-table tt1 no-undo
    field fld1 as char.
  define dataset ds1 for tt1.
  method public void method1().
  method public void method2(input-output foo as char, input dataset-handle ds1).
end interface.
