package uk.co.javahelp.maven.plugin.fitnesse.main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;
import uk.co.javahelp.maven.plugin.fitnesse.mojo.PrintStreamLogger;
import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class FitNesseMainTest {
	
	private static final String PORT = "9124"; // Using default port disturbs other tests for some reason

	@After
	public void tearDown() {
		ByteArrayOutputStream logStream = new ByteArrayOutputStream();
		Log log = new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream)));
       	new FitNesseHelper(log).shutdownFitNesseServer(PORT);
	}
	
	@Ignore
	@Test
	public void testLaunchFailure() throws Exception {
      ArrayList<String> commandLineArguments = Lists.newArrayList( "-e", "0", "-o", "-p", String.valueOf( PORT ) );
      Arguments arguments = new Arguments( commandLineArguments.toArray( new String[commandLineArguments.size()] ));

      FitNesseMain fitnesseMain = new FitNesseMain();
		Assert.assertNotNull(fitnesseMain);
		try {
	      fitnesseMain.launchFitNesse( arguments );
			Assert.fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			Assert.assertEquals("FitNesse could not be launched", e.getMessage());
		}
	}
}
