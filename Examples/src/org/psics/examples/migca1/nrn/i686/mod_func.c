#include <stdio.h>
#include "hocdec.h"
extern int nrnmpi_myid;
extern int nrn_nobanner_;
modl_reg(){
  if (!nrn_nobanner_) if (nrnmpi_myid < 1) {
    fprintf(stderr, "Additional mechanisms from files\n");

    fprintf(stderr," kadist.mod");
    fprintf(stderr," kaprox.mod");
    fprintf(stderr," kdrca1.mod");
    fprintf(stderr," na3.mod");
    fprintf(stderr," nax.mod");
    fprintf(stderr, "\n");
  }
  _kadist_reg();
  _kaprox_reg();
  _kdrca1_reg();
  _na3_reg();
  _nax_reg();
}
