package org.psics.icing;

import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.gui.base.DruBorderPanel;
import org.catacomb.druid.gui.edit.DruHTMLPanel;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.Controller;
import org.psics.be.E;
import org.psics.be.OrientationSource;
import org.psics.begui.Visualizer;
import org.psics.distrib.PointPopulation;
import org.psics.util.JUtil;



public class View3DController implements Controller {


	@IOPoint(xid="3dPanel")
	public DruBorderPanel view3Panel;

	@IOPoint(xid="hmsg")
	public DruHTMLPanel hmsgPanel;

	Visualizer vis3D;

	OrientationSource osource;


	boolean showColors;

	double scaleFactor;

	public void attached() {
	}


	public void lightsBrighter() {
		if (vis3D != null) {
			vis3D.deltaLights(0.1);
		}
	}

	public void lightsDarker() {
		if (vis3D != null) {
			vis3D.deltaLights(-0.1);
		}
	}


	public void setLights(int perc) {
		vis3D.setLightsPercent(perc);
	}


	public void setAA(boolean b) {
		if (vis3D != null) {
			vis3D.setAA(b);
		}
	}

	public void setResolution(String s) {
		if (vis3D != null) {
		if (s == null || s.equals("none")) {

		} else if (s.equals("l")) {
			vis3D.setResolution(Visualizer.LOW);

		} else if (s.equals("m")) {
			vis3D.setResolution(Visualizer.MEDIUM);

		} else if (s.equals("h")) {
			vis3D.setResolution(Visualizer.HIGH);
		} else {
			E.error("cant recognize " + s);
		}
		}
	}



	public void load(double fac3D) {
		scaleFactor = fac3D;
		if (vis3D == null) {
			try {
			   Object obj = JUtil.newInstance("org.psics.icing3d.Icing3DViewer");
               vis3D = (Visualizer)obj;
               vis3D.setScaleFactor(scaleFactor);

			} catch (Exception ex) {
				E.error("cant instantiate 3d viewer " + ex);
			}


			if (vis3D != null) {
				view3Panel.removePanel(hmsgPanel);
				view3Panel.addRaw(vis3D.getPanel());

				checkMem();
			}
		}
	}


	private void checkMem() {
		int maxmem = (int)(Runtime.getRuntime().maxMemory() / (1024. * 1024.));
		if (maxmem < 256) {
			String txt = "Your java session has access to " + maxmem + "MB. This  " +
			"may be too little for 3D rendring to work smoothly. You can allow it to use more  " +
			"memory by starting from the command line with a command like:-b-" +
			"java -Xmx512M -jar icing-xxx.jar";
			Dialoguer.message(txt);
		}
	}

	@SuppressWarnings("unused")
	public void sync3DPops(PointPopulation[] pops, double sfac) {
		if (vis3D == null) {
			return;
		} else {
			vis3D.refreshDecoration(pops);
		}
	}


	@SuppressWarnings("unused")
	public void syncSceneGraph(IcingPoint[] points, double sfac) {
		if (vis3D != null && points != null) {
			vis3D.buildViewable(points);
		}
	}


	public void syncOrientation() {
		vis3D.setFourMatrix(osource.getFourMatrixOrientation());
	}


	public void setOrientationSource(OrientationSource os) {
		osource = os;
	}


	public double[] getFourMatrix() {
		return vis3D.getFourMatrix();
	}


	public void setScaleFactor(double fac3D) {
		if (vis3D != null) {
			vis3D.setScaleFactor(fac3D);
		}
	}




}


