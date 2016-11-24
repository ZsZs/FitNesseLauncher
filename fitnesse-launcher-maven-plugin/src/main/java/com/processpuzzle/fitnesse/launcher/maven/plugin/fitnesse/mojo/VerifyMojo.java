package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.ReaderFactory;
import org.apache.maven.surefire.suite.RunResult;
import org.codehaus.plexus.util.IOUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import fitnesse.slim.SlimServer;

/**
 * Goal that provides summary report on FitNesse tests run with 'run-tests' goal. Intended to be bound to the 'verify' phase. Will fail the build if there are
 * test failures.
 * 
 * @goal verify
 * @phase verify
 */
public class VerifyMojo extends RunTestsMojo {
   private static final String HTML_REPORT_FOLDER = "/html";
   private static final String HTML = ".html";
   private static final String ERR_MSG = "FITNESSE ERROR in %s:%s";

   /**
    * @parameter property="fitnesse.maxErrorsToConsole"
    */
   protected int maxErrorsToConsole = 100;
   private int countErrorsToConsole = 0;
   private RunResult summary;

   public VerifyMojo() {
      super( false );
   }

   // public accessors and mutators
   @Override public final void execute() throws MojoExecutionException, MojoFailureException {
      summary = readSummary();

      if( logErrorsToConsole() && errorOccured() ){
         logExceptions();
      }
      SurefireHelper.reportExecution( this, summary, getLog() );
   }

   // protected, private helper methods
   private String determineReportsFolder() {
      return this.reportsDir + HTML_REPORT_FOLDER;
   }

   private boolean errorOccured() {
      return summary.getErrors() > 0 || summary.getFailures() > 0;
   }

   private void logExceptions() throws MojoExecutionException {
      final Launch[] launches = super.launches();
      for( int i = 0; i < launches.length && logErrorsToConsole(); i++ ){
         final File indexFile = new File( determineReportsFolder(), launches[i].getPageName() + HTML );
         final Document indexHtml = parseHtml( indexFile );
         final List<Element> anchors = indexHtml.getElementsByTag( "a" );
         for( int j = 0; j < anchors.size() && logErrorsToConsole(); j++ ){
            final String test = anchors.get( j ).attr( "href" );
            final File testFile = new File( determineReportsFolder(), test );
            final Document testHtml = parseHtml( testFile );
            final List<Element> errors = testHtml.getElementsByClass( "error" );
            errors.addAll( testHtml.getElementsByClass( "fail" ));
            for( int k = 0; k < errors.size() && logErrorsToConsole(); k++ ){
               final Element error = errors.get( k );
               final Elements fitLabel = error.getElementsByClass( "fit_label" );
               if( !fitLabel.isEmpty() ){
                  logFitNesseError( test, extractElementsText( error ));
               }
               final Elements fitStacktrace = error.getElementsByClass( "fit_stacktrace" );
               if( !fitStacktrace.isEmpty() ){
                  logFitNesseError( test, fitStacktrace.text() );
               }
            }
            final List<Element> details = testHtml.getElementsByClass( "exception-detail" );
            for( int k = 0; k < details.size() && logErrorsToConsole(); k++ ){
               final Element detail = details.get( k );
               if( detail.hasText() ){
                  logFitNesseError( test, StringUtils.substringAfter( detail.text(), SlimServer.EXCEPTION_TAG ) );
               }
            }
         }
      }
   }

   private String extractElementsText( Node element ) {
      String textContent = "";
      for( Node childElement : element.childNodes() ){
         if( childElement instanceof TextNode ){
            textContent += " " + ((TextNode) childElement).text();
         }
         
         if( childElement.childNodes().size() > 0 ){
            textContent += " " + extractElementsText( childElement );
         }
      }
      return textContent;
   }

   private boolean logErrorsToConsole() {
      return(this.countErrorsToConsole < this.maxErrorsToConsole);
   }

   private void logFitNesseError( final String testName, final String errorText ) {
      if( logErrorsToConsole() ){
         getLog().error( String.format( ERR_MSG, StringUtils.substringBeforeLast( testName, HTML ), errorText ) );
         this.countErrorsToConsole++;
      }
   }

   private Document parseHtml( final File file ) throws MojoExecutionException {
      try{
         final Document html = Jsoup.parse( file, ReaderFactory.UTF_8 );
         return html;
      }catch( final IOException e ){
         throw new MojoExecutionException( "IOException: " + file, e );
      }finally{
         // JSoup closes the stream for us
      }
   }

   /**
    * @see org.apache.maven.plugin.failsafe.VerifyMojo
    */
   private RunResult readSummary() throws MojoExecutionException {
      FileInputStream fileInputStream = null;
      BufferedInputStream bufferedInputStream = null;
      try{
         fileInputStream = new FileInputStream( this.summaryFile );
         bufferedInputStream = new BufferedInputStream( fileInputStream );
         return RunResult.fromInputStream( bufferedInputStream, ReaderFactory.UTF_8 );
      }catch( FileNotFoundException e ){
         getLog().error( "Test summary report is missing. Please be aware that with 'wiki' goal, it is not produced. Consider to remove the 'verify' goal." );
         throw new MojoExecutionException( e.getMessage(), e );
      }catch( Exception e ){
         throw new MojoExecutionException( e.getMessage(), e );
      }finally{
         IOUtil.close( bufferedInputStream );
         IOUtil.close( fileInputStream );
      }
   }
}
