package org.psics.model.control;

import org.psics.quantity.annotation.FolderPath;
import org.psics.quantity.annotation.LibraryPath;
import org.psics.quantity.annotation.ModelType;


@ModelType(info = "By default, PSICS looks in the current folder for components referenced from the mode." +
		"ModelFolder specify additional serach paths where other components " +
		"can be found. The path should be expressed with forward slashes for separators, and can be either absolute, " +
		"such as '/home/PSICS/models/CaChannels',  or relative to the main model file, " +
		"such as '../models/CaChannels' where '..' stands for the " +
		"parent folder of the current directory.",
		standalone = false, tag = "A folder to include in the search path for model components",
		usedWithin = { PSICSRun.class })
public class ModelFolder {

	@LibraryPath(tag="Search path", required=true, fallback=".")
	public String path;


}
