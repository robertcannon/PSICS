package org.psics.icing;

import java.util.ArrayList;

import org.catacomb.druid.gui.base.DruLabelPanel;
import org.catacomb.druid.gui.edit.DruButton;
import org.catacomb.druid.gui.edit.DruChoice;
import org.catacomb.druid.gui.edit.DruListPanel;
import org.catacomb.druid.gui.edit.DruTextField;
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
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.PopulationConstraint;

public class PopulationController implements Controller, Targetable, ValueWatcher {


	@IOPoint(xid="popidlabel")
	public DruLabelPanel idLabel;

	@IOPoint(xid="infoL")
	public DruLabelPanel infoLabel;


	@IOPoint(xid="channel")
	public DruChoice channelCH;


	@IOPoint(xid="relpopCH")
	public DruChoice relpopCH;

	@Editable(xid="relpopTF")
	public StringValue relpopSV = new StringValue();

	@Editable(xid="densitytf")
	public StringValue densitySV = new StringValue();

	@Editable(xid="capdencb")
	public BooleanValue capDensityBV = new BooleanValue();

	@Editable(xid="maxtf")
	public StringValue maxDensitySV = new StringValue("", false);

	@Editable(xid="relCB")
	public BooleanValue relBV = new BooleanValue();

	@Editable(xid="fixtotcb")
	public BooleanValue fixtotBV = new BooleanValue();

	@Editable(xid="numbertf")
	public StringValue totnumSV = new StringValue("", false);


	@IOPoint(xid="constraints")
	public DruListPanel constraintsList;

	@IOPoint(xid="allocCH")
	public DruChoice allocCH;

	@IOPoint(xid="modeChoice")
	public DruChoice modeChoice;

	@IOPoint(xid="conditionChoice")
	public DruChoice conditionChoice;

	@IOPoint(xid="labelChoice")
	public DruChoice labelChoice;

	@IOPoint(xid="updateB")
	public DruButton updateButton;



	@Editable(xid="conditionTF")
	public StringValue conditionSV = new StringValue();

	@Editable(xid="color")
	public ColorValue colorCV = new ColorValue();

	@IOPoint(xid="conditionTF")
	public DruTextField conditionTF;


	MorphologyController morphController;
	IcingController rootController;
	MembraneController memController;


	DistribPopulation pop;

	PopulationConstraint activeConstraint;

	String allocationMode;


	public void attached() {
		// TODO this should all happen automatically...
		conditionTF.able(false);
		densitySV.addValueWatcher(this);
		colorCV.addValueWatcher(this);
		totnumSV.addValueWatcher(this);
		maxDensitySV.addValueWatcher(this);
		fixtotBV.addValueWatcher(this);
	}


	public void setTarget(Object obj) {
		if (obj == null) {
			pop = null;
			densitySV.setAble(false);
			maxDensitySV.setAble(false);
			totnumSV.setAble(false);

		} else {


		pop = (DistribPopulation)obj;
		idLabel.setText(pop.getID());

		boolean bcd = pop.getCapDensity();
		capDensityBV.reportableSetBoolean(bcd, this);
		maxDensitySV.reportableSetString(String.format("%8.3f", pop.getMaxDensity()), this);
		maxDensitySV.setAble(bcd);

		densitySV.reportableSetString(pop.getDensityExpression(), this);
		densitySV.setAble(true);

		boolean bft = pop.getFixTotal();
		fixtotBV.reportableSetBoolean(bft, this);
		totnumSV.reportableSetString("" + pop.getTotalNumber(), this);
		totnumSV.setAble(bft);

		if (pop.isRegular()) {
			allocCH.setSelected("Regular");
		} else {
			allocCH.setSelected("Poisson");
		}

		String sc = pop.getColor();
		// E.info("input pop color " + sc);
		if (sc == null) {
			sc = "0xff4040";
		}
		colorCV.reportableSetColor(sc, this);
		constraintsList.setItems(pop.getConstraints());

		relBV.reportableSetBoolean(pop.isRelative(), this);
		if (pop.isRelative()) {
			relpopCH.setSelected(pop.getRelTarget());
			relpopSV.reportableSetString(String.format("%8.3g", pop.getRelFactor()), this);
		} else {
			relpopCH.setSelected(null);
		}

		syncRelDisplay();
		}
	}



