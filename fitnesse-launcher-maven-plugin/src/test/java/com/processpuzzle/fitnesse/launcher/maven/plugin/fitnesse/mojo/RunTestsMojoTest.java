package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import static com.processpuzzle.litest.matcher.SameTextAs.sameTextAs;
import static com.processpuzzle.litest.matcher.CauseMatcher.exceptionOf;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util.FitNesseHelper;

public class RunTestsMojoTest extends MojoTest{
   @Rule public final ExpectedException thrown = ExpectedException.none();
   private String expectedFailsafeSummaryXml;
   private RunTestsMojo mojo;
   private Log log;

   @Before @Override
   public void beforeEachTest() throws Exception {
      super.beforeEachTest();
      File workingDir = new File( System.getProperty( "java.io.tmpdir" ), "unit_test_working" );

      mojo = new RunTestsMojo();
      mojo.pluginDescriptor = mock( PluginDescriptor.class );
      mojo.fitNesseHelper = mock( FitNesseHelper.class );
      mojo.port = WikiMojoTest.PORT;
      mojo.workingDir = workingDir.getCanonicalPath();
      mojo.root = FitNesseHelper.DEFAULT_ROOT;
      mojo.resultsDir = new File( System.getProperty( "java.io.tmpdir" ), "unit_test_results" );
      mojo.reportsDir = new File( System.getProperty( "java.io.tmpdir" ), "unit_test_reports" );
      mojo.summaryFile = new File( mojo.resultsDir, "failsafe-summary.xml" );
      mojo.project = new MavenProject();
      mojo.project.setFile( new File( getClass().getResource( "pom.xml" ).getPath() ) );
      mojo.project.setBuild( new Build() );
      mojo.project.getBuild().setTestOutputDirectory( "test_out" );

      mojo.setLog( log );

      FileUtils.deleteQuietly( workingDir );
      FileUtils.deleteQuietly( mojo.resultsDir );
      FileUtils.deleteQuietly( mojo.reportsDir );

      File root = new File( workingDir, mojo.root );
      root.mkdirs();
      FileUtils.copyDirectoryToDirectory( new File( getClass().getResource( "/files" ).getPath() ), root );
      FileUtils.copyDirectoryToDirectory( new File( getClass().getResource( "/ExampleFitNesseTestSuite" ).getPath() ), root );

      InputStream summaryIn = RunTestsMojoTest.class.getResourceAsStream( "summary.xml" );
      try{
         this.expectedFailsafeSummaryXml = IOUtils.toString( RunTestsMojoTest.class.getResourceAsStream( "summary.xml" ) );
      }finally{
         IOUtils.closeQuietly( summaryIn );
      }
   }

   /**
    * We have to clean up the mess made by {@link TestsInProgress} and {@link PageInProgressFormatter}.
    * @throws Exception 
    */
   @After @Override
   public void afterEachTest() throws Exception {
      FileUtils.deleteQuietly( new File( FitNesseHelper.DEFAULT_ROOT ) );
      super.afterEachTest();
   }

   @Test
   public void testRunTestsMojoBasic() throws Exception {
      mojo.createSymLink = false;
      Launch launch = new Launch( "ExampleFitNesseTestSuite", null );

      mojo.executeInternal( launch );

      verify( mojo.fitNesseHelper, never() ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString() );
      verify( mojo.fitNesseHelper, never() ).createSymLink( any( File.class ), anyString(), anyInt(), any( Launch.class ) );
      verify( mojo.fitNesseHelper, never() ).shutdownFitNesseServer( anyString() );

      this.expectedFailsafeSummaryXml = this.expectedFailsafeSummaryXml.replace( ">3<", ">1<" ).replace( ">6<", ">2<" ).replace( "\r\n", "\n" );
      
      assertThat( generatedSummaryFile(), sameTextAs( this.expectedFailsafeSummaryXml ) );
      assertThat( generatedTestReport(), containsString( "<title>ExampleFitNesseTestSuite</title>" ));
   }

