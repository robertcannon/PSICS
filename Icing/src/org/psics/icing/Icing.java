package org.psics.icing;

import org.catacomb.druid.build.Druid;
import org.catacomb.druid.load.DruidAppBase;
import org.catacomb.icon.splash.Splasher;
import org.catacomb.interlish.reflect.ReflectionConstructor;
import org.psics.IcingRoot;


public class Icing {

	   public static void main(String[] argv) {
		  // root of main XML gui spec
	      String configPath = "org.psics.icing.ICING";
	      ReflectionConstructor.addPath("org.catacomb.druid.manifest");

	      // show the splash screen if there is one;
	      Splasher.showSplash(configPath);

	      DruidAppBase.init("Icing-PSICS", new IcingRoot());

	      // most of the work is done by the druid
	      Druid druid = new Druid(configPath);

	      druid.whizzBang();

	   }
}
