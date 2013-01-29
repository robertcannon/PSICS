package org.psics.num;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.psics.be.E;
import org.psics.geom.Ball;
import org.psics.geom.Geom;
import org.psics.geom.Position;
import org.psics.geom.Projector;
import org.psics.num.model.channel.ChannelSet;
import org.psics.num.model.channel.EnsembleChannelSet;
import org.psics.num.model.channel.NonGatedChannelSet;
import org.psics.num.model.channel.StochasticChannelSet;
import org.psics.num.model.channel.TableChannel;
import org.psics.num.model.synapse.SynapseSet;
import org.psics.num.model.synapse.TableSynapse;
import org.psics.util.TextDataWriter;

public class Compartment {

	String id;

	String partof;
	HashSet<String> labels = new HashSet<String>();
	int sourceSequenceNumber; // only guarantee here is that parents are lower than children;


	HashMap<String, CompartmentChannelPopulation> ccpHM = new HashMap<String, CompartmentChannelPopulation>();
	
	HashMap<String, CompartmentSynapsePopulation> cspHM = new HashMap<String, CompartmentSynapsePopulation>();
	Ball bcenter; // spherical ball or branch point;

	Ball[] section;  // a section

	Ball[][] arms; // arms around a branch point;

	int[] srcPts; // the points in the original tree that ended up in this cpt;

	double work;
	double[] workData = new double[2];
	boolean workFlag;

	double volume;
	double area;
	double length;

	// quantities for channel allocation
	double r;
	double d; // diameter
	double p; // path length from soma;
	int bo;

	double capacitance;


	int ncon;
	CompartmentConnection[] connections = new CompartmentConnection[3];

	CompartmentConnection[] preCon;
	CompartmentConnection[] postCon;


	// local state while traversing the tree (alternative to deep recursion)
	public boolean wkFlag;
	public Compartment wkCpt;



	private int index;

	public boolean clamped = false;
	public double clampValue;
	public double appliedCurrent;


	public double v;
	public double rhs;
	public double diag;

	public double cnInc;

	double gChan;
	double eChan;

	public double appliedConductance;


	public double appliedDrive;


	int nchanset;
	ChannelSet[] channelSets;

	SynapseSet[] synapseSets;
	
	boolean soughtParent;
	Compartment parent;
	Compartment[] children;


	double[][] cachedBoundary;

	double sectionLength = -1;
	double[] sectionPartLengths;
	Ball[][] sectionList;


	Ball[] proxPts = new Ball[2];
	double[][] proxBoundary = null;
	double proxPathLength;


	boolean squareCaps;


	public Compartment(boolean sq) {
		squareCaps = sq;
	}


	public String toString() {
		return (" cpt: " + id + " center=" + bcenter + " sections=" + section + " arms=" + arms +
				" area=" + area + " vol=" + volume);
	}

	public void setID(String s) {
		id = s;
		labels.add(id);
	}

	public void setIndex(int ind) {
		index = ind;
	}

	public int getIndex() {
		return index;
	}

	public String getID() {
		return id;
	}

	public String getNonNullID() {
		// if no ID set yet, create one;
		if (id == null) {
			id = "_cpt" + index;
		}
		return id;
	}


	public double[][] getProxBoundary() {
		return proxBoundary;
	}

	public Ball getProxPoint() {
		Ball ret = null;
		if (proxPts != null) {
			ret = proxPts[0];
		}
		return ret;
	}


	public HashSet<String> getLabels() {
		return labels;
	}


	public void orderConnections() {
		int npre = 0;
		int npost = 0;
		boolean[] ispre = new boolean[ncon];
		for (int i = 0; i < ncon; i++) {
			if (connections[i].otherIndex(this) < index) {
				npre += 1;
				ispre[i] = true;
			} else {
				npost += 1;
				ispre[i] = false;
			}
		}


		preCon = new CompartmentConnection[npre];
		postCon = new CompartmentConnection[npost];
		int ipre = 0;
		int ipost = 0;
		for (int i = 0; i < ncon; i++) {
			CompartmentConnection cc = connections[i];
			if (ispre[i]) {
				preCon[ipre++] = cc;
				cc.orderTo(this);
			} else {
				postCon[ipost++] = cc;
				cc.orderFrom(this);
			}
		}
	}