   @Test
   public void testRunTestsMojoCreateSymLink() throws Exception {
      mojo.createSymLink = true;
      Launch launch = new Launch( "ExampleFitNesseTestSuite", null );

      mojo.executeInternal( launch );

      verify( mojo.fitNesseHelper, times( 1 ) ).launchFitNesseServer( WikiMojoTest.PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir );
      verify( mojo.fitNesseHelper, times( 1 ) ).createSymLink( mojo.project.getBasedir(), mojo.testResourceDirectory, WikiMojoTest.PORT, launch );
      verify( mojo.fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( WikiMojoTest.PORT_STRING );

      // Easier than having a separate summary.xml file. Will separate as need and complexity grows.
      this.expectedFailsafeSummaryXml = this.expectedFailsafeSummaryXml.replace( ">3<", ">1<" ).replace( ">6<", ">2<" ).replace( "\r\n", "\n" );
      
      assertThat( generatedSummaryFile(), sameTextAs( this.expectedFailsafeSummaryXml ));
      assertThat( generatedTestReport(), containsString( "<title>ExampleFitNesseTestSuite</title>" ));
   }

   private String generatedTestReport() throws IOException {
      String generatedTestReport = FileUtils.readFileToString( new File( mojo.reportsDir + "/html", "ExampleFitNesseTestSuite.html" ));
      return generatedTestReport;
   }

   @Test
   public void testCreateSymLinkException() throws Exception {
      doThrow( new IOException( "TEST" ) ).when( mojo.fitNesseHelper ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString() );

      mojo.createSymLink = true;

      try{
         mojo.executeInternal();
         fail( "Expected MojoExecutionException" );
      }catch( MojoExecutionException e ){
         assertEquals( "Exception creating FitNesse SymLink", e.getMessage() );
         assertEquals( IOException.class, e.getCause().getClass() );
      }

      verify( mojo.fitNesseHelper, times( 1 ) ).launchFitNesseServer( anyString(), anyString(), anyString(), anyString() );
      verify( mojo.fitNesseHelper, never() ).createSymLink( any( File.class ), anyString(), anyInt(), any( Launch.class ) );
      verify( mojo.fitNesseHelper, times( 1 ) ).shutdownFitNesseServer( anyString() );

      assertThat( mojo.summaryFile.exists(), is( false ));
      assertThat( new File( mojo.resultsDir, "TEST-ExampleFitNesseTestSuite.xml" ).exists(), is( false ));
      assertThat( new File( mojo.reportsDir, "ExampleFitNesseTestSuite.html" ).exists(), is( false ));
   }

   @Test
   public void testSuiteAndTestException() throws Exception {
      thrown.expect( MojoExecutionException.class );
      thrown.expectMessage( "Exception running FitNesse tests" );
      thrown.expectCause( exceptionOf( IllegalArgumentException.class, "No suite or test page specified" ));

      LogFactory.getFactory().setAttribute( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog" );
      
      mojo.executeInternal( new Launch() );
   }

   @Test
   public void testWriteSummaryException() throws Exception {
      thrown.expect( MojoExecutionException.class );
      thrown.expectMessage( containsString( mojo.resultsDir + File.separator + "failsafe-summary.xml" ));
      thrown.expectCause( exceptionOf( FileNotFoundException.class, "" ));

      mojo.suite = "ExampleFitNesseTestSuite";
      mojo.resultsDir.createNewFile();

      mojo.executeInternal( new Launch( "ExampleFitNesseTestSuite", null ) );
   }

   @Test
   public void testSurefireReportParameters() {
      mojo.setSkipTests( true );
      assertFalse( mojo.isSkipTests() );

      mojo.setSkipExec( true );
      assertFalse( mojo.isSkipExec() );

      mojo.setSkip( true );
      assertFalse( mojo.isSkip() );

      mojo.setFailIfNoTests( false );
      assertFalse( mojo.getFailIfNoTests() );

      mojo.setFailIfNoTests( true );
      assertTrue( mojo.getFailIfNoTests() );

      mojo.setTestFailureIgnore( false );
      assertTrue( mojo.isTestFailureIgnore() );

      mojo.setBasedir( new File( "" ) );
      assertEquals( mojo.project.getBasedir(), mojo.getBasedir() );

      mojo.setTestClassesDirectory( new File( "" ) );
      assertEquals( new File( "test_out" ), mojo.getTestClassesDirectory() );

      File file = mojo.reportsDir;
      mojo.setReportsDirectory( null );
      assertNull( mojo.getReportsDirectory() );
      assertNull( mojo.reportsDir );
      mojo.setReportsDirectory( file );
      assertSame( file, mojo.getReportsDirectory() );
      assertSame( file, mojo.reportsDir );
   }

   private String generatedSummaryFile() throws IOException {
      String generatedSummaryFile = FileUtils.readFileToString( mojo.summaryFile );
      return generatedSummaryFile;
   }
}
