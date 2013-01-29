package org.psics.model.channel;

public class ChannelComponents {


	public static Class<?>[] channelClasses = {
			KSChannel.class,
			ClosedState.class,
			OpenState.class,
			FixedRateTransition.class,
			VHalfTransition.class,
			VRateTransition.class,
			ExpLinearTransition.class,
			ExpTransition.class,
			SigmoidTransition.class,
			KSComplex.class,
			TauInfCodedTransition.class,
			TauInfTransition.class};


}