	public void setMembraneCapacitance(double c) {
		capacitance = c * area;
	}


	void setAppliedCurrent(double d) {
		appliedCurrent = d;
	}

	void setClampVoltage(double d) {
		if (Math.abs(d - clampValue) > 0.1) {
			// E.info("clamp voltage changed to " + d);
		}
		clamped = true;
		clampValue = d;
		v = d;
	}

	double getClampCurrent() {
		return gChan * (eChan - clampValue);
	}


	public void setPartOf(String s) {
		partof = s;
	}

	public void addLabel(String s) {
		labels.add(s);
	}


	public boolean labelledWith(String lbl) {
		return labels.contains(lbl);
	}


	public double getArea() {
		return area;
	}


	public void addChannels(String channelID, int nch) {
		if (ccpHM.containsKey(channelID)) {
			ccpHM.get(channelID).add(nch);
		} else {
			CompartmentChannelPopulation ccp = new CompartmentChannelPopulation(channelID, nch);
			ccpHM.put(ccp.getChannelID(), ccp);
		}
	}
	
	
	public void addSynapses(String sypopID, String synapseID, int nch) {
		// E.info("adding syns for " + sypopID + " " + nch + " " + getIndex());
		if (cspHM.containsKey(sypopID)) {
			cspHM.get(sypopID).add(nch);
		} else {
			CompartmentSynapsePopulation csp = new CompartmentSynapsePopulation(sypopID, synapseID, nch);
			cspHM.put(sypopID, csp);
		}
	}
	


	public void addChannelIDsIfNew(HashSet<String> hset) {
		hset.addAll(ccpHM.keySet());
	}


	public boolean isSpherical() {
		return (bcenter != null && arms == null);
	}

	public void setSpherical(Ball b) {
		bcenter = b;
		double ar = b.getRadius();
		volume = 4. / 3. * Math.PI * ar * ar * ar;
		area = 4. * Math.PI * ar * ar;
		srcPts = new int[1];
		srcPts[0] = b.getWork();
	}



	public void setTerminal(ArrayList<Ball> sec) {
		section = sec.toArray(new Ball[sec.size()]);
		length = Geom.sectionLength(section);
		srcPts = getWorks(section);

		proxPts[0] = sec.get(0);
		proxPts[1] = sec.get(1);
		makeProxBoundary();

		Ball be = section[section.length-1];
		if (squareCaps) {
			// ignore the area
		} else {
			incrementArea(0.5 * (4. * Math.PI * be.getRadius() * be.getRadius()));
		}

	}


	public void setRootSegment(ArrayList<Ball> sec) {
		section = sec.toArray(new Ball[sec.size()]);
		length = Geom.sectionLength(section);
		srcPts = getWorks(section);

		proxPts[0] = sec.get(0);
		proxPts[1] = sec.get(1);
		makeProxBoundary();
	}



	public void setSection(ArrayList<Ball> sec) {
		section = sec.toArray(new Ball[sec.size()]);
		srcPts = getWorks(section);
		length = Geom.sectionLength(section);
		proxPts[0] = sec.get(0);
		proxPts[1] = sec.get(1);
		makeProxBoundary();
	}

	private int[] getWorks(Ball[] ba) {
		int[] ret = new int[ba.length];
		for (int i = 0; i < ba.length; i++) {
			ret[i] = ba[i].getWork();
		}
		return ret;
	}




