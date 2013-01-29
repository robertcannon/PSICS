package org.psics.icing;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.catacomb.datalish.Box;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.gui.base.DruDialog;
import org.catacomb.druid.gui.base.DruDrawingCanvas;
import org.catacomb.druid.gui.base.DruLabelPanel;
import org.catacomb.druid.gui.edit.DruChoice;
import org.catacomb.druid.gui.edit.DruListPanel;
import org.catacomb.graph.gui.BuildPaintInstructor;
import org.catacomb.graph.gui.Builder;
import org.catacomb.graph.gui.Painter;
import org.catacomb.graph.gui.PickListener;
import org.catacomb.graph.gui.Pickable;
import org.catacomb.graph.gui.RotationListener;
import org.catacomb.graph.gui.WorldCanvas;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.BooleanValue;
import org.catacomb.interlish.content.ColorValue;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.interlish.structure.Targetable;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;
import org.psics.be.E;
import org.psics.be.OrientationSource;
import org.psics.distrib.PointPopulation;
import org.psics.distrib.PointTree;
import org.psics.distrib.DistribPopulation;
import org.psics.geom.Position;
import org.psics.geom.Projector;
import org.psics.model.morph.CellMorphology;
import org.psics.morph.MergeDiscretizer;
import org.psics.morph.TreePoint;
import org.psics.num.Compartment;
import org.psics.num.CompartmentTree;
import org.psics.num.math.MersenneTwister;
import org.psics.project.StandaloneItem;
import org.psics.project.StandaloneSWCItem;


