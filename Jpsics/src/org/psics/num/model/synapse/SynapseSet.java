package org.psics.num.model.synapse;





public interface SynapseSet {

	public void advance(double v);

	public double getGEff();

	public double getEEff();

	public String numinfo(double v);

	public int getNChan();

	public void instantiateChannels(double v);

}
