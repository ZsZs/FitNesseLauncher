package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util;

import static com.processpuzzle.litest.matcher.TextContainsLine.containsLine;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.AbstractFitNesseMojo;
import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.PrintStreamLogger;

@RunWith( PowerMockRunner.class )
public class FitNesseHelperTest {
   private static final int DEFAULT_COMMAND_PORT = 9123;
   private FitNesseHelper fitNesseHelper;
   private ArtifactHandler artifactHandler;
   private ByteArrayOutputStream logStream;
   private Log log;
   @Rule public TestName testName = new TestName();

   @BeforeClass public static void beforeAllTests() {
      BasicConfigurator.configure();
      Logger.getRootLogger().setLevel( Level.INFO );
      Logger.getLogger( "org.eclipse.jetty" ).setLevel( Level.OFF );
   }

   @Before public void beforeEachTests() {
      System.out.println( testName.getMethodName() + "-before" );

      artifactHandler = mock( ArtifactHandler.class );

      logStream = new ByteArrayOutputStream();
      log = PrintStreamLogger.createDefaultLog( logStream );
      fitNesseHelper = new FitNesseHelper( log );

      assumeThat( new FitNesseThreadLocator( log ).findFitNesseServerThread(), nullValue() );
   }

   @After public void afterEachTest() {
      assumeThat( new FitNesseThreadLocator( log ).findFitNesseServerThread(), nullValue() );
      System.out.println( testName.getMethodName() + "-after" );
   }

   @Test public void formatAndAppendClasspath() {
      // Save the real os.name
      String os = System.getProperty( "os.name" );

      System.setProperty( "os.name", "windows" );
      // assertFormatAndAppendClasspath( "" );

      System.setProperty( "os.name", "linux" );
      assertFormatAndAppendClasspath( "[ERROR] THERE IS WHITESPACE IN CLASSPATH ELEMENT [/x/y z]" );
      assertFormatAndAppendClasspath( "FitNesse classpath may not function correctly in wiki mode" );

      // Restore the real os.name (to prevent side-effects on other tests)
      System.setProperty( "os.name", os );
   }

   @Test public void formatAndAppendClasspathArtifact() {
      String jarPath = new File( getClass().getResource( "/dummy.jar" ).getPath() ).getPath();
      Artifact artifact = new DefaultArtifact( "org.fitnesse", "fitnesse", "20130530", "compile", "jar", null, artifactHandler );
      artifact.setFile( new File( jarPath ) );

      StringBuilder sb = new StringBuilder();
      assertSame( sb, fitNesseHelper.formatAndAppendClasspathArtifact( sb, artifact ) );

      assertEquals( "!path " + jarPath + "\n", sb.toString() );
   }

   @SuppressWarnings( "unused" )
   @PrepareForTest( { FitNesseHelper.class } ) @Test public void forkFitNesseServer_launcherInSeparateProcess() throws Exception {
      String javaHome = System.getProperty("java.home");
      String javaClassPath = System.getProperty( "java.class.path" );
      PowerMockito.mockStatic( System.class );
      PowerMockito.when( System.getProperty( "java.home" ) ).thenReturn( javaHome );
      PowerMockito.when( System.getProperty( AbstractFitNesseMojo.MAVEN_CLASSPATH ) ).thenReturn( currentClassPath() );
      File logDir = new File( System.getProperty( "java.io.tmpdir" ), "fitnesse-launcher-logs" );
      String port = String.valueOf( DEFAULT_COMMAND_PORT );
      File working = new File( System.getProperty( "java.io.tmpdir" ), "fitnesse-launcher-test" );

      Process fitnesseProcess = fitNesseHelper.forkFitNesseServer( port, working.getCanonicalPath(), FitNesseHelper.DEFAULT_ROOT, logDir.getCanonicalPath(), javaClassPath );

      // TEAR DOWN:
      fitNesseHelper.shutdownFitNesseServer( port );
      //fitnesseProcess.destroy();
   }