	private DistribPopulation getFinalRelTargetPopulation() {
		DistribPopulation ret = null;
		String s = pop.getRelTarget();
		if (s != null) {
			ret = memController.getFinalRelTargetPopulation(s);
		}

		return ret;
	}

	private DistribPopulation getRelTargetPopulation() {
		DistribPopulation ret = null;
		String s = pop.getRelTarget();
		if (s != null) {
			ret = memController.getPopulation(s);
		}
		return ret;
	}




	public void syncRelDisplay() {

		boolean b = relBV.getBoolean();


		if (b) {
			DistribPopulation rp = getRelTargetPopulation();
			pop.setRelTargetPopulation(rp);

			DistribPopulation relpop = getFinalRelTargetPopulation();
			if (relpop != null) {
				densitySV.reportableSetString(relpop.getDensityExpression(), this);
				relpopCH.setSelected(relpop.getID());
			} else {
				densitySV.reportableSetString("", this);
				relpopCH.setSelected(null);
			}
			constraintsList.setItems(new String[0]);

		} else {
			if (pop != null) {
				densitySV.reportableSetString(pop.getDensityExpression(), this);
				constraintsList.setItems(pop.getConstraints());
			}
			relpopCH.clearSelection();
		}
		densitySV.setAble(!b);
		relpopSV.setAble(b);
	}




	public void setMorphologyController(MorphologyController mc) {
		morphController = mc;
	}

	public void setRootController(IcingController ic) {
		rootController = ic;
	}

	public void setMembraneController(MembraneController mc) {
		memController = mc;
	}


	public void setChannelIDs(String[] sa) {
		channelCH.setOptions(sa);
	}


	public void setPopulationIDs(String[] sa){
		relpopCH.setOptions(sa);
	}


	public void addConstraint() {
		pop.addConstraint();
		ArrayList<PopulationConstraint> cal = pop.getConstraints();
		constraintsList.setItems(cal);
		constraintsList.selectAt(cal.size()-1);
		editConstraint(cal.get(cal.size()-1));
		memController.populationChanged();
	}



	public void editConstraint(String s) {

		PopulationConstraint pc = null;
		for (PopulationConstraint c : pop.getConstraints()) {
			if (s.equals(c.toString())) {
				pc = c;
				break;
			}
		}
		if (pc != null) {
			editConstraint(pc);
		} else {
			E.warning("cant find constraint " + s);
		}
	}



	private void editConstraint(PopulationConstraint pc) {
		activeConstraint = pc;
		if (pc.isInclude()) {
			modeChoice.setSelected("i");
		} else if (pc.isExclude()) {
			modeChoice.setSelected("e");
		} else if (pc.isRestrict()){
			modeChoice.setSelected("r");

		} else {
			modeChoice.setSelected("r");
		}


		if (pc.isRegion()) {
			setCondition("");
			if (pc.isRegionProximal()) {
				conditionChoice.setSelected("p");
			} else if (pc.isRegionDistal()) {
				conditionChoice.setSelected("d");
			} else {
				conditionChoice.setSelected("l");
			}
			conditionSV.reportableSetString("", this);
			labelChoice.setSelected(pc.getRegion());


		} else {
			setCondition("w");
			conditionChoice.setSelected("w");
			conditionSV.reportableSetString(pc.getCondition(), this);
		}

	}





	public void setCondition(String s) {
		if (s.equals("w")) {
			labelChoice.able(false);
			conditionTF.able(true);

		} else {
			labelChoice.able(true);
			conditionTF.able(false);
		}
	}



