package uk.co.javahelp.maven.plugin.fitnesse.util;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import com.google.common.collect.Lists;

import fitnesse.Shutdown;
import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;
import uk.co.javahelp.maven.plugin.fitnesse.mojo.Launch;

public class FitNesseHelper {
   public static final String DEFAULT_ROOT = "FitNesseRoot";
   private static final String UTF8 = "UTF-8";
   private static final long SHUTDOWN_WAIT_MS = 50;
   private final Log log;

   public FitNesseHelper( final Log log ) {
      this.log = log;
   }

   /**
    * Note: Through experiment I've found that we can safely send duplicate 'create SymLink' requests - FitNesse isn't bothered But We use a HashSet to
    * eliminate duplicate top-level link names anyway, just to keep the output clean
    * 
    * @throws IOException
    * @see <a href="http://fitnesse.org/FitNesse.UserGuide.SymbolicLinks">FitNesse SymLink User Guide</a>
    */
   public void createSymLink( final File basedir, final String testResourceDirectory, final int port, final Launch... launches ) throws IOException {
      final Set<String> linkNames = new HashSet<String>();
      for( final Launch launch : launches ){
         linkNames.add( calcLinkName( launch ) );
      }
      for( final String linkName : linkNames ){
         createSymLink( basedir, testResourceDirectory, port, linkName );
      }
   }

   public StringBuilder formatAndAppendClasspathArtifact( final StringBuilder wikiFormatClasspath, final Artifact artifact ) {
      return formatAndAppendClasspath( wikiFormatClasspath, artifact.getFile().getPath() );
   }

   public StringBuilder formatAndAppendClasspath( final StringBuilder wikiFormatClasspath, final String path ) {
      if( Utils.whitespaceSituation( path ) ){
         log.error( Utils.whitespaceWarning( path, "FitNesse classpath may not function correctly in wiki mode" ) );
      }
      wikiFormatClasspath.append( "!path " );
      wikiFormatClasspath.append( path );
      wikiFormatClasspath.append( "\n" );
      return wikiFormatClasspath;
   }

   public void launchFitNesseServer( final String port, final String workingDir, final String root, final String logDir ) throws Exception {
      ArrayList<String> commandLineArguments = Lists.newArrayList( "-e", "0", "-o", "-p", String.valueOf( port ), "-d", root );
      
      if( logDir != null && !logDir.trim().equals( "" ) ){
         commandLineArguments.add( "-l" );
         commandLineArguments.add( logDir );
      }

      Arguments arguments = null;
      try{
         arguments = new Arguments( commandLineArguments.toArray( new String[commandLineArguments.size()] ));
      }catch( IllegalArgumentException e ){
         //Arguments.printUsage();
         exit( 1 );
      }
      Integer exitCode = 0;
      try{
         exitCode = new FitNesseMain().launchFitNesse( arguments );
      }catch( Exception e ){
         e.printStackTrace( System.out );
         exitCode = 1;
      }
      if( exitCode != null ){
         exit( exitCode );
      }
   }

   public void shutdownFitNesseServer( final String port ) {
      try{
         Shutdown.main( new String[] { "-p", port } );
         // Pause to give it a chance to shutdown
         Thread.sleep( SHUTDOWN_WAIT_MS );
      }catch( ConnectException e ){
         // If we get this specific exception,
         // we assume FitNesse is already not running
         this.log.info( "FitNesse already not running." );
      }catch( Exception e ){
         this.log.error( e );
      }
   }

   private int createSymLink( final File basedir, final String testResourceDirectory, final int port, final String linkName ) throws IOException {
      final String linkPath = calcLinkPath( linkName, basedir, testResourceDirectory );

      HttpURLConnection connection = null;
      try{
         final String urlPath = String.format( "/root?responder=symlink&linkName=%s&linkPath=%s&submit=%s", URLEncoder.encode( linkName, UTF8 ), URLEncoder.encode( linkPath, UTF8 ),
               URLEncoder.encode( "Create/Replace", UTF8 ) );
         final URL url = new URL( "http", "localhost", port, urlPath );
         this.log.info( "Calling " + url );
         connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod( "GET" );
         connection.connect();
         final int responseCode = connection.getResponseCode();
         this.log.info( "Response code: " + responseCode );
         return responseCode;
      }finally{
         if( connection != null ){
            connection.disconnect();
         }
      }
   }

   private String calcLinkName( final Launch launch ) {
      final String linkName = StringUtils.substringBefore( launch.getPageName(), "." );
      return linkName;
   }

   /**
    * We want File.toURL() exactly because it doesn't properly encode URI's, otherwise we end up encoding parts of the returned linkPath twice.
    */
   @SuppressWarnings( "deprecation" )
   private String calcLinkPath( final String linkName, final File basedir, final String testResourceDirectory ) throws MalformedURLException {
      final StringBuilder linkPath = new StringBuilder( basedir.toURL().toString().replaceFirst( "/[A-Z]:", "" ).replaceFirst( ":", "://" ) );
      linkPath.append( "/" );
      linkPath.append( testResourceDirectory );
      linkPath.append( "/" );
      linkPath.append( linkName );
      return linkPath.toString();
   }
   
   private void exit( int exitCode ) {
      System.exit( exitCode );
   }
}
