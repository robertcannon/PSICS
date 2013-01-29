package org.psics.model.neuroml;

import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.model.channel.AlphaBetaCodedTransition;
import org.psics.model.channel.ExpLinearTransition;
import org.psics.model.channel.ExpTransition;
import org.psics.model.channel.KSTransition;
import org.psics.model.channel.SigmoidTransition;
import org.psics.model.channel.TauInfCodedTransition;



public class ChannelMLVoltageGate {

	public ChannelRateCurve alpha;

	public ChannelRateCurve beta;


	public ChannelRateCurve gamma;

	public ChannelRateCurve zeta;


	public ChannelRateCurve tau;

	public ChannelRateCurve inf;



	public KSTransition getForwardTransition() throws ImportException {
		KSTransition ret = null;
		if (isAlphaBeta()) {
			 if (isAlphaBetaParameterized()) {
				 ret = alpha.getKSTransition();
			 } else {
				 ret = makeAlphaBetaCodedTransition();
			 }

		} else if (isTauInf()) {
			ret = makeTauInfCodedTransition();

		} else {
			throw new ImportException("cant convert voltage gate (neither alpha-beta form nore tau-inf form)" + this);
		}
		return ret;
	}



	private boolean isAlphaBetaParameterized() {
		return (alpha.isParameterized() && beta.isParameterized());
	}



	private KSTransition makeTauInfCodedTransition() {
		TauInfCodedTransition ret = new TauInfCodedTransition();
		ret.setTauVar("tau");
		ret.setInfVar("inf");

		StringBuffer sb = new StringBuffer();
		sb.append(tau.getCodeLines("tau"));
		sb.append("\n");
		sb.append(inf.getCodeLines("inf"));

		ret.setCodeFragment(sb.toString());
		return ret;
	}


	private KSTransition makeAlphaBetaCodedTransition() {
		AlphaBetaCodedTransition ret = new AlphaBetaCodedTransition();
		ret.setAlphaVar("alpha");
		ret.setBetaVar("beta");

		StringBuffer sb = new StringBuffer();
		sb.append(alpha.getCodeLines("alpha"));
		sb.append("\n");
		sb.append(beta.getCodeLines("beta"));

		ret.setCodeFragment(sb.toString());
		return ret;
	}





	public KSTransition getReverseTransition() {
		 KSTransition ret = null;
		 if (isAlphaBeta()) {
			 if (isAlphaBetaParameterized()) {
				 ret = beta.getKSTransition();
			 } else {
				 // already done
			 }

		 } else if (isTauInf()) {
			 ret = null; // forward did it all;

		 } else {
			// OK to be quiet here as forward will have warned
			 //  E.error("cant convert voltage gate for psics: " + this);
		 }
		 return ret;
	}






	private int nNonNull() {
		int ret = 0;
		if (alpha != null) {
			ret += 1;
		}
		if (beta != null) {
			ret += 1;
		}
		if (gamma != null) {
			ret += 1;
		}
		if (zeta != null) {
			ret += 1;
		}
		if (tau != null) {
			ret += 1;
		}
		if (inf != null) {
			ret += 1;
		}
		return ret;
	}



	boolean isAlphaBeta() {
		boolean ret = false;
		if (alpha != null && beta != null && nNonNull() == 2) {
			ret = true;
		}
		return ret;
	}


	private boolean isTauInf() {
		boolean ret = false;
		if (tau != null && inf != null && nNonNull() == 2) {
			ret = true;
		}
		return ret;
	}



	public void populateFrom(KSTransition kst) {
		 if (kst instanceof SigmoidTransition) {
			 
			 
		 } else if (kst instanceof ExpTransition) {

			 
		 } else if (kst instanceof ExpLinearTransition) {

			 
		 } else {
			 E.warning("cant export: " + kst);
		 }
	}




}
