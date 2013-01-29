package org.psics.model.stimrec;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.model.control.CommandConfig;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.phys.PhysicalQuantity;


public abstract class Profile<T extends PhysicalQuantity> implements AddableTo {



	@Identifier(tag = "profile id - only needed it if its parameters are to be modified")
	public String id;

	public T baseQ;

	int stepStyle = CommandProfile.MIDPOINT;

	TimeSeries timeSeries;

	boolean recordable = true;


	public String getID() {
		return id;
	}


	public abstract void add(Object obj);

	public abstract double getNonDimStart();

	public abstract ArrayList<ProfileFeature> getFeatures();


	public void setStepStyle(int iss) {
		stepStyle = iss;
	}


	public void setTimeSeries(TimeSeries ts) {
		timeSeries = ts;
	}

	public void setRecordable(boolean b) {
		recordable = b;
	}


	public CommandProfile makeCommand() {
		 CommandProfile cp = new CommandProfile();
		 cp.setStepStyle(stepStyle);
		 // cp.setUnit(CalcUnits.current);

	     double v = getNonDimStart();
		 cp.setStartValue(v);

		 for (ProfileFeature vpf : getFeatures()) {
			 vpf.exportTo(cp);
		 }
		 return cp;
	}


	public abstract PhysicalQuantity getBaseCalcValue();


	public CommandProfile[] makeCommands(ArrayList<CommandConfig> commands) {
		 CommandProfile[] cpa = new CommandProfile[commands.size()];
		 // cp.setUnit(CalcUnits.voltage);


		HashMap<String, Object> idHM = new HashMap<String, Object>();
		if (getID() != null) {
			idHM.put(getID(), this);
		}
		for (ProfileFeature pf : getFeatures()) {
			if (pf.getID() != null) {
				idHM.put(pf.getID(), pf);
			}
		}

		CommandProfile base = new CommandProfile();

		base.setStartValue(getNonDimStart());
		base.setStepStyle(stepStyle);

		if (timeSeries != null) {
			base.setTimeSeries(timeSeries.getNormalizedFlattenedData(getBaseCalcValue()));
		}

		 for (ProfileFeature cpf : getFeatures()) {
			 cpf.exportTo(base);
		 }

		 int nc = commands.size();
		 for (int i = 0; i < nc; i++) {
			 CommandConfig cconf = commands.get(i);

			 if (cconf == null) {
				 cpa[i] = base;

			 } else {
				 String vid = cconf.getTargetID();
				 if (idHM.containsKey(vid)) {
					 cconf.applyTo(idHM.get(vid));
					 CommandProfile cp = new CommandProfile();
					 cp.setStartValue(getNonDimStart());
					 cp.setStepStyle(stepStyle);

					 for (ProfileFeature cpf : getFeatures()) {
						 cpf.exportTo(cp);
					 }
					 cpa[i] = cp;


				 } else {
					 cpa[i] = base;
				 }

			 }
		 }

		 return cpa;
	}



}
