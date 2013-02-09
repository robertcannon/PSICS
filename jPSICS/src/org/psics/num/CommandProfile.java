package org.psics.num;

import java.util.ArrayList;
import java.util.TreeSet;

import org.psics.be.E;
import org.psics.util.TextDataWriter;



public class CommandProfile {

	public final static int MIDPOINT = 0;
	public final static int AVERAGE = 1;
	public final static int SAMPLED = 2;

	private double startValue;

	private ArrayList<ProfileEvent> events = new ArrayList<ProfileEvent>();

	EventQueue equeue = null;

	double currentTime;
	double currentValue;

	double noiseMean;
	double noiseAmplitude = Double.NaN;
	double noiseTimescale;
	int noiseSeed;

	int stepStyle;

	double[] tsData;

	public void setTimeSeries(double[] da) {
		tsData = da;
	}


	public void setStartValue(double v) {
		startValue = v;
	}

	public double getStartValue() {
		return startValue;
	}

	public void setStepStyle(int iss) {
		stepStyle = iss;
	}


	public void addRepeatingStep(double time, double value, double duration) {
		ProfileEvent pe = new RepeatingStep(time, value, duration);
		events.add(pe);
	}

	public void addStep(double time, double value) {
		ProfileEvent pe = new Step(time, value);
		events.add(pe);

	}

	public void addRepeatingBox(double time, double value, double duration, double repeat) {
		ProfileEvent pe = new RepeatingBox(time, value, duration, repeat);
		events.add(pe);
	}


	public void addBox(double time, double value, double duration) {
		ProfileEvent pe = new Box(time, value, duration);
		events.add(pe);
	}


	public void addNoise(double zm, double za, double zts, int sn) {
		noiseMean = zm;
		noiseAmplitude = za;
		noiseTimescale = zts;
		noiseSeed = sn;
	}


	private void initQueue() {
		equeue = new EventQueue();
		equeue.addAll(events);
		currentTime = 0.;
		currentValue = startValue;
	}



		// MUSTDO - use stepStyle to change evaluation behavor



	public double valueAt(double t) {
		if (equeue == null || t < currentTime) {
			initQueue();
		}
		equeue.advanceTo(t);
		return currentValue;
	}




	public double valueOver(double t, double dt) {
		double va = valueAt(t);
		double vb = valueAt(t + dt);
		double ret = vb;
		double tlt = equeue.lastTransition;
		if (tlt >= t) {
			ret = (tlt - t) / dt * va + (t + dt - tlt) / dt * vb;
		}
		return ret;
	}







	class EventQueue {
		double lastTransition = 0.;
		TreeSet<ProfileEvent> tset = new TreeSet<ProfileEvent>();

		void addAll(ArrayList<ProfileEvent> ape) {
			tset.addAll(ape);
		}

		void insert(ProfileEvent pe) {
			tset.add(pe);
		}


		void advanceTo(double time) {

			if (!Double.isNaN(noiseAmplitude)) {
				E.missing("need noise implementation for java!");
			}

			while (tset.size() > 0) {
				ProfileEvent pe = tset.first();
				if (pe.precedes(time)) {
					currentValue = pe.apply(this, currentValue);
					lastTransition = pe.getTime();
					tset.remove(pe);
				} else {
					break;
				}
			}
			currentTime = time;
		}



	}





	abstract class ProfileEvent implements Comparable<ProfileEvent> {

		double time;

		ProfileEvent(double t) {
			time = t;
		}

		public int compareTo(ProfileEvent pe) {
			int ret = 0;
			if (time < pe.time) {
				ret = -1;
			} else if (time > pe.time) {
				ret = 1;
			}
			return ret;
		}

		boolean precedes(double t) {
			return (time < t);
		}

		double getTime() {
			return time;
		}

		abstract double apply(EventQueue eq, double prev);

	}




	class Step extends ProfileEvent {
		double value;

	    Step(double t, double v) {
	    	super(t);
	    	value = v;
		}

	    double apply(EventQueue eq, double prev) {
	    	return value;
	    }

	}




	class DeltaStep extends ProfileEvent {
		double delta;

	    DeltaStep(double t, double v) {
	    	super(t);
	    	delta = v;
		}

	    double apply(EventQueue eq, double prev) {
	    	return prev + delta;
	    }

	}




	class RepeatingStep extends Step {
		double repeat;

		RepeatingStep(double t, double v, double r) {
			super(t, v);
			repeat = r;
		}

