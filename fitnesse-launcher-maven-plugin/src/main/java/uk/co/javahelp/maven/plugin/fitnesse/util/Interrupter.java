package uk.co.javahelp.maven.plugin.fitnesse.util;

public class Interrupter extends Thread {

	private final Thread threadToInterrupt;
	
	private final long howLongToWaitBeforeInterrupting;
		
	public Interrupter(final Thread threadToInterrupt, final long howLongToWaitBeforeInterrupting) {
		this.threadToInterrupt = threadToInterrupt;
		this.howLongToWaitBeforeInterrupting = howLongToWaitBeforeInterrupting;
	}

	@Override
	public final void run() {
		if(howLongToWaitBeforeInterrupting > 0L) {
    		try {
    			Thread.sleep(howLongToWaitBeforeInterrupting);
		    } catch (InterruptedException e) {
			    // ignore
			}
		}
		threadToInterrupt.interrupt();
	}
}