	public int[] getSourcePoints() {
		return srcPts;
	}

	
	public void setBranchPoint(Ball b, ArrayList<ArrayList<Ball>> aarms, Ball[] prox) {
		bcenter = b;
		proxPts = prox;
		makeProxBoundary();
		int np = 1;
		arms = new Ball[aarms.size()][];
		for (int i = 0; i < arms.length; i++) {
			arms[i] = aarms.get(i).toArray(new Ball[0]);
			np += arms[i].length;
		}

		srcPts = new int[np];
		srcPts[0] = b.getWork();
		np = 1;
		for (int i = 0; i < arms.length; i++) {
			for (int j = 0; j < arms[i].length; j++) {
				srcPts[np] = arms[i][j].getWork();
				np += 1;
			}
		}

		double av = 0.;
		for (Ball[] arm : arms) {
			av += Geom.sectionLength(b, arm);
		}
		length = 2./ 3. * av;
		// POSERR - not quite, but close enough
	}





	public double getLength() {
		return length;
	}


	public void makeProxBoundary() {
		if (proxPts == null || proxPts.length == 0 || proxPts[0] == null) {
			// this should be OK - TODO - could check it only occurs once on tree
			// E.warning("no proximal points? ");
			proxPathLength = 0.;
		} else {
			proxPathLength = proxPts[0].getRWork();
			if (proxPathLength < 0) {
				E.error("compartment " + index + " has prox " + proxPathLength);
			}
		}
		// makes a disc perpendicular to the structure at proximal end
		/*
		if (proxPts[0] != null) {
			proxBoundary = Geom.perpCirc(proxPts[0], proxPts[1]);
		}
		*/
	}


	public double getProxPathLength() {
		return proxPathLength;
	}



	


	public double[][] getCachedBoundary() {
		if (cachedBoundary == null) {
			cachedBoundary = new double[2][0];
		}
		return cachedBoundary;
	}


	public double[] randomInternalPoint(double r1, double r2) {
		double x = 0.;
		double y = 0.;

		if (isSpherical()) {
			double phi = 2. * Math.PI * r1;
			double rr = Math.sqrt(r2);
			x = bcenter.getX() + rr * Math.cos(phi);
			y = bcenter.getY() + rr * Math.sin(phi);

		} else {
			if (sectionList == null) {
				if (section != null) {
					int ns = section.length-1;
					sectionList = new Ball[ns][2];
					for (int i = 0; i < ns; i++) {
						sectionList[i][0] = section[i];
						sectionList[i][1] = section[i+1];
					}
				} else {
					int ns = 0;
					for (Ball[] baa : arms) {
						ns += baa.length;
					}
					sectionList = new Ball[ns][2];
					int iseg = 0;
					for (Ball[] baa : arms) {
						sectionList[iseg][0] = bcenter;
						sectionList[iseg][1] = baa[0];
						iseg += 1;

						for (int j = 0; j < baa.length-1; j++) {
							sectionList[iseg][0] = baa[j];
							sectionList[iseg][1] = baa[j+1];
							iseg += 1;
						}
					}
				}

				int ns = sectionList.length;
				sectionLength = 0.;
				sectionPartLengths = new double[ns];
				for (int i = 0; i < ns; i++) {
					double dseg = Geom.xyDistanceBetween(sectionList[i][0], sectionList[i][1]); // TODO projection
					sectionLength += dseg;
					sectionPartLengths[i] = dseg;
				}
			}

			double fl = sectionLength * r1;
			int isec = 0;
			while (fl > sectionPartLengths[isec]) {
				fl -= sectionPartLengths[isec];
				isec += 1;
			}
			double f = fl / sectionPartLengths[isec];
			Ball ba = sectionList[isec][0];
			Ball bb = sectionList[isec][1];
			double ax = ba.getX();
			double ay = ba.getY();
			double bx = bb.getX();
			double by = bb.getY();

			double vx = (bx - ax) / sectionPartLengths[isec];
			double vy = (by - ay) / sectionPartLengths[isec];
			double rf = 2. * (r2 - 0.5) * (f * bb.getRadius() + (1. - f) * ba.getRadius());
			x = f * bx + (1. - f) * ax - rf * vy;
			y = f * by + (1. - f) * ay + rf * vx;

		}

		double[] ret = new double[2];
		ret[0] = x;
		ret[1] = y;
		return ret;
	}




