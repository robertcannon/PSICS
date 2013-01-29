package org.psics.icing;



public class MorphBuilder implements Runnable {

	MorphologyController mc;

	boolean repprog = false;

	boolean tostop = false;
	boolean running = false;


	public MorphBuilder(MorphologyController c) {
		mc = c;
	}

	public boolean reportProgress() {
		return repprog;
	}


	public void setStop() {
		tostop = true;
	}

	public boolean shouldStop() {
		return tostop;
	}

	public boolean isRunning() {
		return running;
	}


	public void threadBuild() {
		repprog = true;
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}


	public void run() {
		running = true;
		mc.buildPopulations(this);
		running = false;
	}

	public void inplaceBuild() {
		repprog = false;
		run();
	}

}