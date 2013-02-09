package org.psics.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.psics.be.E;

public class Version {

	static Properties properties;

	static String versionName;
	static String versionDate;


	public static String getVersionName() {
		if (versionName == null) {
			findVersionName();
		}
		return versionName;
	}

	public static String getVersionDate() {
		if (versionDate == null) {
			findVersionDate();
		}
		return versionDate;
	}



	static void findVersionName() {
		versionName = "unknwon version";
		checkProperties();

		if (properties == null) {
			E.error("cant find properties file for version");
		} else {
			String major = properties.getProperty("version.major");
			String minor = properties.getProperty("version.minor");
			String release = properties.getProperty("version.release");
			versionName = major + "." + minor + "." + release;
		}
	}


	private static void findVersionDate() {
		versionDate = "-";
		checkProperties();
		if (properties != null) {
			versionDate = properties.getProperty("version.date");
		}
	}



	private static void checkProperties() {
		if (properties == null) {
			properties = new Properties();
			try {
			File f = new File("VERSION");
			if (f.exists()) {
				properties.load(new FileInputStream(f));

			} else {
				// E.info("no such file as " + f.getAbsolutePath());
				InputStream ins = (new Version()).getClass().getResourceAsStream("VERSION");
				E.info("got input stream for version " + ins);
				properties.load(ins);
			}

		} catch (Exception ex) {
			E.error("cant read properties " + ex);
			ex.printStackTrace();
		}
		}
	}


	public static void main(String[] argv) {
		E.info("versionName  is " + getVersionName());
	}

}
