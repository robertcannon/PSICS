package org.psics.icing;

import org.catacomb.druid.dialog.DialogController;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.content.ColorValue;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;



public class ColorsDialogController extends DialogController implements ValueWatcher {


	 @Editable(xid="bgColor")
	public ColorValue bgCV = new ColorValue("0x404040");

@Editable(xid="fgColor")
		public ColorValue fgCV = new ColorValue("0x606060");


@Editable(xid="gridColor")
public ColorValue gridCV = new ColorValue("0x202020");

@Editable(xid="axisColor")
public ColorValue axisCV = new ColorValue("0x202020");



	MorphologyController morphController;



   public ColorsDialogController() {
      super();

   }

   public void attached() {
	   bgCV.addValueWatcher(this);
	   fgCV.addValueWatcher(this);
	   axisCV.addValueWatcher(this);
	   gridCV.addValueWatcher(this);
   }


   public void setMorphController(MorphologyController mc) {
	   morphController = mc;
   }



   public void valueChangedBy(Value pv, Object src) {
	   if (morphController != null) {
		   morphController.setColors(bgCV.getIntColor(), gridCV.getIntColor(),
				   axisCV.getIntColor(), fgCV.getIntColor());
	   }
   }

   public void resetColors() {
	   bgCV.reportableSetColor("0x404040", this);
	   fgCV.reportableSetColor("0xf0f0f0", this);
	   axisCV.reportableSetColor("0x202020", this);
	   gridCV.reportableSetColor("0x202020", this);
   }


   public void setShowGrid(boolean b) {
	   morphController.setShowGrid(b);
   }


}