	public void applyToConstraint() {

		if (activeConstraint == null) {
			activeConstraint = pop.newConstraint();
		}

		String sm = modeChoice.getSelected();
		String sc = conditionChoice.getSelected();
		String sl = labelChoice.getSelected();
		String se = conditionSV.getAsString();

		if (sm.equals("i")) {
			activeConstraint.setInclude();
		} else if (sm.equals("e")) {
			activeConstraint.setExclude();
		} else if (sm.equals("r")) {
			activeConstraint.setRestrict();
		} else {
			E.error("unrecognized " + sm);
		}

		String cond = "";
		if (sc.equals("p")) {
			cond = "region<" + sl;
		} else if (sc.equals("d")) {
			cond = "region>" + sl;
		} else if (sc.equals("l")) {
			cond = "region=" + sl;
		} else if (sc.equals("w")) {
			cond = se;
		} else {
			E.error("unrecognized " + sc);
		}
		activeConstraint.setCondition(cond);



		ArrayList<PopulationConstraint> cal = pop.getConstraints();
		constraintsList.setItems(cal);
		// constraintsList.selectAt(cal.indexOf(activeConstraint));
		clearConstraint();
		pop.flagChange();
		morphController.populationChanged(pop);
		memController.populationChanged();


	}


	public void clearConstraint() {
		conditionSV.reportableSetString("", this);
		constraintsList.setSelected(null);
		activeConstraint = null;
	}


	public void deleteConstraint() {
		if (pop != null && activeConstraint != null) {
			pop.removeConstraint(activeConstraint);
			constraintsList.setItems(pop.getConstraints());
		}
	}


	public void valueChangedBy(Value pv, Object src) {
		if (pv instanceof ColorValue) {
			String s = ((ColorValue)pv).getAsString();
			if (! s.equals(pop.getColor())) {
				pop.setColor(colorCV.getAsString());
				morphController.populationColorChanged(pop);
				memController.populationColorChanged(src);
			}
		}

		// readValues();
		// morphController.populationChanged(pop);
	}





	private void readValues() {
		if (pop != null) {

			boolean b = relBV.getBoolean();

			if (b) {
				double f = Double.parseDouble(relpopSV.getAsString());
				pop.setRelativeDensity(f, relpopCH.getSelected());
			} else {
				pop.setDensityExpression(densitySV.getAsString());
			}

			if (capDensityBV.getBoolean()) {
				String smd = maxDensitySV.getAsString();
				if (smd != null && smd.trim().length() > 0) {
					pop.setMaxDensity(Double.parseDouble(smd));
				} else {
					pop.setCapDensity(false);
				}
			} else {
				pop.setCapDensity(false);
			}


			if (fixtotBV.getBoolean()) {
				pop.setTotalNumber(Integer.parseInt(totnumSV.getAsString()));
			} else {
				pop.setFixTotal(false);
			}

			String sal = allocCH.getSelected();
			if (sal.equals("Poisson")) {
				pop.setAllocationPoisson();

			} else if (sal.equals("Regular")) {
				pop.setAllocationRegular();

			} else {
				E.error("unrecognized " + sal);
			}

			pop.setColor(colorCV.getAsString());
		}
	}

	public void apply() {
		readValues();
		morphController.populationChanged(pop);
		memController.populationChanged();
	}


	public void setMorphLabels(ArrayList<IcingLabel> pts) {
		String[] sa = new String[pts.size()];
		for (int i = 0; i < sa.length; i++) {
			sa[i] = pts.get(i).getText();
		}
		labelChoice.setOptions(sa);

	}


	public void setChannel(String s) {
		if (pop != null) {
			pop.setTypeID(s);
		}
	}


	public void reseed() {
		if (pop != null) {
			pop.reseed();
		}
	}

	@SuppressWarnings("unused")
	public void densityFocus(boolean b) {

	}

	@SuppressWarnings("unused")
	public void reldensityFocus(boolean b) {

	}

	@SuppressWarnings("unused")
	public void setRelativeDensity(boolean b) {
		syncRelDisplay();
	}


	public void upToDate() {
		updateButton.deSuggest();
	}


}