	public void cacheProjectedBoundary(Projector proj) {
		Position[] pos =  makeBoundary(proj);
		 
		
		int np = pos.length;
		cachedBoundary = new double[2][np];
		for (int i = 0; i < np; i++) {
			Position pcb = pos[i];
			cachedBoundary[0][i] = pcb.getX();
			cachedBoundary[1][i] = pcb.getY();
		}
	}

	
	
	public Position[] makeBoundary(Projector proj) {
		Position[] ret = null;
		if (isSpherical()) {
			ret = Geom.makeBallBoundary(bcenter, proj);
		} else if (section != null) {
			if (squareCaps) {
				ret = Geom.makeSquareSectionBoundary(section, proj);
			} else {
				ret = Geom.makeSectionBoundary(section, proj);
			}
		} else {
			if (squareCaps) {
				ret = Geom.squareStarfishBoundary(bcenter, arms, proj);
			} else {
				ret = Geom.starfishBoundary(bcenter, arms, proj);
			}
		}
		return ret;
	}

	public void addConnection(CompartmentConnection con) {
		 if (ncon >= connections.length) {
			 // need more space - repeted use only if adding lots of spines
			 int nnew = (3 * ncon) / 2 + 4;

			 CompartmentConnection[] wcon = new CompartmentConnection[nnew];
			 System.arraycopy(connections, 0, wcon, 0, ncon);
			 connections = wcon;
		 }

		 connections[ncon] = con;
		 ncon += 1;

	}

	public void incrementArea(double dinc) {
		area += dinc;
	}


	public ArrayList<Compartment> getNeighbors() {
		ArrayList<Compartment> ret = new ArrayList<Compartment>();
		for (int i = 0; i < ncon; i++) {
			ret.add(connections[i].getOther(this));
		}
		return ret;
	}




 
	public void allocateChannels(HashMap<String, TableChannel> channelHM) {
		channelSets = new ChannelSet[ccpHM.size()];
		int ics = 0;
		for (CompartmentChannelPopulation ccp : ccpHM.values()) {
			String sid = ccp.getChannelID();
			if (channelHM.containsKey(sid)) {
				TableChannel tch = channelHM.get(sid);


				// to use the alt form:
				TableChannel altch = tch.getAlt();
				if (altch != null) {
					// TODO - make this an option from the top level
				    // tch = altch;
				}

				channelSets[ics] = tch.makeChannelSet(ccp.getNChan());


			} else {
				E.error("unknown channel typ " + sid);
			}
			ics += 1;
		}
	}
	

	public void allocateSynapses(HashMap<String, TableSynapse> synapseHM) {
		
		synapseSets = new SynapseSet[cspHM.size()];
		int ics = 0;
		for (CompartmentSynapsePopulation csp : cspHM.values()) {
			String sid = csp.getChannelID();
			if (synapseHM.containsKey(sid)) {
				TableSynapse tsy = synapseHM.get(sid);
				synapseSets[ics] = tsy.makeSynapseSet(csp.getNSynapse());


			} else {
				E.error("unknown channel typ " + sid);
			}
			ics += 1;
		}
	}
	
	
	
	
	
	

	public void instantiateChannels() {
		for (ChannelSet cs : channelSets) {
			cs.instantiateChannels(v);
		}
	}



	public void advanceChannels() {
		eChan = 0;
		gChan = 0;

		for (ChannelSet cset : channelSets) {
			 cset.advance(v);

			 double g = cset.getGEff();
			 double e = cset.getEEff();
			 eChan += g * e;
			 gChan += g;


			   //      gs[ich] = cs.getGEff();
			   //      es[ich] = cs.getEEff();
		}
		if (gChan > 0.) {
			eChan /= gChan;
		}
	}


	public void printAdvance() {
		eChan = 0;
		gChan = 0;

		for (ChannelSet cset : channelSets) {
			 cset.advance(v);

			 double g = cset.getGEff();
			 double e = cset.getEEff();
			 eChan += g * e;
			 gChan += g;
		}
		if (gChan > 0.) {
			eChan /= gChan;
		}
	}



