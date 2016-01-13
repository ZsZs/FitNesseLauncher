package com.processpuzzle.maven.plugin.fitnesse.mojo;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.processpuzzle.maven.plugin.fitnesse.util.FitNesseThreadLocator;

public class VerifyMojoTest {
   private VerifyMojo mojo;
   private Log log;
   private ByteArrayOutputStream logStream;

   @Before
   public void setUp() throws IOException {
      mojo = new VerifyMojo();
      logStream = new ByteArrayOutputStream();
      log = PrintStreamLogger.createDefaultLog( logStream );
      mojo.setLog( log );
   }
   
   @After
   public void afterEachTest(){
      assertThat( new FitNesseThreadLocator( log ).findFitNesseServerThread(), nullValue() );      
   }

   @Test
   public void testSuccess() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-success.xml" ).getPath() );
      mojo.execute();
      assertEquals( "", logStream.toString() );
   }

   @Test
   public void testFailure() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-failure.xml" ).getPath() );
      try{
         mojo.execute();
         fail( "Expected MojoFailureException" );
      }catch( MojoFailureException e ){
         assertEquals( "There are test failures.\n\n" + String.format( "Please refer to %s for the individual test results.", mojo.reportsDir ), e.getMessage() );
      }
      assertEquals( "", logStream.toString() );
   }

   @Test
   public void testNoTests() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-no-tests.xml" ).getPath() );
      mojo.execute();
      assertEquals( "", logStream.toString() );
   }

   @Test
   public void testBadXml() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-bad-xml.xml" ).getPath() );
      try{
         mojo.execute();
         fail( "Expected " );
      }catch( MojoExecutionException e ){
         assertTrue( e.getMessage().startsWith( "org.xml.sax.SAXParseException" ) );
         assertTrue( e.getMessage().endsWith( "XML document structures must start and end within the same entity." ) );
      }
      assertEquals( "", logStream.toString() );
   }

   @Test
   public void testBadXml2() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-not-failsafe.xml" ).getPath() );
      try{
         mojo.execute();
         fail( "Expected MojoExecutionException" );
      }catch( MojoExecutionException e ){
         assertEquals( NullPointerException.class, e.getCause().getClass() );
      }
      assertEquals( "", logStream.toString() );
   }
}
