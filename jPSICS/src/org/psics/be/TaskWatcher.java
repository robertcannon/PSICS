package org.psics.be;


public interface TaskWatcher {

	public void taskAdvanced(double f, String msg);

	public void taskCompleted(String s);

}
