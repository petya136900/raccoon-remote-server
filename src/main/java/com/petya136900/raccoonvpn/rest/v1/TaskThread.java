package com.petya136900.raccoonvpn.rest.v1;

public class TaskThread {
	private Thread thread;
	private StringBuilder sb = new StringBuilder();
	private boolean done=false;
	private ActionSB action;
	public String getData() {
		return sb.toString();
	}
	public void stop() {
		if(thread!=null) {
			thread.interrupt();
		}
	}
	public TaskThread(ActionSB action) {
		this.action=action;
	}
	public void start() {
		if(thread==null) {
			thread = new Thread(()->{
				action.appendTo(sb);
				done=true;
			});
			thread.start();
		}
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
}
