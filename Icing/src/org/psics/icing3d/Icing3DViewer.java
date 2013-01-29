package org.psics.icing3d;

import java.awt.Color;
import java.util.HashSet;

import javax.swing.JPanel;

import org.psics.be.E;
import org.psics.begui.Visualizer;
import org.psics.icing.IcingPoint;

import org.psics.distrib.PointPopulation;



public class Icing3DViewer implements Visualizer {

	double scaleFactor = 1.;


	SceneGraphViewer sceneGraphViewer;


	int resolution = Visualizer.MEDIUM;

	IcingPoint[] cachedPoints;
	PointPopulation[] cachedPops;


	public Icing3DViewer() {
		sceneGraphViewer = new SceneGraphViewer();
	}



	public JPanel getPanel() {
		return sceneGraphViewer.getPanel();
	}

	public void setScaleFactor(double d) {
		scaleFactor = d;
	}


	public void buildViewable(Object obj) {
		RunBuilder rb = new RunBuilder(this, obj);
		Thread thread = new Thread(rb);
		thread.start();
	}

	public void reallyBuildVewable(Object obj) {
		if (obj instanceof IcingPoint[]) {
			IcingPoint[] points = (IcingPoint[])obj;
			cachedPoints = points;
			SceneGraphBuilder sgb  = new SceneGraphBuilder();
			sgb.buildTree(points, resolution, scaleFactor);
			sceneGraphViewer.removeAllDecoration();
			sceneGraphViewer.setSceneGraph(sgb.getSceneGraph());

		} else {
			E.error("cant build viewable from " + obj);
		}
	}

	public void refreshDecoration(Object obj) {
		RunDecorator rb = new RunDecorator(this, obj);
		Thread thread = new Thread(rb);
		thread.start();
	}


	public void reallyRefreshDecoration(Object obj) {
		if (obj instanceof PointPopulation[]) {
			PointPopulation[] pops = (PointPopulation[])obj;
			cachedPops = pops;
			HashSet<String> pHS = new HashSet<String>();
			for (PointPopulation p : pops) {
				pHS.add(p.getID());
			}
			sceneGraphViewer.removeUnlistedDecoration(pHS);

			for (PointPopulation p : pops) {
				if (p.isSynced3D()) {
					sceneGraphViewer.setDecorationVisibility(p.getID(), p.shouldShow());

				} else if (p.hasPositions()) {
					Color pcol = makeColor(p.getBestColor());

					ChannelGraphBuilder cgb = new ChannelGraphBuilder(p.getCHPos(), pcol);
					p.setSynced3();
					cgb.build(resolution, scaleFactor);
					sceneGraphViewer.setDecoration(p.getID(), cgb.getRoot(), p.shouldShow());
				} else {
					// just leave it for now? - will get called again... TODO
				}
			}



		} else {
			E.error("cant refresh from " + obj);
		}
	}



	public void deltaLights(double d) {
		 sceneGraphViewer.deltaLights(d);

	}



	public void setAA(boolean b) {
		sceneGraphViewer.setAA(b);

	}



	public void setResolution(int res) {
		if (res != resolution) {
			 resolution = res;
			 if (cachedPoints != null) {
				 SceneGraphBuilder sgb  = new SceneGraphBuilder();
				sgb.buildTree(cachedPoints, resolution, scaleFactor);
				sceneGraphViewer.setSceneGraph(sgb.getSceneGraph());
			 }

			 if (cachedPops != null) {
				 for (PointPopulation p : cachedPops) {
					 p.setUnsynced3();
				 }
				 refreshDecoration(cachedPops);
			 }
		}
	}

	public Color makeColor(Object obj) {
		Color ret = null;
		if (obj instanceof Color) {
			ret = (Color)obj;
		} else if (obj instanceof String) {
			String s = (String)obj;
			if (s == null) {
				s = "0xff0000";
			}
			if (!s.toLowerCase().startsWith("0x")) {
				s = "0x" + s;
			}
			try {
				ret = new Color(Integer.decode(s).intValue());
			} catch (Exception ex) {
				E.warning("dodgy color " + s);
				ret = Color.red;
			}
		} else {
			ret = Color.cyan;
		}
		return ret;
	}



	public void setLightsPercent(int p) {
		sceneGraphViewer.setLightsPercent(p);
	}



	public void setFourMatrix(double[] fmo) {
		sceneGraphViewer.setFourMatrix(fmo);
	}



	public double[] getFourMatrix() {
		return sceneGraphViewer.getFourMatrix();
	}

}