		double apply(EventQueue eq) {
			RepeatingStep rs = new RepeatingStep(time + repeat, value, repeat);
			eq.insert(rs);
			return value;
		}


	}




	class Box extends ProfileEvent {

		double value;
		double duration;

		Box(double t, double v, double d) {
			super(t);
			value = v;
			duration = d;
		}

		// POSERR is a box really a delta, or to a fixed val?
		double apply(EventQueue eq, double prev) {
			DeltaStep ds = new DeltaStep(time + duration, prev - value);
			eq.insert(ds);
			return value;
		}


	}



    class RepeatingBox extends Box {

		 double repeat;

		RepeatingBox(double t, double v, double d, double r) {
			super(t, v, d);
			repeat = r;
		}

		double apply(EventQueue eq, double prev) {
			DeltaStep ds = new DeltaStep(time + duration, prev - value);
			eq.insert(ds);
			RepeatingBox rb = new RepeatingBox(time + repeat, value, duration, repeat);
			eq.insert(rb);
			return value;
		}
	}



    public double[] getNoiseData() {
    	double[] ret = {};
    	if (!Double.isNaN(noiseAmplitude)) {
    		double[] a = {noiseMean, noiseAmplitude, noiseTimescale};
    		ret = a;
    	}
    	return ret;
    }



    public double[] getTVTEncoding() {
    	if (events == null || events.size() == 0) {
    		return new double[0];
    	}

    	ArrayList<double[]> bits = new ArrayList<double[]>();
    	double cv = startValue;



    	for (ProfileEvent pev : events) {
    		if (pev instanceof RepeatingStep) {
    			E.missing("repeating steps");
    		}

    		if (pev instanceof Step) {
    			cv = ((Step)pev).value;
    			double[] a = {pev.time, cv, 0};
    			bits.add(a);


    		} else if (pev instanceof DeltaStep) {
    			cv += ((DeltaStep)pev).delta;
    			double[] a = {pev.time, cv, 0};
    			bits.add(a);


    		} else if (pev instanceof Box) {
    			double cvpr = cv;
    			cv = ((Box)pev).value;
    			double[] a = {pev.time, cv, 0};
    			bits.add(a);
    			double[] ae = {pev.time + ((Box)pev).duration, cvpr, 0};
    			cv = cvpr;
    			bits.add(ae);

    			if (pev instanceof RepeatingBox) {
    				//repeats are encoded with a template copy of the repeated event(s) containing the
    				// time of the first repeat, the offset to the event it is repeating, and a code (10)
    				// indicating what it is
    				double r = ((RepeatingBox)pev).repeat;
    				double[] ras = {a[0] + r, 2, 10};
    				bits.add(ras);
    			}


   			} else {
    			E.missing("unhandled component in profile: " + pev);
    		}
    	}

    	double[] ret = new double[3 * bits.size()];
    	for (int i = 0; i < bits.size(); i++) {
    		double[] tvt = bits.get(i);
    		ret[3 * i] = tvt[0];
    		ret[3 * i + 1] = tvt[1];
    		ret[3 * i + 2] = tvt[2];
    	}

    	return ret;
    }

	public void appendTo(TextDataWriter tdw) {
		tdw.add(getStartValue());
		tdw.addMeta("start val, ");

		double[] dn = getNoiseData();
		tdw.addRowInts(dn.length, noiseSeed);
		tdw.addMeta("nnoise, seed, ");
		if (dn.length > 0) {
			tdw.addRow(dn);
			tdw.addMeta("noise params(" + dn.length + "), ");
		}


		if (tsData != null) {
			int npl = 20;
			tdw.addRowInts(CommandProfile.SAMPLED, tsData.length, npl);
			tdw.addMeta("step style, ntvt, n per line");
			for (int ia = 0; ia < tsData.length; ia += npl) {
				tdw.newRow();

				if (ia + npl <= tsData.length) {
					tdw.addRow(tsData, ia, ia + npl-1);
				} else {
					tdw.addRow(tsData, ia, tsData.length - 1);
				}
			}

		}

		else {

			double[] tvt = getTVTEncoding();
			tdw.addRowInts(stepStyle, tvt.length, tvt.length);
			tdw.addMeta("step style, ntvt, n per line");
			if (tvt.length > 0) {
				tdw.newRow();
				tdw.addRow(tvt);
				tdw.addMeta(" (time,val,type)*");
			}
		}
	}



}