	public void exportVariables(HashMap<String, Double> varHM) {
		varHM.put("r", new Double(r));
		varHM.put("d", new Double(d));
		varHM.put("p", new Double(p));
		varHM.put("bo", new Double(bo)); // POSERR bo is an int;
		varHM.put("radius", new Double(r));
		varHM.put("diameter", new Double(d));
		varHM.put("path", new Double(p));
		varHM.put("order", new Double(bo)); // POSERR bo is an int;
	}




	public double getRadius() {
		double ret = 0.;
		if (bcenter != null) {
			ret = bcenter.getRadius();
		} else {
			ret = section[0].getRadius();
		}
		return ret;
	}


	public void setRootMetrics() {
		r = getRadius();
		d = 2 * r;
		p = 0.;
		bo = 0;
	}


	public void setMetrics(Compartment par) {
		r = getRadius();
		d = 2 * r;
		bo = par.bo;
		if (arms != null) {
			bo += 1;
		}

		p = par.p + 0.5 * par.getLength() +  0.5 * getLength(); // todo - diff for terminals?
	}


	public double getPathLength() {
		return p;
	}


	public int getNChannels() {
		int nc = 0;
		for (CompartmentChannelPopulation ccp : ccpHM.values()) {
			nc += ccp.getNChan();
		}
		return nc;
	}

	@SuppressWarnings("boxing")
	   public String getAsText() {
	      StringBuffer sb = new StringBuffer();
	     // export boundary if have it, ow just the center point;

	     Position[] boundary = makeBoundary(null);

	         for (Position ap : boundary) {
	            sb.append(String.format(" (%.5g %.5g %.5g) ", ap.getX(), ap.getY(), ap.getZ()));
	         }

	      return sb.toString();
	   }


	public void addLabels(Collection<String> lbls) {
		if (lbls != null) {
			labels.addAll(lbls);
		}
	}


	public void setSourceTreeSequenceNumber(int tsno) {
		 sourceSequenceNumber = tsno;

	}




	// EFF - this sets each one twice;
	public void setResistivity(double res) {
		 for (int i = 0; i < ncon; i++) {
			 connections[i].setResistivity(res);
		 }
	}


	public void appendTo(TextDataWriter tdw, HashMap<String, Integer> channelNumIDs,
			HashMap<String, Integer> sypopNumIDs) {
		int ncp = ccpHM.size();
		int nsp = cspHM.size();
		
		int[] icp = new int[4 + 2 * ncp + 2 * nsp];
		icp[0] = index;
		icp[1] = ncon;
		icp[2] = ncp;
		icp[3] = nsp;

		int io = 4;
		for (CompartmentChannelPopulation ccp : ccpHM.values()) {
			String sid = ccp.getChannelID();
			icp[io] = channelNumIDs.get(sid);
			icp[io + 1] = ccp.getNChan();
			io += 2;
		}
		for (CompartmentSynapsePopulation csp : cspHM.values()) {
			String sid = csp.getPopulationID();
			icp[io] = sypopNumIDs.get(sid);
			icp[io + 1] = csp.getNSynapse();
			io += 2;
		}
		
		tdw.addInts(icp);
		tdw.addEcoMeta("index, ncon, nchpop, nsynpop, (chantype, nchan)*nchpop, (syntype, nsyn)*nsynpop");

		Ball b = bcenter;
		if (b == null) {
			 b = section[0]; // TODO get the cofg
		}
		// tdw.add(volume, capacitance, b.getX(), b.getY(), b.getZ());
		// tdw.addEcoMeta("volume, capacitance, x, y, z");

		int[] inbr = new int[ncon];
		double[] dnbr = new double[ncon];
		for (int i = 0; i < ncon; i++) {
			inbr[i] = connections[i].otherIndex(this);
			dnbr[i] = connections[i].getConductance();
		}
		tdw.addInts("    ", inbr);
		tdw.addRow(dnbr);
		tdw.addRow(volume, capacitance, b.getX(), b.getY(), b.getZ());
		tdw.addEcoMeta("neighbours, conductances to nieghbours, v capacitance, x, y, z");
	}

	
	
	
	

