package org.psics.icing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.catacomb.druid.build.Druid;
import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.gui.base.DruDialog;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.druid.gui.base.DruLabelPanel;
import org.catacomb.druid.gui.base.DruScrollingHTMLPanel;
import org.catacomb.druid.gui.edit.DruCheckboxMenuItem;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.druid.gui.edit.DruProgressReport;
import org.catacomb.druid.gui.edit.DruTreePanel;
import org.catacomb.druid.load.DruidAppBase;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.service.AppPersist;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.interlish.structure.TreeNode;
import org.catacomb.util.AWTUtil;
import org.catacomb.util.ImageUtil;
import org.psics.be.E;
import org.psics.be.IDable;
import org.psics.be.TaskWatcher;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.DistribSpec;
import org.psics.model.electrical.CellProperties;
import org.psics.model.morph.CellMorphology;
import org.psics.project.StandaloneItem;
import org.psics.project.StandaloneMorphologyItem;
import org.psics.project.StandaloneSWCItem;
import org.psics.util.FileUtil;
import org.psics.util.JUtil;



public class IcingController implements Controller, TaskWatcher {


	@IOPoint(xid="MainFrame")
	public DruFrame mainFrame;

	@IOPoint(xid="*.RecentMenu")
	public DruMenu recentMenu;


	@IOPoint(xid="ModelsTree")
	public DruTreePanel modelsTree;

	@IOPoint(xid="morphedit")
	public Druid morphEditor;

	@IOPoint(xid="memedit")
	public Druid memEditor;

	@Editable(xid="morphFormat")
	public StringValue morphFormat = new StringValue("xml");

	@IOPoint(xid="morphSaveChooser")
	public DruDialog morphFormatD;

	@IOPoint(xid="messageDialog")
	public DruDialog messageD;

	@IOPoint(xid="messageHTML")
	public DruScrollingHTMLPanel messagePanel;


	@IOPoint(xid="infoL")
	public DruLabelPanel statusLabel;

	@IOPoint(xid="statusPR")
	public DruProgressReport progressReport;

	@IOPoint(xid="autosaveCBMI")
	public DruCheckboxMenuItem autosaveMI;

	private IcingDM icingDM;


	StandaloneItem activeItem;

	boolean continueOperation;


	public final static int MORPH = 1;
	public final static int PROPS = 2;
	public final static int CHANNEL = 3;
	public final static int OTHER = 10;





	MorphologyController morphController;
	MembraneController memController;



	IcingMessageHandler messageHandler;

	NewNameDialogController newNameDialogController;

	ColorsDialogController colorsDialogController;


	double lastFraction;
	String lastMessage;


	public boolean autoSave;



	public IcingController() {

	}


	public void requestClose() {
		 requestExit();
	}

	public void requestExit() {
		System.exit(0);
	}


	public void attached() {
		icingDM = IcingDM.getDM();

		String[] sa = DruidAppBase.getSys().getRecentPaths();
		recentMenu.setItems(sa);



		morphController = (MorphologyController)(morphEditor.getController());
		memController = (MembraneController)(memEditor.getController());

		memController.setMorphologyController(morphController);

		memController.setRootController(this);
		morphController.setRootController(this);


		messageHandler = new IcingMessageHandler(messageD, messagePanel);
	    E.addMessageHandler(messageHandler);


	    autoSave = true;
	    if (AppPersist.hasValueFor("autosave")) {
	    	String s = AppPersist.getValueFor("autosave");
	    	if (s.equals("true")) {
	    		autoSave = true;
	    	} else {
	    		autoSave = false;
	    	}
	    }
	    autosaveMI.setState(autoSave);
	}


	public void setAutoSave(boolean b) {
		autoSave = b;
		AppPersist.setValue("autosave", (b ? "true" : "false"));
	}


	public void waitCursor() {
		mainFrame.waitCursor();
	}

	public void normalCursor() {
		mainFrame.normalCursor();
	}


	public void showMessages() {
		messageHandler.show();
	}

	public void clearMessages() {
		messageHandler.clearMessages();
	}



	public void setMorphLabels(ArrayList<IcingLabel> pts) {
		memController.setMorphLabels(pts);
	}



	private boolean closeOldDM() {
		// check unsaved files etc;
		// returns true if ok to proceed
		return true;
	}


	public void openRecent(String s) {
		if (closeOldDM()) {
    		IcingDM.newStart();
    		icingDM = IcingDM.getDM();
//    		icingDM.readFile(new File(s));

    		statusLabel.setText("Opening " + s);
        	progressReport.setIndeterminate(true);
        	waitCursor();
        	icingDM.threadReadFile(new File(s), this);

		}
	}


    public void open() {
    	if (closeOldDM()) {
    		IcingDM.newStart();
    		icingDM = IcingDM.getDM();
    		File f = Dialoguer.getFileToRead("Open");
    		if (f != null) {
    			openFolder(f);
    		}
    	}
    }

