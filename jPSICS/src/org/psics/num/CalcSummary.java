package org.psics.num;


public class CalcSummary {

	public int mode;
	public String method;
	public double weightFactor;
	public int nrun;
	public double runtime;
	public double dt;
	public double cputime;
	public int ncompartments;
	public int nchannels;
	public int nchannels_stoch;
	public int nchannels_cont;
	public int nchannels_ng;

	public int ncompartments_stoch;
	public int ncompartments_cont;
	public int ncompartments_ng;

	public String filePattern;

	public String variableName;
	public String variableText;
	public double variableValue;

	//	public Discretization vdisc;



	public void setImplicitWeightFactor(double wf) {
		weightFactor = wf;
	}


	public void setNRun(int n) {
		 nrun = n;
	}


	@SuppressWarnings("unused")
	public void setVDiscretization(Discretization disc) {
		// vdisc = disc;
	}


	public String getFileRoot() {
		// E.info("returning filepattern for root? " + filePattern);
		return filePattern; // TODO - ? right thing
	}


	public void setRuntime(double xr) {
		runtime = xr;
	}


	public void setDt(double xdt) {
		dt = xdt;
 	}


	public void setCPUTime(double cput) {
		cputime = cput;
	}

	public void setNChannels(int nc) {
		nchannels = nc;
	}

	public void setNCompartments(int ncomp) {
		ncompartments = ncomp;
	}

	public void setNChannelsStoch(int n) {
		nchannels_stoch = n;
	}
	public void setNChannelsCont(int n) {
		nchannels_cont = n;
	}

	public void setNChannelsNg(int n) {
		nchannels_ng = n;
	}

	public void setNCompartmentsStoch(int n) {
		ncompartments_stoch = n;
	}
	public void setNCompartmentsCont(int n) {
		ncompartments_cont = n;
	}
	public void setNCompartmentsNg(int n) {
		ncompartments_ng = n;
	}

	public void setFilePattern(String fp) {
		filePattern = fp;

	}


	public void setVariableName(String vn) {
		variableName = vn;

	}
	public void setVariableValue(double v) {
		variableValue = v;

	}
	public void setVariableText(String vn) {
		variableText = vn;

	}


	public double getCPUTime() {
		return cputime;
	}

}