	public int getNStochasticChannels() {
		 int n = 0;
		 for (ChannelSet cset : channelSets) {
			 if (cset instanceof StochasticChannelSet) {
				 n += cset.getNChan();
				// StochasticChannelSet scs = (StochasticChannelSet)cset;
				// E.info("stoch channel set " + cset.getNChan() + " " + scs.getTable().getID());
			 }
		 }
		 return n;
	}


	public int getNContinuousChannels() {
		 int n = 0;
		 for (ChannelSet cset : channelSets) {
			 if (cset instanceof EnsembleChannelSet) {
				 n += cset.getNChan();
			 }
		 }
		 return n;
	}


	public int getNNonGatedChannels() {
		 int n = 0;
		 for (ChannelSet cset : channelSets) {
			 if (cset instanceof NonGatedChannelSet) {
				 n += cset.getNChan();
			 }
		 }
		 return n;
	}



	private double getNetCurrent(double vtgt, HashMap<String, ChannelGE> geHM) {
		double cur = 0;
		for (Map.Entry<String, CompartmentChannelPopulation>  me : ccpHM.entrySet()) {
			ChannelGE aGE = geHM.get(me.getKey());
			CompartmentChannelPopulation ccp = me.getValue();
			if (aGE == null) {
				// could be serious, could be harmless: harmless if a channel balance has been
				// specified for channels that aren't used
				
				//	E.fatalError("null aGE for " + me.getKey());

			} else {
				cur += ccp.getNChan() * aGE.g * (aGE.e - vtgt);
			}
		}
		return cur;
	}

	/*
	private int ranRound(double x, MersenneTwister mersenne) {
		int n = (int)Math.floor(x);
		double rem = x - n;
		if (mersenne.random() < rem) {
			n += 1;
		}
		return n;
	}
*/

	public void zeroWorkData() {
		workData[0] = 0;
		workData[1] = 0;
	}
	

	public boolean calculateBalanceNumbers(double vtgt, String ch1, String ch2,
				HashMap<String, ChannelGE> geHM) {
		zeroWorkData();
		boolean hasBoth = false;
		if (ccpHM.containsKey(ch1) && ccpHM.containsKey(ch2)) {
			hasBoth = true;
		} else {
			hasBoth = false;
		}
		
		
		if (!ccpHM.containsKey(ch1)) {
			CompartmentChannelPopulation ccp = new CompartmentChannelPopulation(ch1, 0);
			ccpHM.put(ccp.getChannelID(), ccp);
		}

		if (!ccpHM.containsKey(ch2)) {
			CompartmentChannelPopulation ccp = new CompartmentChannelPopulation(ch2, 0);
			ccpHM.put(ccp.getChannelID(), ccp);
		}
		
		if (hasBoth) {
			double cur = getNetCurrent(vtgt, geHM);

			workData[0] = 0.;
			workData[1] = 0.;
			if (Math.abs(cur) > 1.e-7) {
			CompartmentChannelPopulation ccp1 = ccpHM.get(ch1);
			CompartmentChannelPopulation ccp2 = ccpHM.get(ch2);

			ChannelGE ge1 = geHM.get(ch1);
			ChannelGE ge2 = geHM.get(ch2);

			double det = (ge2.e - ge1.e);
			double dg1 = cur / det;
			double dg2 = -cur / det;

			workData[0] = ccp1.getNChan() + dg1 / ge1.g;
			workData[1] = ccp2.getNChan() + dg2 / ge2.g;
			
			/*
			E.log("wkdats " + ccp1.getNChan() + " " + ccp2.getNChan() + " " + workData[0] + " " + workData[1] + 
					" " + cur + " " +  id);
			*/
			if (workData[0] < 0 || workData[1] < 0) {
				E.info("need negative number of channels at " + id + " " + workData[0] + " " + workData[1]);
				
				for (Map.Entry<String, CompartmentChannelPopulation>  me : ccpHM.entrySet()) {
					ChannelGE aGE = geHM.get(me.getKey());
					CompartmentChannelPopulation ccp = me.getValue();
					E.info("current nch " + ccp.getChannelID() + " " + ccp.getNChan());
				}
			}
			}
		}

			// the following applied to local balance, but now we do it by a neighbor averaging process
			/*
			if (g1 + dg1 < 0) {
				cur -= ccp1.getNChan() * ge1.g * (ge1.e - vtgt);
				ccp1.zero();
				int n2 = ranRound(-cur / (ge2.e - vtgt) / ge2.g, mersenne);
				ccp2.add(n2);

			} else if (g2 + dg2 < 0) {
				cur -= ccp1.getNChan() * ge2.g * (ge2.e - vtgt);
				ccp2.zero();
				int n1 = ranRound(-cur / (ge1.e - vtgt) / ge1.g, mersenne);
				ccp1.add(n1);

			} else {
				int n1 = ranRound(dg1 / ge1.g, mersenne);
				int n2 = ranRound(dg2 / ge2.g, mersenne);
				ccp1.add(n1);
				ccp2.add(n2);
			}
	*/
		return hasBoth;
	}


