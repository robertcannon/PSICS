
func morphplot(fnm) {
	
   window, 0;
   fma;

   f = open(fnm, "r");
   
   line = rdline(f);
   while (line) {
      plotline, line;
      line = rdline(f);
   }

}


func plotline(line) {

   typ = "";
   lbl = "";
   n = 0;
   rest = "";
   nread = sread(line, format="%s %s %d %[^n]", typ, lbl, n, rest);

   if (typ == "xyzline") {
      dat = array(double, 3, n);
      nread = sread(rest, dat);
      grow, dat, dat(,1);
      plg, dat(2,), dat(1,), marks=0
         
   } else if (typ == "point") {
      dat = array(double, n);
      nread = sread(rest, dat);

      if (strpart(lbl, 1:4) == "comp") {
         //   plmk, dat(2), dat(1), marker=4, msize=0.1, color="yellow";
      } else {
         plmk, dat(2), dat(1), marker=4, msize=0.3, color="red";
      }
      
   } else {
      print, "ignoring unrecognized data: " + typ + " " + dat;
   }
   
}
