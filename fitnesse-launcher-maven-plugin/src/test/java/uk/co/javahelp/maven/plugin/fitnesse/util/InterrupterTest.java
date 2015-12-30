package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InterrupterTest {

	private Interrupter interrupter1;
	
	private Interrupter interrupter2;
	
	@Before
	public void setUp() {
		interrupter1 = new Interrupter(Thread.currentThread(), 300);
		interrupter2 = new Interrupter(interrupter1, 200);
	}
	
	@After
	public void tearDown() {
		interrupter1.interrupt();
		interrupter2.interrupt();
   		try {
   			Thread.sleep(100);
	    } catch (InterruptedException e) {
		    // ignore
		}
	}
	
	@Test
	public void testInterruptCurrentThread() {
		
		long started = System.currentTimeMillis();
		
		interrupter1.start();
		
   		try {
   			Thread.sleep(400);
    		fail("Expecting to be interrupted!");
	    } catch (InterruptedException e) {
		    // ignore
		}
		
		long stopped = System.currentTimeMillis();
		
		assertTrue((stopped - started) < 400);
	}
	
	@Test
	public void testInterruptTheInterrupter() {
		
		long started = System.currentTimeMillis();
		
		interrupter1.start();
		interrupter2.start();
		
   		try {
   			Thread.sleep(400);
    		fail("Expecting to be interrupted!");
	    } catch (InterruptedException e) {
		    // ignore
		}
	    
		long stopped = System.currentTimeMillis();
		
		assertTrue((stopped - started) < 300);
	}
	
	@Test
	public void testNotInterrupted() {
		
		long started = System.currentTimeMillis();
		
		interrupter1.start();
		
   		try {
   			Thread.sleep(100);
	    } catch (InterruptedException e) {
    		fail("Was interrupted!");
		}
	    
		long stopped = System.currentTimeMillis();
		
		assertTrue((stopped - started) < 200);
	}
}
