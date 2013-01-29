package org.psics.icing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.catacomb.druid.build.Druid;
import org.catacomb.druid.gui.edit.DruScrollingCheckboxListPanel;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.interlish.structure.Targetable;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;
import org.psics.be.E;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.DistribSpec;
import org.psics.model.electrical.CellProperties;
import org.psics.project.StandaloneItem;
import org.psics.util.Timer;


public class MembraneController implements Controller, Targetable, ValueWatcher {


	@IOPoint(xid="popedit")
	public Druid popEditor;

	@IOPoint(xid="poplist")
	public DruScrollingCheckboxListPanel populationList;

	PopulationController popController;

	IcingController icingController;
	MorphologyController morphController;


	public DistribSpec populations;
	DistribPopulation activePopulation;

	StandaloneItem popItem;


	HashMap<String, DistribPopulation> popHM;

	HashSet<DistribPopulation> checkedHS;


	boolean showOnCell = true;

	public void attached() {
		// TODO Auto-generated method stub
		popController = (PopulationController)(popEditor.getController());
		popController.setMembraneController(this);
	}




	public void setRootController(IcingController ic) {
		icingController = ic;
		popController.setRootController(ic);
	}


	public void setShowChannels(boolean b) {
		showOnCell = b;
		if (showOnCell) {
			icingController.syncPopDisplay();
		} else {
		//	icingCotroller.showNoChannels();
		}
	}


	public void setTarget(Object obj) {

		popItem = (StandaloneItem)obj;
    	if (popItem.hasEdited()) {
    		Object ed = popItem.getEdited();
    		populations = (DistribSpec)ed;
    		syncToList();
    	} else {
    		E.error("no distrib spec?");
    	}
	}

	public void valueChangedBy(Value pv, Object src) {
		// TODO Auto-generated method stub

	}


    public void popSelected(String s) {
    	activePopulation = populations.getPopulation(s);
    	popEditor.show(activePopulation);

    }

    @SuppressWarnings("unused")
    public void popToggled(boolean b) {
    	icingController.syncPopDisplay();
    }


    public void addPopulation() {

    	activePopulation = populations.newPopulation();
    	syncToList();

    	popEditor.show(activePopulation);

    	if (showOnCell) {
    		icingController.syncPopDisplay();
    	}
    }

    public void deletePopulation() {

    	if (activePopulation != null) {
    		populations.remove(activePopulation);
    		syncToList();
    		if (showOnCell) {
    			icingController.syncPopDisplay();
    		}
    	}
    }






    public void moveUp() {
    	readChecked();
    	populations.moveUp(activePopulation);
    	syncToList();
    }


    public void moveDown() {
    	readChecked();
    	populations.moveDown(activePopulation);
    	syncToList();
    }



	public void populationChanged() {
		if (icingController.autoSave) {
		if (popItem != null) {
			Timer t = new Timer();
			storeEdits(popItem);
   		 	popItem.saveXMLObject();
   		 	t.showSlow("saving xml object");
		} else {
			E.warning("not saving - no popItem");
		}
		} else {
			icingController.itemChanged(popItem);
		}
	}


	public void storeEdits(StandaloneItem item) {
		if (item != null && item.getObject() instanceof CellProperties) {
			CellProperties cp = (CellProperties)(item.getObject());
			DistribSpec ds = (DistribSpec)(item.getEdited());
			if (ds != null) {
				cp.setPopulationsFrom(ds);
			} else {
				E.info("store edits - no edited item??");
			}
		} else {
			E.info("not storing! " + item + " " + item.getObject());
		}
	}


    private ArrayList<CPWrapper> colorWrap(ArrayList<DistribPopulation> items) {
		 ArrayList<CPWrapper> cpw = new ArrayList<CPWrapper>();
		 for (DistribPopulation cp : items) {
			 cpw.add(new CPWrapper(cp));
		 }
		 return cpw;
	}



    private void readChecked() {
    	ArrayList<DistribPopulation> pops = populations.getItems();
    	checkedHS = new HashSet<DistribPopulation>();
    	for (int i : populationList.getCheckedIndexes()) {
    		checkedHS.add(pops.get(i));
    	}
    }


    private void syncToList() {
    	popHM = null;
    	ArrayList<DistribPopulation> pops = populations.getItems();
    	populationList.setItems(colorWrap(pops));
    	int isel = -1;
    	if (activePopulation != null) {
    		isel = pops.indexOf(activePopulation);
    	}
    	if (isel >= 0) {
    		populationList.selectAt(isel);
    	}

    	String[] sa = new String[pops.size()];
    	for (int i = 0; i < sa.length; i++) {
    		sa[i] = pops.get(i).getID();
    	}
    	popController.setPopulationIDs(sa);

    	if (checkedHS != null) {
    		int[] ichs = new int[checkedHS.size()];
    		int ic = 0;
    		for (int i = 0; i < pops.size(); i++) {
    			if (checkedHS.contains(pops.get(i))) {
    				ichs[ic] = i;
    				ic += 1;
    			}
    		}
    		if (ic != ichs.length) {
    			E.warning("checked pop miscount? " + ic + " " + ichs.length);
    		}
    		populationList.setCheckedIndexes(ichs);
    	}

    }


    public void populationColorChanged(Object src) {
    	populationList.repaint();
    	if (src != this) {
    		populationChanged();
    	}
    }



	// TODO too much interlinking of controllers
	public void setMorphologyController(MorphologyController mc) {
		morphController = mc;
		popController.setMorphologyController(mc);
	}


	public void setMorphLabels(ArrayList<IcingLabel> pts) {
		popController.setMorphLabels(pts);

	}


	public void setChannelIDs(String[] channelIDs) {
		 popController.setChannelIDs(channelIDs);

	}

	public int[] getSelectedPopulationIndexes() {
		return populationList.getCheckedIndexes();
	}


	public ArrayList<DistribPopulation> getPopulations() {
		 return populations.getItems();
	}




	private void makePopHM() {
		popHM = new HashMap<String, DistribPopulation>();
		if (populations != null) {
			for (DistribPopulation dp : populations.getItems()) {
				popHM.put(dp.getID(), dp);
			}
		}
	}


	public DistribPopulation getPopulation(String s) {
		if (popHM == null) {
			makePopHM();
		}
		DistribPopulation ret = null;
		if (s != null && popHM.containsKey(s)) {
			ret = popHM.get(s);
		}
		return ret;
	}




	public DistribPopulation getFinalRelTargetPopulation(String s) {
		int nrec = 1;
		DistribPopulation ret = null;

		ret = getPopulation(s);
		while (ret != null && nrec < 10 && ret.isRelative()) {
			ret = getPopulation(ret.getRelTarget());
		}
		return ret;
	}

	public void upToDate() {
		popController.upToDate();
	}
}
