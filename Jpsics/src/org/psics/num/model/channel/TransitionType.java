package org.psics.num.model.channel;


public enum TransitionType {
	EXP_LINEAR_ONE_WAY(TransitionTypes.EXP_LINEAR_ONE_WAY),
	FIXED_RATE(TransitionTypes.FIXED_RATE),
	BOLTZMANN_VDEP(TransitionTypes.BOLTZMANN_VDEP),
	BINDING(TransitionTypes.BINDING),
	EXP_ONE_WAY(TransitionTypes.EXP_ONE_WAY),
	SIGMOID_ONE_WAY(TransitionTypes.SIGMOID_ONE_WAY),
	CODED(TransitionTypes.CODED),
	FUNCTION(TransitionTypes.FUNCTION);


	int code;


	private TransitionType(int k) {
		code = k;
	}

	public int getCode() {
		return code;
	}




}
