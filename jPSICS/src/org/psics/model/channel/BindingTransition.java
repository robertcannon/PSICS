package org.psics.model.channel;

import org.psics.be.E;
import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Concentration;
import org.psics.quantity.phys.Rate;
import org.psics.quantity.phys.RateByConcentration;
import org.psics.quantity.units.Units;


@ModelType(standalone=false, usedWithin={KSChannel.class}, tag="Binding transition defined by the reverse rate and " +
		"either the forward rate or the dissocaition constant",
		info="A simple mass-action reaction involving the binding of a ligand to one state of a channel " +
				"produce another state. The reverse reaction, unbinding of the ligand is expressed in reactions per " +
				"unit time. For the forward transition either the forward rate (reactions per mole per unit time) or " +
				"the dissocaition constant must be specified.")
public class BindingTransition extends KSTransition {


	@Quantity(range = "(0.0001, 1000)", required = false, tag = "forward rate", units = Units.l_per_s_per_mol)
	public RateByConcentration forward;


	@Quantity(range = "", required = false,
			tag = "dissociation constant (concentration of ligand at which half the receptors are occupied)",
			units = Units.mol_per_l)
	public Concentration kd;


	@Quantity(units=Units.per_s, range = "(0.001, 1000)", required=true,
			tag="Rate of the unbinding reaction")
	public Rate reverse;



	public TransitionType getTransitionType() {
		return TransitionType.BINDING;
	}



	public double[] getTransitionData() {
		double[] ret = new double[4];


		if (forward != null && kd != null) {
			E.error("Either the forward rate or kd should be specified, not both, in " + this);
		} else if (forward == null && kd == null) {
			E.error("must have either kd or forward rate in " + this);
		} else if (forward != null) {
			ret[2] = forward.getValue(Units.l_per_s_per_mol); // MUSTDO right units
		} else {
			ret[2] = kd.getValue(Units.mol_per_l) / reverse.getValue(Units.per_ms); // MUSTDO
		}
		 ret[3] = reverse.getValue(Units.per_ms);


		 writeTempDependence(ret);

		 return ret;
	}


	 public String getExampleText() {
		 return "<BindingTransition kd=\"0.3mol_per_l\" reverse=\"1.3per_ms\"/> ||| " +
		 	"<BindingTransition forwar=\"0.23per_ms_per_mM\" reverse=\"1.3per_ms\"/>";
	 }


	public BindingTransition makeCopy(KSState sa, KSState sb) {
		 BindingTransition ret = new BindingTransition();
		 ret.setEnds(sa, sb);
		 ret.forward = forward.makeCopy();
		 if (kd != null) {
			 ret.kd = kd.makeCopy();
		 }
		 if (reverse != null) {
			 ret.reverse = reverse.makeCopy();
		 }
		 copyTemperatureTo(ret);
		 return ret;
	}



	@Override
	public BindingTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		BindingTransition ret = makeCopy(sa, sb);
		ret.forward.multiplyBy(ff);
		if (ret.reverse != null) {
			ret.reverse.multiplyBy(fr);
		} else {
			E.missing();
		}
		return ret;
	}

}
