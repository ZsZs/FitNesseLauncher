package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefireReportParameters;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.ReaderFactory;
import org.apache.maven.surefire.suite.RunResult;

import fitnesse.junit.JUnitRunNotifierResultsListener;
import fitnesse.junit.PrintTestListener;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import uk.co.javahelp.maven.plugin.fitnesse.junit.TestHelper;
import uk.co.javahelp.maven.plugin.fitnesse.responders.run.DelegatingResultsListener;

/**
 * Goal that launches FitNesse tests using fitnesse.junit.TestHelper. Intended to be bound to the 'integration-test' phase.
 * 
 * @goal run-tests
 * @phase integration-test
 * @requiresDependencyResolution test
 * @see fitnesse.junit.TestHelper
 */
public class RunTestsMojo extends AbstractFitNesseMojo implements SurefireReportParameters {

   private final boolean testFailureIgnore;

   public RunTestsMojo() {
      this( true );
   }

   protected RunTestsMojo( final boolean testFailureIgnore ) {
      this.testFailureIgnore = testFailureIgnore;
   }

   @Override
   protected final void executeInternal( final Launch... launches ) throws MojoExecutionException, MojoFailureException {
      if( launches.length == 0 ) {
         getLog().warn( "No FitNesse Suites or Tests to run! (Set -Dfitnesse.fitnesse.failIfNoTests=false to ignore.)" );
      }

      if( this.createSymLink ) {
         createSymLink( launches );
      }

      final RunResult result = runFitNesseTests( launches );
      try{
         result.writeSummary( this.summaryFile, false, ReaderFactory.UTF_8 );
      }catch( final IOException e ){
         throw new MojoExecutionException( e.getMessage(), e );
      }
   }

   /**
    * Creating a SymLink is easiest when FitNesse is running in 'wiki server' mode.
    */
   private void createSymLink( final Launch... launches ) throws MojoExecutionException {
      final String portString = this.port.toString();
      try{
         this.fitNesseHelper.launchFitNesseServer( portString, this.workingDir, this.root, this.logDir );
         this.fitNesseHelper.createSymLink( this.project.getBasedir(), this.testResourceDirectory, this.port, launches );
      }catch( Exception e ){
         throw new MojoExecutionException( "Exception creating FitNesse SymLink", e );
      }finally{
         this.fitNesseHelper.shutdownFitNesseServer( portString );
      }
   }

   /**
    * Strange side-effect behaviour: If debug=false, FitNesse falls into wiki mode.
    */
   private RunResult runFitNesseTests( final Launch... launches ) throws MojoExecutionException {
      TestSystemListener<WikiTestPage> junitListener = new JUnitRunNotifierResultsListener( null, null );
      final TestSystemListener<WikiTestPage> resultsListener = new DelegatingResultsListener<WikiTestPage>( new PrintTestListener(), junitListener );
      final TestHelper helper = new TestHelper( this.workingDir, this.reportsDir.getAbsolutePath(), resultsListener );
      helper.setDebugMode( true );

      try{
         final TestSummary summary = helper.run( this.port, launches );
         getLog().info( summary.toString() );
         final RunResult result = new RunResult( summary.getRight(), summary.getExceptions(), summary.getWrong(), summary.getIgnores() );
         return result;
      }catch( Exception e ){
         throw new MojoExecutionException( "Exception running FitNesse tests", e );
      }
   }

   // ------------------------------------------------------------------------
   // See http://maven.apache.org/plugins/maven-surefire-plugin/test-mojo.html
   // ------------------------------------------------------------------------

   @Override
   public final boolean isSkipTests() {
      return false;
   }

   @Override
   public void setSkipTests( final boolean unused ) {}

   @Override
   public final boolean isSkipExec() {
      return false;
   }

   @Override
   public void setSkipExec( final boolean unused ) {}

   @Override
   public final boolean isSkip() {
      return false;
   }

   @Override
   public void setSkip( final boolean unused ) {}

   @Override
   public final boolean isTestFailureIgnore() {
      return this.testFailureIgnore;
   }

   @Override
   public void setTestFailureIgnore( final boolean unused ) {}

   @Override
   public final File getBasedir() {
      return this.project.getBasedir();
   }

   @Override
   public void setBasedir( final File unused ) {}

   @Override
   public final File getTestClassesDirectory() {
      return new File( this.project.getBuild().getTestOutputDirectory() );
   }

   @Override
   public final void setTestClassesDirectory( final File unused ) {}

   @Override
   public final File getReportsDirectory() {
      return this.reportsDir;
   }

   @Override
   public final void setReportsDirectory( final File reportsDirectory ) {
      this.reportsDir = reportsDirectory;
   }

   @Override
   public final Boolean getFailIfNoTests() {
      return this.failIfNoTests;
   }

   @Override
   public final void setFailIfNoTests( final Boolean failIfNoTests ) {
      this.failIfNoTests = failIfNoTests;
   }
}