    private void openFolder(File f) {
    	DruidAppBase.getSys().addRecentFile(f);
    	statusLabel.setText("Opening " + f.getName());
    	progressReport.setIndeterminate(true);
    	waitCursor();
    	icingDM.threadReadFile(f, this);
    	// icingDM.readFile(f);

    }



    public void taskAdvanced(double f, String msg) {
    	lastFraction = f;
    	lastMessage = msg;
    	Runnable r = new Runnable() {
    	    public void run() {
    	    	statusLabel.setText(lastMessage);
    	    	progressReport.setFraction(lastFraction);
    	    	progressReport.update();
    	    }
    	};
    	SwingUtilities.invokeLater(r);
    }



    public void taskCompleted(String task) {
    	if (task.equals(IcingDM.READ_FILE_TASK)) {
    		syncToNewDM();
    	} else if (task.equals("misc")) {

    	} else {
    		E.error("unknown task");
    	}
    	statusLabel.setText("Ready");
    	progressReport.setFraction(1.);
    	progressReport.update();
    	normalCursor();
    }


    public void openExample() {
    	try {
    		File fdir = File.createTempFile("icing", ".tmp");
    		E.info("tmp file is " + fdir);
    		fdir.delete();
    		fdir.mkdir();

    		JUtil.unpackPackage(IcingRoot.class, "samples", fdir);
    		openFolder(fdir);
    	} catch (Exception ex) {
    		E.error("cant make tmp file " + ex);
    	}
    }


    	public void setShowChannels(boolean b) {
    		morphController.setShowChannels(b);

    	}


    private void syncToNewDM() {
    	HashBasedTree itemTree = icingDM.getItemTree();
    	modelsTree.setTree(itemTree);

    	String[] exps = {"CellProperties", "SWC Morphology", "CellMorphology"};
    	for (String s : exps) {
    		if (itemTree.hasChild(s)) {
    			modelsTree.expandPath(itemTree.getChildPath(s));
    		}
    	}


    	memController.setChannelIDs(icingDM.getChannelIDs());
    }




    public void deleteSelected() {
    	if (activeItem != null) {
    		icingDM.deleteItem(activeItem);
    	}
    }

    public void syncToNewActive(StandaloneItem sit) {
    	icingDM.newFileAppeared(sit.getFile());
    	String sid = sit.getID();

    	E.info("syncing to new active " + sid);

    	modelsTree.ensureVisible(sid);
    	modelsTree.setSelected(sid);
    	treeItemSelected(sid);
    }


    @SuppressWarnings("unused")
    private void dumpTree(String indent, TreeNode tn) {
    	E.info("tree: " + indent + "  " + tn.toString());
    	for (int i = 0; i < tn.getChildCount(); i++) {
    		Object obj = tn.getChild(i);
    		if (obj instanceof TreeNode) {
    			dumpTree(indent + "   ", (TreeNode)obj);
    		} else {
    			E.info(indent + "     (no tree node)" + obj);
    		}
    		}
    }




    // REFAC one standaloneItem per type with read/write code in
    public void treeItemSelected(String s) {
    	StandaloneItem sit = icingDM.getItem(s);
    	activeItem = sit;
    	if (sit.hasEdited()) {
    		Object ed = sit.getEdited();
    		if (ed instanceof CellMorphology) {
    			if (sit instanceof StandaloneSWCItem) {
    				morphController.setSWCSource(true);
    			} else {
    				morphController.setSWCSource(false);
    			}
    			morphEditor.show(sit);



    		} else if (ed instanceof DistribSpec) {
    			memEditor.show(sit);


    		} else {
    			E.error("unknown type");
    		}


    	} else {
    		if (!sit.hasObject()) {
    			waitCursor();
    		}
    	Object obj = sit.getObject();
    	if (obj instanceof CellMorphology) {
    		sit.setType(MORPH);
    		morphEditor.show(sit);

    	} else if (obj instanceof CellProperties) {
    		DistribSpec dp = ((CellProperties)obj).getChannelDistributionSpecification();
    		sit.setEdited(dp);
    		memEditor.show(sit);
    		sit.setType(PROPS);

    	} else {
    		sit.setType(OTHER);
    		E.info("cant yet handle " + obj);
    	}
    	normalCursor();
    	}

    }



	void syncPopDisplay() {
    	ArrayList<DistribPopulation> pops = memController.getPopulations();

    	morphController.setPopulations(pops);
    	morphController.hideAll();

    	int ndr = 0;
    	for (int i : memController.getSelectedPopulationIndexes()) {
    		morphController.setShow(pops.get(i), true);
    		ndr += 1;
    	}
    	morphController.popListChange();
    }








    public void saveAll() {
    	E.missing();
    }