public class MorphologyController implements Controller, Targetable, BuildPaintInstructor,
	PickListener, RotationListener, ValueWatcher, OrientationSource {


	@IOPoint(xid = "canvas")
	public DruDrawingCanvas drawingCanvas;

	@Editable(xid = "labelText")
	public StringValue dummyLabel = new StringValue();

	@IOPoint(xid="discL")
	public DruLabelPanel discLabel;

	@IOPoint(xid = "labels")
	public DruListPanel listPanel;

	@Editable(xid="labelText")
	public StringValue labelSV = new StringValue();

	@IOPoint(xid = "partofs")
	public DruListPanel partOfPanel;


	@Editable(xid="partColor")
	public ColorValue partCV = new ColorValue();

	@Editable(xid="partName")
	public StringValue partSV = new StringValue();

	@Editable(xid="showColors")
	public BooleanValue colorBV = new BooleanValue(true);



	@IOPoint(xid = "styleChoice")
	public DruChoice styleChoice;


	@IOPoint(xid = "nchLabel")
	public DruLabelPanel nchLabel;

	@IOPoint(xid = "ncompLabel")
	public DruLabelPanel ncompLabel;

	@IOPoint(xid = "activePtLabel")
	public DruLabelPanel activeLabel;

	@IOPoint(xid = "activePtPosition")
	public DruLabelPanel activePos;


	@IOPoint(xid = "swcStyle")
	public DruChoice swcStyleChoice;


	@IOPoint(xid="3dDialog")
	public DruDialog view3DD;

	@IOPoint(xid="View3D")
	public Druid view3DDruid;

	View3DController view3DController;


	StandaloneItem targetItem;
	CellMorphology morph;

	TreePoint[] treePoints;
	IcingPoint[] points;


	IcingPoint preActive;
	IcingPoint active;



	ArrayList<IcingLabel> pointLabels = new ArrayList<IcingLabel>();

	IcingLabel dragLabel;
	IcingLabel editLabel;

	ArrayList<String> partOfs = new ArrayList<String>();

	String ssNew = "20";
	String ssCache = null;
	CompartmentTree compartmentTree;
	PointTree channelTree;

	Projector cachedProj;
	Painter cachedPainter; // TODO shouldn't do this

	public final static int FILLED = 1;
	public final static int SEGMENTS = 2;
	public final static int SKELETON = 3;
	public final static int NODES = 4;
	public final static int NONE = 5;
	int style = FILLED;


	public final static int TAPERED = 1;
	public final static int AUTO = 2;
	public final static int UNIFORM = 3;
	int swcStyle = AUTO;
	boolean swcSource = true;

	public boolean showDiscretization = false;


	HashMap<String, String> colorHM = new HashMap<String, String>();
	PartMap[] pam;
	PartMap activePAM;


	private ArrayList<DistribPopulation> populations = new ArrayList<DistribPopulation>();


	PointPopulation[] pops = new PointPopulation[0];

	MersenneTwister mersenne;

	IcingController rootController;


	Color fgColor = new Color(90, 90, 90);




	final static int[] markWidths = { 0, 1, 2, 2, 2, 3, 3, 3, 3, 3 };
	final static int[] markHeights = { 0, 1, 1, 1, 2, 2, 2, 3, 3, 3 };

	boolean draggingLabel = false;

	int[] xptcache = null;
	int[] yptcache = null;

	int bufCanvasWidth = 200;

	boolean showChannels = true;
	boolean opaqueCore = false;

	double fac3D = 0.001;

	MorphBuilder morphBuilder;



	public MorphologyController() {
		mersenne = new MersenneTwister();
	}


	public void setAntialias(boolean b) {
		drawingCanvas.setAntialias(b);
	}

	public void setOpaque(boolean b) {
		opaqueCore = b;
	}


	public void setRootController(IcingController ic) {
		rootController = ic;
	}



	public void show3D() {
		view3DD.open();
		view3DController.load(fac3D);

		syncSceneGraph();
		sync3DPops();
	}


	public void sync3DPops() {
		view3DController.sync3DPops(pops, fac3D);

	}


	private void syncSceneGraph() {
		boolean bcols = colorBV.getBoolean();
		int ips = IcingPoint.AUTO;
		if (swcSource && swcStyle == TAPERED) {
			ips = IcingPoint.TAPERED;
		} else if (swcSource && swcStyle == UNIFORM) {
			ips = IcingPoint.UNIFORM;
		}

		for (IcingPoint p : points) {
			p.setColored3d(bcols);
		    p.setSectionStyle(ips);
		}
		view3DController.syncSceneGraph(points, fac3D);
	}




	public void setMode(String s) {
		String dmode = "";
		if (s.equals("Pan")) {
			dmode = WorldCanvas.PAN;
		} else if (s.equals("Zoom")) {
			dmode = WorldCanvas.ZOOM;

		} else if (s.equals("Roll")) {
			dmode = WorldCanvas.MULTI;

		} else if (s.equals("Turn")) {
			dmode = WorldCanvas.TURN;

		} else if (s.equals("Combined")) {
			dmode = WorldCanvas.MULTI;

		} else {
			E.missing(s);
		}
		drawingCanvas.setMouseMode(dmode);
	}


	public void setStyle(String s) {
		setOpaque(false);
		if (s.equals("Nodes")) {
			style = NODES;
		} else if (s.equals("Skeleton")) {
			style = SKELETON;
		} else if (s.equals("Filled")) {
			style = FILLED;

		} else if (s.equals("Filled opaque")) {
			style = FILLED;
			setOpaque(true);

		} else if (s.equals("Segments")) {
			style = SEGMENTS;
		} else if (s.equals("None")) {
			style = NONE;

		} else {
			E.error(s);
			style = NODES;
		}
		repaint();
	}


	public void setShowChannels(boolean b) {
		showChannels = b;
		repaint();
	}


	public void setSWCStyle(String s) {
		if (s.equals("tapered")) {
			swcStyle = TAPERED;
		} else if (s.equals("uniform")) {
			swcStyle = UNIFORM;
		} else if (s.equals("auto")) {
			swcStyle = AUTO;
		} else {
			E.error("unrecognized: " + s);
		}
		repaint();
		if (swcSource) {
			syncSceneGraph();
		}
	}


	public void setSWCSource(boolean b) {
		if (b) {
			swcSource = true;
			swcStyleChoice.able(true);
		} else {
			swcSource = false;
			swcStyleChoice.able(false);
		}
	}



	public void setPopulations(ArrayList<DistribPopulation> pa) {
		populations.clear();
		populations.addAll(pa);
		repopulate();
	}


	public void setShow(DistribPopulation cp, boolean b) {
		boolean bdone = false;
		for (PointPopulation chp : pops) {
			if (chp.matchesPopulation(cp)) {
				chp.setShow(b);
				bdone = true;
			}
		}
		if (!bdone) {
			E.error("no match for " + cp);
		}
		showNCH();
		sync3DPops();
	}


	public void popListChange() {
		if (pops != null && pops.length > 0) {
			repaint();
		}
		showNCH();
	}


	private void showNCH() {
		int nch = 0;
		for (PointPopulation chp : pops) {
			if (chp.shouldShow()) {
				nch += chp.getNChannel();
			}
		}
		nchLabel.setText("" + nch + " channels");
	}


	@SuppressWarnings("unused")
	public void populationColorChanged(DistribPopulation p) {
		if (pops != null && pops.length > 0) {
			repaint();
		}
		sync3DPops();
	}


	public void populationChanged(DistribPopulation p) {
		p.flagChange();

		repopulate();
		repaint();

		showNCH();
	}



	private boolean needsScaleRemake() {
		boolean bret = false;
		if (compartmentTree == null || ssNew != ssCache || channelTree == null) {
			bret = true;
		}
		return bret;
	}


	public void changeDiscretization() {
		int[] xy = styleChoice.getXYLocationOnScreen();
		xy[0] += 200;
		String s = Dialoguer.getNewName(xy, "New discretization parameter -b-" +
				"N.B. this will displayed the discretization corresponding to a " +
				"given value. It does not change the main model file where the " +
				"actual value for use in calculations should be specified.", ssNew);
		if (s != null) {
			updateScale(s);
		}
	}



	public void updateScale(String ss) {
		ssNew = ss.trim();
		updateScale();
	}

	public void updateScale() {
		if (treePoints == null) {
			return;
		}
		if (needsScaleRemake()) {
			ssCache = ssNew;
			double bes = Double.parseDouble(ssNew);
			channelTree = new PointTree(treePoints);
			MergeDiscretizer md = new MergeDiscretizer(channelTree.getPoints());
			double interpPower = -0.5; // TODO match with psics value
			compartmentTree = md.getCompartmentTree(bes, 20000, interpPower);
			ncompLabel.setText("" + compartmentTree.size() + " compartments");


			discLabel.setText(" at " + ssCache + " um^(3/2)");

		}
		updateProjection();
	}



	public void rotationChanged() {
		if (showDiscretization) {
			updateProjection();
		}
	}

	public void updateProjection() {
		if (compartmentTree != null) {

		Projector proj = new Projector(drawingCanvas.getProjectionMatrix(), drawingCanvas.get3Center(),
				drawingCanvas.get2Center());
		if (!compartmentTree.hasBoundaries() || proj != cachedProj) {
			cachedProj = proj;
		//	double[] cen = { 0., 0., 0. };
			compartmentTree.cacheProjectedBoundaries(proj);
			repaint();
		}
		}

	}


	public void hideAll() {
		for (PointPopulation p : pops) {
			p.setShow(false);
		}
		showNCH();
	}


	public void syncRelRefs(DistribPopulation dp) {
		if (dp.isRelative()) {
			String s = dp.getRelTarget();
			DistribPopulation cdp = dp.getRelTargetPopulation();
			if (cdp != null && cdp.getID().equals(s)) {
				// Nothing to do;
			} else {

			DistribPopulation dpr = rootController.getPopulation(s);
			if (dpr == null) {
				E.oneLineWarning("cant get rel tgt? " + s + " " + dp);
			}
			dp.setRelTargetPopulation(dpr);
			}
		}
	}


	private void semipopulate() {
		updateScale();

		// reuse old CHPs if we can, so we can call repopulate indiscriminately
		HashMap<DistribPopulation, PointPopulation> chm = new HashMap<DistribPopulation, PointPopulation>();
		for (PointPopulation c : pops) {
			chm.put(c.getPopulation(), c);
		}
		pops = new PointPopulation[populations.size()];

		for (int ipop = 0; ipop < populations.size(); ipop++) {
			DistribPopulation population = populations.get(ipop);
			if (population.isRelative()) {
				syncRelRefs(population);
			}



			PointPopulation chp = null;
			if (chm.containsKey(population)) {
				chp = chm.get(population);
			} else {
				chp = new PointPopulation(population);
			}
			pops[ipop] = chp;
		}
	}


	private void repopulate() {
		if (morphBuilder != null && morphBuilder.isRunning()) {
			morphBuilder.setStop();
		}
		semipopulate();

		// work out if it will be slow or not;
		boolean bslow = false;
		if (needsScaleRemake()) {
			bslow = true;
		} else if (channelTree == null) {
			bslow = true;
		} else {
			for (PointPopulation chp : pops) {
				if (chp.needsRemake(channelTree)) {
					bslow = true;
				}
			}
		}

		morphBuilder = new MorphBuilder(this);
		if (bslow) {
			morphBuilder.threadBuild();

		} else {
			morphBuilder.inplaceBuild();
		}
	}


	public synchronized void buildPopulations(MorphBuilder mb) {

		rootController.waitCursor();

		if (mb.reportProgress()) {
			rootController.taskAdvanced(0.05, "discretization");
		}

		semipopulate();

		if (mb.shouldStop()) {
			return;
		}

	//	timer.show("done sp");

		if (channelTree == null && treePoints != null) {
			channelTree = new PointTree(treePoints);
		}
		if (mb.shouldStop()) {
			return;
		}
	//	timer.show("done chtree");

		int nch = 0;
		if (channelTree != null) {
		int ipop = 0;
		for (PointPopulation chp : pops) {
			// Timer t = new Timer();
			if (mb.reportProgress()) {
				rootController.taskAdvanced(0.1 + 0.9 *  (ipop / (1. * pops.length)), chp.getID());
			}
			// TODO - do we care about square caps here?
			chp.realize(channelTree, mersenne, false); // lazy - only does it if pop
												// has changed

			// E.info("done pop " + chp.getID() + " " + chp.getNChannel());
			nch += chp.getNChannel();
			ipop += 1;
		//	timer.show("done " + chp.getID());
			if (mb.shouldStop()) {
				return;
			}
		}
		ncompLabel.setText("" + compartmentTree.size() + " compartments");
		repaint();
		}

		if (mb.reportProgress()) {
			rootController.taskCompleted("misc");
		}

		showNCH();


		rootController.normalCursor();
		repaint();

		sync3DPops();
		rootController.doneBuild();
	}



	public void labelMoved(String lbl) {
		for (PointPopulation chp : pops) {
			chp.labelMoved(lbl);
		}
		repopulate();
	}




	public void attached() {
		view3DController = (View3DController)(view3DDruid.getController());
		view3DController.setScaleFactor(fac3D);
		view3DController.setOrientationSource(this);

		drawingCanvas.attachGraphicsController(this);
		setMode("Pan");
		partCV.addValueWatcher(this);
		partSV.addValueWatcher(this);
		colorBV.addValueWatcher(new ValueWatcher() {
			public void valueChangedBy(Value pv, Object src) {
				repaint();
				syncSceneGraph();
			}
		});
		labelSV.addValueWatcher(new ValueWatcher() {
			public void valueChangedBy(Value pv, Object src) {
				if (src != this && editLabel != null) {
					editLabel.setText(labelSV.getAsString());
					listPanel.repaint();
					repaint();
				}
			}
		});

	}


	public void scaleFocus() {
		E.error("scale focus with no args?");
	}


	public void scaleFocus(boolean b) {
		if (!b && showDiscretization) {
			updateScale();
			repaint();
		}
	}



	public void setShowDiscretization(boolean b) {
		showDiscretization = b;
		if (b) {
			updateScale();
		}
		repaint();
	}




	public void setTarget(Object obj) {
		targetItem = (StandaloneItem) obj;
		morph = (CellMorphology) (targetItem.getObject());
		treePoints = morph.exportTreePoints(false); // TODO do we care about squareCaps here?

		pointLabels.clear();

		points = new IcingPoint[treePoints.length];

		for (int i = 0; i < treePoints.length; i++) {
			points[i] = new IcingPoint(treePoints[i]);
			points[i].color = fgColor;
			treePoints[i].iwork = i;
		}

		for (int i = 0; i < treePoints.length; i++) {
			if (treePoints[i].parent != null) {
				points[i].setParent(points[treePoints[i].parent.iwork]);
			}
		}
		for (int i = 0; i < treePoints.length; i++) {
			points[i].checkDeBall();
		}

		compartmentTree = null;
		active = null;
		preActive = null;
		listPanel.clear();

		extractLabels();
		listPanel.setItems(pointLabels);
		rootController.setMorphLabels(pointLabels);



		if (pam != null) {
			for (PartMap pm : pam) {
				colorHM.put(pm.getName(), pm.getStringColor());
			}
		}


		pam = new PartMap[partOfs.size()];
		for (int i = 0; i < pam.length; i++) {
			String s = partOfs.get(i);
			String scolor = null;
			if (colorHM.containsKey(s)) {
				scolor = colorHM.get(s);
			}
			pam[i] = new PartMap(partOfs.get(i), scolor);
		}
		partOfPanel.setItems(pam);
		applyColors();

		if (showDiscretization) {
			updateScale();
		}

		reframe();
		drawingCanvas.repaint();

		syncSceneGraph();
	}


	private void extractLabels() {
		pointLabels.clear();
		partOfs.clear();

		// only want unique lables;
		HashSet<String> labHS = new HashSet<String>();
		HashSet<String> duplabs = new HashSet<String>();

		HashSet<String> poHS = new HashSet<String>();


		for (IcingPoint p : points) {
			if (p.label != null) {
				if (duplabs.contains(p.label)) {
					// ignore;
				} else if (labHS.contains(p.label)) {
					duplabs.add(p.label);
				} else {
					labHS.add(p.label);
				}
			}
			if (p.partof != null) {
				if (poHS.contains(p.partof)) {
					// OK
				} else {
					poHS.add(p.partof);
					partOfs.add(p.partof);
				}
			}
		}
		for (IcingPoint p : points) {
			if (p.label != null && !duplabs.contains(p.label)) {
				pointLabels.add(new IcingLabel(p.label, p));
			}
		}
		if (pointLabels.size() > 20) {
			 hideLabelled();
		}
	}






	public boolean antialias() {
		// TODO Auto-generated method stub
		return false;
	}


	public void instruct(Painter ptr, Builder b) {
		cachedPainter = ptr;

		if (points == null) {
			return;
		}


		instructMorph(ptr, b);


		if (showChannels) {
			instructChannels(ptr, b);
		}

		if (showDiscretization) {
			instructDiscretization(ptr, b);
		}

	}


	@SuppressWarnings("unused")
	private void instructDiscretization(Painter ptr, Builder b) {
		if (compartmentTree == null) {
			return;
		}
		ptr.setColorCyan();
		for (Compartment cpt : compartmentTree.getCompartments()) {
			/*
			double[][] da = cpt.getProxBoundary();
			if (da != null) {
			   ptr.draw3DPolygon(da);
			}
			*/

			double[][] da = cpt.getCachedBoundary();
			if (da != null) {
				ptr.drawPolygon(da[0], da[1]);
			}
		}
	}

	@SuppressWarnings("unused")
	private void instructChannels(Painter ptr, Builder b) {
		double[] lims = ptr.getXYXYLimits();
		double dx = lims[2] - lims[0] + 50;
		lims[0] -= 0.5 * dx;
		lims[2] += 0.5 * dx;
		double dy = lims[3] - lims[1] + 50;
		lims[1] -= 0.5 * dy;
		lims[3] += 0.5 * dy;

		ptr.setColorGray();


		for (PointPopulation chp : pops) {
			if (chp.shouldShow()) {
				float[][][] chpos = chp.getPositions();
				if (chpos != null) {
				double chsize = 2.e-3;
				double f = chsize / ptr.getPixelArea();
				double diam = 2 * Math.sqrt(f / Math.PI);

				DistribPopulation cp = chp.getPopulation();
				Color c = (Color) (cp.getCachedColor());
				if (c == null) {
					String s = cp.getColor();
					if (s == null) {
						s = "0xff0000";
					}
					if (!s.toLowerCase().startsWith("0x")) {
						s = "0x" + s;
					}
					try {
						c = new Color(Integer.decode(s).intValue());
					} catch (Exception ex) {
						c = Color.red;
						E.info("dodgy color " + s);
					}
					cp.cacheColor(c);
				}
				ptr.setColor(c);


				if (drawingCanvas.isAntialiasing()) {

					for (int i = 0; i < chpos.length; i++) {
						float[][] chset = chpos[i];
						if (chset != null && chset.length > 0) {
							double x = ptr.getXProj(chset[0]);
							double y = ptr.getYProj(chset[1]);
							if (x > lims[0] && x < lims[2] && y > lims[1] && y < lims[3]) {
								if (f > 1.) {
									ptr.draw3DAreaMarks(chset, chset.length, diam);
								} else {
									ptr.drawSome3DMarks(chset, chset.length, 1./f);
								}
							}
						}
					}

				} else {
					int bw = 1;
					int bh = 1;
					int fint = (int) (f + 0.5);
					if (fint < markWidths.length) {
						bw = markWidths[fint];
						bh = markHeights[fint];
					}

					for (int i = 0; i < chpos.length; i++) {
						float[][] chset = chpos[i];
						if (chset != null && chset.length > 0) {
							double x = ptr.getXProj(chset[0]);
							double y = ptr.getYProj(chset[0]);

							if (x > lims[0] && x < lims[2] && y > lims[1] && y < lims[3]) {
								if (opaqueCore) {
									TreePoint pd = channelTree.getIthPoint(i);
									Position posd = pd.getPosition();
									Position posp = pd.parent.getPosition();
									double zp = ptr.getZProj(posp.getX(), posp.getY(), posp.getZ());
									double zd = ptr.getZProj(posd.getX(), posd.getY(), posd.getZ());

									if (f > 6) {
										ptr.drawUpper3DAreaMarks(chset, chset.length, diam, zp, zd);

									} else if (f > 1.) {
										ptr.drawUpper3DIntMarks(chset, chset.length, bw, bh, zp, zd);

									} else {
										ptr.drawUpperSome3DMarks(chset, chset.length, 1./f, zp, zd);
									}

								} else {


								if (f > 6) {
									ptr.draw3DAreaMarks(chset, chset.length, diam);

								} else if (f > 1.) {
									ptr.draw3DIntMarks(chset, chset.length, bw, bh);

								} else {
									ptr.drawSome3DMarks(chset, chset.length, 1./f);
									// could draw all in a fainter color.
									// Antailias and drawAreaMarks does this but
									// is very slow
								}
								}
							}
						}
					}
				}
			}
			}
		}
	}


	private void instructMorph(Painter ptr, Builder b) {
		ptr.setColor(fgColor);
		boolean colorCode = colorBV.getBoolean();

		if (style == NODES) {
			for (IcingPoint p : points) {
				if (p.parent != null) {
					if (colorCode) {
						ptr.setColor(p.color);
					}
					ptr.draw3DCarrot(p.x, p.y, p.z, p.r, p.px, p.py, p.pz, p.pr);
				}
			}
		} else if (style == SKELETON) {
			for (IcingPoint p : points) {
				if (p.parent != null) {
					if (colorCode) {
						ptr.setColor(p.color);
					}
					ptr.draw3DLine(p.x, p.y, p.z, p.px, p.py, p.pz);
				}
			}


		} else if (style == FILLED && swcSource && swcStyle == UNIFORM) {
			for (IcingPoint p : points) {
				if (p.parent != null) {
					if (colorCode) {
						ptr.setColor(p.color);
					}

					ptr.fill3DSegment(p.x, p.y, p.z, p.r, p.px, p.py, p.pz, p.r);
				}
				if (p.ball) {
					ptr.fill3DCircle(p.x, p.y, p.z, p.r);
				}
			}

		} else if (style == FILLED) {
			for (IcingPoint p : points) {
				if (p.parent != null) {
					if (colorCode) {
						ptr.setColor(p.color);
					}

					ptr.fill3DSegment(p.x, p.y, p.z, p.r, p.px, p.py, p.pz, p.pr);
				}
				if (p.ball) {
					ptr.fill3DCircle(p.x, p.y, p.z, p.r);
				}
			}


		} else if (style == SEGMENTS  && swcSource && swcStyle == UNIFORM) {
			for (IcingPoint p : points) {
				if (p.parent != null) {
					if (colorCode) {
						ptr.setColor(p.color);
					}

					ptr.draw3DSegment(p.x, p.y, p.z, p.r, p.px, p.py, p.pz, p.r);
				}
			}

		} else if (style == SEGMENTS) {
			for (IcingPoint p : points) {
				if (p.parent != null) {
					if (colorCode) {
						ptr.setColor(p.color);
					}

					ptr.draw3DSegment(p.x, p.y, p.z, p.r, p.px, p.py, p.pz, p.pr);
				}
			}
		}


		if (draggingLabel) {
			for (IcingPoint p : points) {
				b.add3DPickablePoint(p.x, p.y, p.z, p.pickable);
			}
		}


		if (active != null) {
			ptr.setColorRed();
			// ptr.fill3DIntCircle(active.x, active.y, active.z, 4);
			ptr.draw3DMark(active.x, active.y, active.z, 0, 10);
		}
		if (preActive != null) {
			ptr.setColorOrange();
			// ptr.fill3DCircle(preActive.x, preActive.y, preActive.z, preActive.r);
			ptr.draw3DMark(preActive.x, preActive.y, preActive.z, 0, 10);
		}


		if (draggingLabel && xptcache == null) {
			xptcache = new int[points.length];
			yptcache = new int[points.length];
			for (int i = 0; i < points.length; i++) {
				IcingPoint p = points[i];
				xptcache[i] = ptr.getXProjPixel(p.x, p.y, p.z);
				yptcache[i] = ptr.getYProjPixel(p.x, p.y, p.z);
			}
		}


		if (!draggingLabel) {
			xptcache = null;
			yptcache = null;
		}

		bufCanvasWidth = ptr.getCanvasWidth();
		for (IcingLabel lbl : pointLabels) {
			if (lbl.visible()) {
				IcingPoint p = lbl.getTarget();
				String s = lbl.getText();
				if (p == null) {
					ptr.setColorWhite();


					lbl.xtgt = lbl.xpos - 8; // - lbl.getXRel();
					lbl.ytgt = lbl.ypos + 8; // - lbl.getYRel();
					ptr.setColorYellow();

				} else {
					lbl.xtgt = ptr.getXProjPixel(p.x, p.y, p.z);
					lbl.ytgt = ptr.getYProjPixel(p.x, p.y, p.z);


					lbl.xpos = lbl.xtgt + lbl.getXRel();
					lbl.ypos = lbl.ytgt + lbl.getYRel();

					ptr.setColorWhite();
				}

				if (lbl.w <= 0) {
					lbl.w = ptr.stringWidth(s);
				}




				ptr.drawString(s, lbl.xpos, lbl.ypos);
				b.addPickableRegion(lbl, lbl.xpos, lbl.ypos-14, 6 * s.length(), 14);

				if (lbl.xpos < lbl.xtgt - lbl.w / 2) {
					ptr.drawPixelLine(lbl.xpos + lbl.w, lbl.ypos, lbl.xtgt, lbl.ytgt);
				} else {
					ptr.drawPixelLine(lbl.xpos, lbl.ypos, lbl.xtgt, lbl.ytgt);
				}
			}
		}
	}



	public void attachLabelToPoint(IcingLabel lbl, IcingPoint pt) {
		lbl.setTarget(pt);
		lbl.saveLabelToTreePoint();
		labelMoved(lbl.getText());

	}


	public void turnPlus() {
		drawingCanvas.turn(10.0);
	}

	public void turnMinus() {
		drawingCanvas.turn(-10.0);
	}


	public void reframe() {
		drawingCanvas.reframe();
	}


	public Box getLimitBox(Painter ptr) {
			ptr.startBox();
			if (points != null) {
				for (IcingPoint p : points) {
					ptr.push3D(p.x, p.y, p.z);
				}
			}
		return ptr.getBox();
	}


	public void backgroundPressed(int i, int x, int y) {

		IcingPoint nrstPoint = null;
		int sd2 = 1000 * 1000;
		for (IcingPoint p : points) {

			int d2 = cachedPainter.screenDistance2(p.x, p.y, p.z, x, y);
			if (d2 < sd2) {
				sd2 = d2;
				nrstPoint = p;
			//	E.info("comp " + x + " " + y + " " + d2);
			}
		}
		if (nrstPoint != null) {
		active = nrstPoint;
		drawingCanvas.setRollCenter(active.x, active.y, active.z);
		String lbltxt = " ";
		if (active.getLabel() != null) {
			lbltxt = ", " + active.getLabel() + ", ";
		}
		String stxt = "ID: " + active.getID() + lbltxt;
		activeLabel.setText(stxt);

		String postxt = String.format("(%4.1f, %4.1f, %4.1f)", active.x, active.y, active.z);
		activePos.setText(postxt);
		}
	}


	public void pickDragged(Pickable pbl, org.catacomb.be.Position pos, int button, int ix, int iy) {
		Object obj = pbl.getRef();


		if (obj instanceof IcingLabel) {
			IcingLabel lbl = (IcingLabel)obj;
			setActiveLabel(lbl);

			int ixbl = ix - 3 * dragLabel.getText().length();
			int iybl = iy + 7;


			int ixtgt = ixbl - 8;
			if (lbl.xtgt > lbl.xpos) {
				ixtgt = ixbl + lbl.w + 8;
			}

			int iytgt = iybl + 8;

			dragLabel.setIntPosition(ixbl, iybl);

			if (Math.abs(ixtgt - dragLabel.xtgt) + Math.abs(iytgt - dragLabel.ytgt) > 60) {
				dragLabel.detach();
			}

			if (dragLabel.isFree() && xptcache != null) {
				for (int i = 0; i < xptcache.length; i++) {
					if (Math.abs(ixtgt - xptcache[i]) <= 6 && Math.abs(iytgt - yptcache[i]) < 6) {
						attachLabelToPoint(dragLabel, points[i]);
						break;
					}
				}
			}

			if (!dragLabel.isFree()) {
				dragLabel.relativize();
			}

			// ADHOC - the ix,iy in this call are where the middle of the lael should be;
		} else {

		}
	}





	public void pickEnteredTrash(Pickable pbl) {
	}


	public void pickHovered(Pickable hoverItem) {
	}


	public void pickLeftTrash(Pickable pbl) {
	}


	public void pickPressed(Pickable pbl, int button, int xpr, int ypr) {
		preActive = active;
		Object obj = pbl.getRef();
		if (obj instanceof IcingPoint) {
			active = (IcingPoint) (pbl.getRef());
			drawingCanvas.setRollCenter(active.x, active.y, active.z);

			String stxt = "ID: " + active.getID() + " " +
				String.format("(%4.1f, %4.1f, %4.1f)", active.x, active.y, active.z);
			activeLabel.setText(stxt);

		} else if (obj instanceof IcingLabel) {
			draggingLabel = true;
			setActiveLabel((IcingLabel)obj);


		}
	}


	public void pickReleased(Pickable pbl, int button) {
		draggingLabel = false;
		//applyLabels();
	}


	public void pickTrashed(Pickable pbl) {
	}


	public void trashPressed() {
	}



	public void setActiveLabel(IcingLabel il) {
		if (il != null && il != dragLabel) {
			dragLabel = il;
			listPanel.setSelected(dragLabel);
			editLabel(dragLabel);
		}
	}


	public void newLabel() {
		int ilab = 0;
		int ntop = 1;
		HashSet<String> exl = new HashSet<String>();
		for (IcingLabel l : pointLabels) {
			exl.add(l.getText());
			if (l.isFree() && Math.abs(l.xpos - (bufCanvasWidth - 100)) < 40) {
				ntop += 1;
			}
		}

		while(exl.contains("label" + ilab)) {
			ilab += 1;
		}
		String lbl = "label" + ilab;
		IcingLabel lab = new IcingLabel(lbl, null);
		lab.xpos = bufCanvasWidth - 100;
		lab.ypos = 40 + 20 * ntop;

		pointLabels.add(lab);
		listPanel.setItems(pointLabels);

		rootController.setMorphLabels(pointLabels);
		repaint();
	}




	public void labelSelected(String s) {

		IcingLabel lbl = null;
		for (IcingLabel l : pointLabels) {
			if (s.startsWith(l.getText())) {
				lbl = l;
				break;
			}
		}
		if (lbl == null) {
			E.error("no label? " + s);
		}
		editLabel(lbl);
	}

	public void editLabel(IcingLabel lbl) {
		if (lbl == null) {
			editLabel = null;
			labelSV.reportableSetString("", null);
		} else {
			editLabel = lbl;
			labelSV.reportableSetString(lbl.getText(), this);
			lbl.setShow();
			repaint();
		}
	}


	public void deleteLabel() {
		if (editLabel != null) {
			pointLabels.remove(editLabel);
			listPanel.setItems(pointLabels);
			editLabel = null;
			repaint();
		}
		if (pointLabels.size() > 0) {
			listPanel.selectAt(0);
			editLabel(pointLabels.get(0));
		} else {
			labelSV.reportableSetString("", this);
		}
	}


	public void showLabelled() {
		for (IcingLabel lbl : pointLabels) {
			lbl.setShow();
		}
		repaint();
	}


	public void hideLabelled() {
		editLabel = null;
		labelSV.reportableSetString("", null);
		for (IcingLabel lbl : pointLabels) {
			lbl.setHide();
		}
		repaint();
	}


	private void repaint() {
		drawingCanvas.repaint();
	}


	private void applyLabels() {


		for (IcingLabel p : pointLabels) {
			p.saveLabelToTreePoint();
		}
	}


	public void save(StandaloneItem item) {
		applyLabels();
		if (item instanceof StandaloneSWCItem) {
			saveSWC((StandaloneSWCItem) item);
		} else {
			CellMorphology cm = new CellMorphology();
			cm.setID(item.getID());
			cm.importTreePoints(treePoints);
			item.setObject(cm);
			item.saveXMLObject();
		}
	}


	public void saveSWC(StandaloneSWCItem item) {
		item.saveMorphology(treePoints);
	}


	public void setColors(int ibg, int igrid, int iaxis, int ifg) {
		drawingCanvas.setBackgroundColor(new Color(ibg));
		drawingCanvas.setAxisColor(new Color(iaxis));
		drawingCanvas.setGridColor(new Color(igrid));

		fgColor = new Color(ifg);
		repaint();
	}


	public void partOfSelected(String s) {
		activePAM = null;
		for (PartMap pa : pam) {
			if (pa.getName().equals(s)) {
				activePAM  = pa;
			}
		}
		if (activePAM == null) {
			E.error("no such item ? " + s);
		} else {
			partCV.reportableSetColor(activePAM.getStringColor(), this);
			partSV.reportableSetString(activePAM.getName(), this);
		}
	}


	private void applyColors() {
		HashMap<String, Color> cHM = new HashMap<String, Color>();
		for (PartMap pm : pam) {
			cHM.put(pm.getOriginalName(), pm.getColor());
		}
		for (IcingPoint p : points) {
			String pof = p.partof;
			if (pof != null) {
				p.color = cHM.get(pof);
			} else {
				p.color = fgColor;
			}
		}
	}



	public void valueChangedBy(Value pv, Object src) {
		if (src.equals(this)) {
			// skip;
		} else if (activePAM != null) {

			E.info("changing active pam " + src + " " + partSV.getAsString());

			 activePAM.setColor(partCV.getAsString());
			 activePAM.setName(partSV.getAsString());

			 applyColors();

			 partOfPanel.repaint();
			 repaint();
		}
	}


	public void setShowGrid(boolean b) {
		 drawingCanvas.setShowGrid(b);
		repaint();

	}


	public double[] getFourMatrixOrientation() {
		double[] fm = drawingCanvas.getFourMatrix();
		for (int i = 0; i < 3; i++) {
			fm[4 * i + 3] *= fac3D;
		}
		return fm;
	}


	public void sync3DOrientation() {
		double[] fm = view3DController.getFourMatrix();
		// TODO is this the best place? the 3D rendered object is fac3D times the
		// size of the our version
		for (int i = 0; i < 3; i++) {
			fm[4 * i + 3] /= fac3D; // = 0.;
		}
		drawingCanvas.setFourMatrix(fm);
		// reframe();
	}


	public void saveX3D(File f) {
		try {
		X3DWriter x3dw = new X3DWriter(f);
		x3dw.drawTree(points);

		for (PointPopulation p : pops) {
			// TODO - create positions if not already there?
			if (p.hasPositions() && p.shouldShow()) {
				Color c = (Color)(p.getBestColor());
				float[][][] chp = p.getCHPos();
				x3dw.addChannels(chp, c.getRed(), c.getGreen(), c.getBlue());
			}
		}
		x3dw.close();
		} catch (IOException ex) {
			E.error("cant write " + f + " " + ex);
		}
	}

}