   @Test public void launchFitNesseServer() throws Exception {
      File logDir = new File( System.getProperty( "java.io.tmpdir" ), "fitnesse-launcher-logs" );
      // Clean out logDir, as it might still exist from a previous run,
      // because Windows doesn't always delete this file on exit
      FileUtils.deleteQuietly( logDir );
      assertLaunchFitNesseServer( null );
      assertLaunchFitNesseServer( " " );
      assertLaunchFitNesseServer( logDir.getCanonicalPath() );
      String[] logFiles = logDir.list();
      assertEquals( 1, logFiles.length );
      assertTrue( logFiles[0].matches( "fitnesse[0-9]+\\.log" ) );
      FileUtils.forceDeleteOnExit( logDir );
   }

   @Test public void shutdownFitNesseServerOk() throws Exception {
      int port = DEFAULT_COMMAND_PORT;
      Server server = new Server( port );
      server.setHandler( new OkHandler( "/", "responder=shutdown" ) );
      server.start();

      try{
         fitNesseHelper.shutdownFitNesseServer( String.valueOf( port ) );
      }finally{
         server.stop();
      }
   }

   @Test public void shutdownFitNesseServerNotRunning() throws Exception {
      int port = DEFAULT_COMMAND_PORT;
      fitNesseHelper.shutdownFitNesseServer( String.valueOf( port ) );
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse already not running." ) );
   }

   @Test public void shutdownFitNesseServerDisconnect() throws Exception {
      int port = DEFAULT_COMMAND_PORT;
      Server server = new Server( port );
      server.setHandler( new DisconnectingHandler( server ) );
      server.start();

      try{
         fitNesseHelper.shutdownFitNesseServer( String.valueOf( port ) );

         assertThat( logStream.toString(), containsLine( "[ERROR]" ) );
         assertThat( logStream.toString(), containsLine( "java.io.IOException: Could not parse Response" ) );
      }finally{
         server.stop();
      }
   }
   
   @Test public void shutdownFitNesseServer_destroysSpawnedProcess(){
      fitNesseHelper.shutdownFitNesseServer( String.valueOf( DEFAULT_COMMAND_PORT ) );
   }
   
   // protected, private tes helper methods
   private void assertFormatAndAppendClasspath( String expectedLogMsg ) {
      StringBuilder sb = new StringBuilder();

      assertSame( sb, fitNesseHelper.formatAndAppendClasspath( sb, "/x/y/z" ) );
      assertEquals( "!path /x/y/z\n", sb.toString() );
      // assertEquals( "", logStream.toString() );

      assertSame( sb, fitNesseHelper.formatAndAppendClasspath( sb, "/x/y z" ) );
      assertEquals( "!path /x/y/z\n!path /x/y z\n", sb.toString() );
      assertThat( logStream.toString(), containsLine( expectedLogMsg ) );
   }

   private void assertLaunchFitNesseServer( String logDir ) throws Exception {
      String port = String.valueOf( DEFAULT_COMMAND_PORT );
      File working = new File( System.getProperty( "java.io.tmpdir" ), "fitnesse-launcher-test" );
      fitNesseHelper.launchFitNesseServer( port, working.getCanonicalPath(), FitNesseHelper.DEFAULT_ROOT, logDir );
      URL local = new URL( "http://localhost:" + port );
      InputStream in = local.openConnection().getInputStream();
      try{
         String content = IOUtils.toString( in );
         assertTrue( content.startsWith( "<!DOCTYPE html>" ) );
         assertTrue( content.contains( "<title>Page doesn't exist. Edit: FrontPage</title>" ) );
      }finally{
         IOUtils.closeQuietly( in );
         fitNesseHelper.shutdownFitNesseServer( port );
         Thread.sleep( 100L );
         FileUtils.deleteQuietly( working );
      }
   }

   private String currentClassPath() {
      String currentClassPath = "";
      ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
      URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

      for( int i = 0; i < urls.length; i++ ){
         currentClassPath += urls[i].getFile() + ";";
      }
      return currentClassPath;
   }
}
