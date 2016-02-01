package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assume.assumeThat;

import java.io.ByteArrayOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util.FitNesseThreadLocator;
import com.processpuzzle.litest.rule.DefaultLocaleRule;

public abstract class MojoTest {
   static int PORT = 9123;
   static String PORT_STRING = Integer.toString( PORT );
   protected Log log;
   protected ByteArrayOutputStream logStream;
   @Rule public DefaultLocaleRule defaultLocaleRule = DefaultLocaleRule.en();
   @Rule public TestName testName = new TestName();

   @Before public void beforeEachTest() throws Exception{
      logStream = new ByteArrayOutputStream();
      log = PrintStreamLogger.createDefaultLog( logStream ); 
      log.debug( testName.getMethodName() + "-before" );
      assumeThat( new FitNesseThreadLocator( log ).findFitNesseServerThread(), nullValue() );            
   }
   
   @After public void afterEachTest() throws Exception{
      assumeThat( new FitNesseThreadLocator( log ).findFitNesseServerThread(), nullValue() );      
      log.debug( testName.getMethodName() + "-after" );
   }
}