	public void checkBalance(double vtgt, HashMap<String, ChannelGE> geHM) {
			double cpost = getNetCurrent(vtgt, geHM);

			if (Math.abs(cpost / capacitance) > 2) {
				E.warning("Large residual current after channel balance: " +
						 cpost + "  capacitance=" + capacitance);
			}
	}


	public void toWork(int i) {
		work = workData[i];
	}


	public void setChannelsFromWork(String ch) {
		int nch = (int)(Math.round(work));
		if (ccpHM.containsKey(ch)) {
			ccpHM.get(ch).setNChan(nch);
		} else {
			CompartmentChannelPopulation ccp = new CompartmentChannelPopulation(ch, 0);
			ccpHM.put(ccp.getChannelID(), ccp);
		}
	}




	public void setWork(double d) {
		work = d;
	}

	public double getWork() {
		return work;
	}

	public void incrementWork(double dw) {
		work += dw;
	}

	public boolean isTerminal() {
		return (ncon == 1);
	}

	public Compartment getParent() {
		if (!soughtParent) {
			soughtParent = true;
			parent = null;
			for (int i = 0; i < ncon; i++) {
				if (connections[i].otherIndex(this) < index) {
					if (parent != null) {
						E.error("Two parents for compartment ? " + this);
					} else {
						parent = connections[i].getOther(this);
					}
				}
			}
			if (parent == null) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < ncon; i++) {
					sb.append(" nbr " + connections[i].otherIndex(this));
				}
				// E.info("pt with no parent: " + index + " "  + ncon + " " + sb.toString());
			}
		}
		return parent;
	}


	public boolean isBranchPoint() {
		 return (getChildren().length > 1);
	}


	public boolean isSegmentPoint() {
		 return (getChildren().length == 1);
	}


	public Compartment[] getChildren() {
		if (children == null) {
			ArrayList<Compartment> ac = new ArrayList<Compartment>();
			for (int i = 0; i < ncon; i++) {
				if (connections[i].otherIndex(this) > index) {
					ac.add(connections[i].getOther(this));
				}
			}
			children = ac.toArray(new Compartment[ac.size()]);
		}
		return children;
	}



	public boolean hasUnflaggedChild() {
		boolean ret = false;
		Compartment[] ca = getChildren();
		for (int i = 0; i < ca.length; i++) {
			if (children[i].wkFlag) {

			} else {
				ret = true;
			}
		}
		return ret;
	}


	public boolean hasParent() {
		boolean ret = true;
		if (parent == null) {
			ret = false;
		}
		return ret;
	}


}