    public void saveActive() {
    	if (activeItem == null) {
    		return;
    	}
    	if (activeItem.getType() == MORPH) {
    		morphController.save(activeItem);

    	} else if (activeItem.getType() == PROPS) {
    		memController.storeEdits(activeItem);
    		 activeItem.saveXMLObject();
    	} else {
    		E.warning("cant save : " + activeItem);
    	}
    }


    public void cancel() {
    	continueOperation = false;
    }







    public void saveActiveAs() {
    	if (activeItem == null) {
    		return;
    	}
    	if (activeItem.getType() == MORPH) {
    		continueOperation = true;
    		if (activeItem instanceof StandaloneSWCItem) {
    			morphFormat.set("swc");
    		} else {
    			morphFormat.set("xml");
    		}
    		morphFormatD.open();

    	} else if (activeItem.getType() == PROPS) {
    		 String newid = getNewName("Save as: new name - ", activeItem.getID());
    		 if (newid != null) {
    			 memController.storeEdits(activeItem);
    			 File f = new File(activeItem.getFile().getParentFile(), newid + ".xml");

    			 Object obj = activeItem.getObject();
    			 StandaloneItem sit = new StandaloneItem(newid, f, null);
    			 sit.saveXMLObjectAs(obj, newid);
    			 syncToNewActive(sit);
    		 }

    	} else {
    		E.missing();
    	}
    }


    public void exportActiveAs() {
        	File f = Dialoguer.getFileToWrite("export3d", null, "x3d", "x3d 3D model");
        	if (f != null) {
        		morphController.saveX3D(f);
        	}
        }


    	/*
    	if (activeItem.getType() == CHANNEL) {
    		KSChannel ksc = (KSChannel)activeItem.getObject();
    		String nnsug = ksc.getID() + "-channelml.xml";
    		int[] xy = {200, 200};
    		String newname = Dialoguer.getNewName(xy, "Export in ChannelML format as: ", nnsug);
    		if (newname != null) {
    			channelml cml = new channelml();
    			cml.populateFrom(ksc);
    			String s = Serializer.serialize(cml);
    			File fdir = activeItem.getFile().getParentFile();
    			File fout = new File(fdir, newname);
    			FileUtil.writeStringToFile(s, fout);
    		}


    	} else {
    		Dialoguer.showText("The export option currently only applies to channel models");
    	}
    }

*/










    public void newChannelDist() {
    	if (icingDM == null) {
    		E.error("must open a project folder first");
    		return;
    	}
    	 String newid = getNewName("Name for new channel distribution", "");
    	 if (newid != null) {
    		 File f = new File(icingDM.getRootFolder(), newid + ".xml");

			 Object obj = new CellProperties();
			 ((IDable)obj).setID(newid);
			 StandaloneItem sit = new StandaloneItem(newid, f, null);
			 sit.saveXMLObjectAs(obj, newid);
			 syncToNewActive(sit);
    	 }


    }


    private String getNewName(String msg, String oldname) {
    	if (newNameDialogController == null) {
    		newNameDialogController = new NewNameDialogController();
    	}
    	String ret = newNameDialogController.getNewName(morphEditor.getXY(), msg, oldname);
    	if (ret != null) {
    		ret = ret.replaceAll(" ", "_");
    	}
    	return ret;
    }


    public void showColorDialog() {
    	if (colorsDialogController == null) {
    		colorsDialogController = new ColorsDialogController();
    		colorsDialogController.setMorphController(morphController);
    	}
    	colorsDialogController.showNonModalAt(100, 100);
    }



    public void saveMorphAs() {
    	String s = morphFormat.getAsString();
    	E.info("saving in morph format " + s);
    	String extTxt = (s.equals("swc") ? "SWC Morphology files" : "PSICS XML files");
    	File f = Dialoguer.getFileToWrite("morph", activeItem.getFile().getParentFile(), s, extTxt);
    	if (f != null) {
    		String sid = FileUtil.getRootName(f);
    		StandaloneItem sai = null;
    		if (s.equals("swc")) {
    			sai = new StandaloneSWCItem(sid, f);
    		} else {
    			sai = new StandaloneMorphologyItem(sid, f);
    		}
    		((MorphologyController)morphEditor.getController()).save(sai);
    		syncToNewActive(sai);
    	}
    }


	public DistribPopulation getPopulation(String s) {
		 return memController.getPopulation(s);
	}

	 @SuppressWarnings("unused")
	public void itemChanged(StandaloneItem popItem) {
		 // TODO show in the tree that it has unsaved edits

	}


	public void saveSnapshot() {
		File f = Dialoguer.getFileToWrite("save snapshot as");
		if (f != null) {
			BufferedImage img = AWTUtil.getBufferedImage(mainFrame.getContent());
			if (img != null) {
				ImageUtil.writePNG(img, f);
			}
		}
	}


	public void doneBuild() {
		memController.upToDate();
	}

}


