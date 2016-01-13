package com.processpuzzle.maven.plugin.fitnesse.mojo;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.processpuzzle.maven.plugin.fitnesse.util.FitNesseHelper;
import com.processpuzzle.maven.plugin.fitnesse.util.FitNesseThreadLocator;
import com.processpuzzle.maven.plugin.fitnesse.util.Interrupter;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.plugins.PluginException;

public class WikiMojoTest {
   static int PORT = 9123;
   static String PORT_STRING = Integer.toString( PORT );
   private WikiMojo mojo;
   private FitNesseHelper fitNesseHelper;
   private FitNesse fitNesse;
   private ByteArrayOutputStream logStream;
   private Log log;

   @Before
   public void setUp() throws IOException, PluginException {
      fitNesseHelper = mock( FitNesseHelper.class );

      ContextConfigurator contextConfigurator = ContextConfigurator.systemDefaults();
      contextConfigurator.withPort( PORT );
      fitNesse = new FitNesse( contextConfigurator.makeFitNesseContext() );
      fitNesse.start();

      mojo = new WikiMojo();
      mojo.fitNesseHelper = this.fitNesseHelper;
      mojo.port = PORT;
      mojo.workingDir = "fitnesse";
      mojo.root = FitNesseHelper.DEFAULT_ROOT;
      mojo.project = new MavenProject();
      mojo.project.setFile( new File( getClass().getResource( "pom.xml" ).getPath() ) );

      logStream = new ByteArrayOutputStream();
      log = PrintStreamLogger.createDefaultLog( logStream );
      mojo.setLog( log );
   }

   @After
   public void tearDown() throws Exception {
      if( fitNesse != null ){
         fitNesse.stop();
      }
      assertThat( new FitNesseThreadLocator( log ).findFitNesseServerThread(), nullValue() );
   }

   @Test
   public void testWikiMojoBasic() throws Exception {

      mojo.createSymLink = false;

      new Interrupter( Thread.currentThread(), 100L ).start();
      mojo.executeInternal();

      verify( fitNesseHelper, times( 1 ) ).launchFitNesseServer( PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir );
      verify( fitNesseHelper, never() ).createSymLink( any( File.class ), anyString(), anyInt(), any( Launch.class ) );
      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );

      assertEquals( String.format( "[INFO] FitNesse wiki server launched.%n" + "[INFO] FitNesse wiki server interrupted!%n" + "[INFO] FitNesse wiki server is shutdown.%n" ), logStream.toString() );
   }

   @Test
   public void testWikiMojoCreateSymLink() throws Exception {

      mojo.createSymLink = true;
      // mojo.suite = "suite";
      // mojo.test = "test";
      mojo.testResourceDirectory = "testResourceDirectory";

      Launch launch = new Launch( "suite", "test" );
      new Interrupter( Thread.currentThread(), 100L ).start();

      mojo.executeInternal( launch );

      verify( fitNesseHelper, times( 1 ) ).launchFitNesseServer( PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir );
      verify( fitNesseHelper, times( 1 ) ).createSymLink( mojo.project.getBasedir(), mojo.testResourceDirectory, PORT, launch );
      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );

      assertThat( logStream.toString(), containsString( String.format( "[INFO] FitNesse wiki server launched." ) ) );
      assertThat( logStream.toString(), containsString( String.format( "[INFO] FitNesse wiki server interrupted!" ) ) );
      assertThat( logStream.toString(), containsString( String.format( "[INFO] FitNesse wiki server is shutdown." ) ) );
   }

   @Test
   public void testWikiLaunchException() throws Exception {
      doThrow( new IOException( "TEST" ) ).when( fitNesseHelper ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString() );

      try{
         mojo.executeInternal();
         fail( "Expected MojoExecutionException" );
      }catch( MojoExecutionException e ){
         assertEquals( "Exception launching FitNesse", e.getMessage() );
         assertEquals( IOException.class, e.getCause().getClass() );
      }

      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );

      assertEquals( String.format( "[INFO] FitNesse wiki server is shutdown.%n" ), logStream.toString() );
   }

   @Test
   public void testServiceThreadFinishesWithoutInterrupt() throws Exception {
      new Thread() {
         @Override
         public void run() {
            try{
               Thread.sleep( 200L );
               fitNesse.stop();
            }catch( Exception e ){
               e.printStackTrace();
            }
         }
      }.start();
      mojo.executeInternal();

      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );

      assertEquals( String.format( "[INFO] FitNesse wiki server launched.%n" + "[INFO] FitNesse wiki server is shutdown.%n" ), logStream.toString() );
   }

   @Test
   public void testFitNesseNotRunning() throws Exception {
      fitNesse.stop();
      doNothing().when( fitNesseHelper ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString() );

      Thread.sleep( 100 );

      mojo.executeInternal();

      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );

      assertEquals( String.format( "[WARNING] Could not identify FitNesse service Thread.%n" + "[INFO] FitNesse wiki server is shutdown.%n" ), logStream.toString() );
   }
}
