package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import static com.processpuzzle.litest.matcher.TextContainsLine.containsLine;
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

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util.FitNesseHelper;
import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util.Interrupter;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;

@Ignore
public class WikiMojoTest extends MojoTest{
   private FitNesse fitNesse;
   private FitNesseHelper fitNesseHelper;
   private WikiMojo mojo;

   @Before @Override public void beforeEachTest() throws Exception {
      super.beforeEachTest();
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

      mojo.setLog( log );
   }

   @After @Override public void afterEachTest() throws Exception {      
      if( fitNesse != null ){
         fitNesse.stop();
      }
      super.afterEachTest();
   }

   @Test
   public void testWikiMojoBasic() throws Exception {
      mojo.createSymLink = false;

      new Interrupter( Thread.currentThread(), 50L ).start();
      mojo.executeInternal();

      verify( fitNesseHelper, times( 1 ) ).launchFitNesseServer( PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir, mojo.authentication );
      verify( fitNesseHelper, never() ).createSymLink( any( File.class ), anyString(), anyInt(), any( Launch.class ) );
      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );

      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server launched." ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server interrupted!" ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ));
   }

   @Test
   public void testWikiMojoCreateSymLink() throws Exception {
      mojo.createSymLink = true;
      mojo.testResourceDirectory = "testResourceDirectory";
      Launch launch = new Launch( "suite", "test" );
      new Interrupter( Thread.currentThread(), 100L ).start();

      mojo.executeInternal( launch );

      verify( fitNesseHelper, times( 1 ) ).launchFitNesseServer( PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir, mojo.authentication );
      verify( fitNesseHelper, times( 1 ) ).createSymLink( mojo.project.getBasedir(), mojo.testResourceDirectory, PORT, launch );
      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server launched." ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server interrupted!" ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ));
   }

   @Test
   public void testWikiLaunchException() throws Exception {
      doThrow( new IOException( "TEST" ) ).when( fitNesseHelper ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString(), anyString() );

      try{
         mojo.executeInternal();
         fail( "Expected MojoExecutionException" );
      }catch( MojoExecutionException e ){
         assertEquals( "Exception launching FitNesse", e.getMessage() );
         assertEquals( IOException.class, e.getCause().getClass() );
      }

      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ));
   }

   @Test
   public void testServiceThreadFinishesWithoutInterrupt() throws Exception {
      new Thread() {
         @Override
         public void run() {
            try{
               Thread.sleep( 500L );
               fitNesse.stop();
            }catch( Exception e ){
               e.printStackTrace();
            }
         }
      }.start();
      mojo.executeInternal();

      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server launched." ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ));
   }

   @Test
   public void testFitNesseNotRunning() throws Exception {
      fitNesse.stop();
      doNothing().when( fitNesseHelper ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString(), anyString() );
      Thread.sleep( 100 );

      mojo.executeInternal();

      verify( fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( PORT_STRING );
      assertThat( logStream.toString(), containsLine( "[WARNING] Could not identify FitNesse service Thread." ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ));
   }
}
