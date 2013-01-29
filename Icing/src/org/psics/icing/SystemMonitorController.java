

package org.psics.icing;



import org.catacomb.druid.gui.edit.DruFillGuage;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.*;
import org.psics.be.E;




public class SystemMonitorController  implements Controller, Runnable {


   @IOPoint(xid="MemoryGuage")
   public DruFillGuage memoryGuage;


   DruFillGuage diskGuage;



   boolean shouldContinue = true;




   public SystemMonitorController() {
      super();

      startUpdating();
   }



   public void attached() {
      showState();
      startUpdating();
   }


   public void collectGarbage() {
      System.gc();
      showState();
   }


   public void configureDisk() {
      E.missing();
   }


   public void stopUpdating() {
      System.out.println("WARNING - missing code in SystemMonitorController");
      shouldContinue = false;
   }


   // NB - sloppy stop then start will crate multiuple threads POSERR (

   public void startUpdating() {
      shouldContinue = true;
      Thread runThread = new Thread(this);
      runThread.start();

   }



   public void run() {
      while (shouldContinue) {
	 try {
	    Thread.sleep(2000);

	 } catch (Exception ex) {

	 }
	 showState();
      }
   }







   private void showState() {
      if (memoryGuage != null) {

	 double fmb = 1024. * 1024;

	 Runtime runtime = Runtime.getRuntime();

	 long totmem = runtime.totalMemory();
	 long maxmem = runtime.maxMemory();
	 long freemem = runtime.freeMemory();

	 long usedmem = totmem - freemem;

	 double fu = usedmem / fmb;
	 double ft = maxmem / fmb;

	 String smem = "mem: " + ((int)(fu + 0.5)) + " / " + ((int)(ft + 0.5)) + " Mb";

	 double ffu = fu / ft;

	 memoryGuage.showValue(ffu, smem);


//	 diskGuage.showValue(0.0, "disk: none");
      }
   }


}
