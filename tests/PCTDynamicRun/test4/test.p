define output parameter xx1 as char no-undo.

define shared variable pctVerbose as logical no-undo.

if pctVerbose then
    message 'Running Test4 test.p'
        view-as alert-box.
/*
if true then
    /* THROW an Error */
    undo, throw new Progress.Lang.AppError ('Bad Exception').
*/
/* Raise an unhandled exception */
find _user.

xx1 = substitute('&1 &2', dynamic-function("getParameter" in source-procedure, "prm1"), dynamic-function("getParameter" in source-procedure, "prm2")).

return "0".
