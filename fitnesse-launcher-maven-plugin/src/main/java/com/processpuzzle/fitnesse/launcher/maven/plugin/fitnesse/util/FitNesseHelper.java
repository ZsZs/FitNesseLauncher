package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import com.google.common.collect.Lists;
import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.AbstractFitNesseMojo;
import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.Launch;

import fitnesse.Shutdown;
import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;

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

   public Process forkFitNesseServer( final String port, final String workingDir, final String root, final String logDir, final String classpath ) throws Exception {
      List<String> commandLine = buildCommandLine( port, workingDir, root, classpath );      

      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command( commandLine );
      processBuilder.redirectOutput();
      Map<String, String> environment = processBuilder.environment();
      environment.put( AbstractFitNesseMojo.MAVEN_CLASSPATH, System.getProperty( AbstractFitNesseMojo.MAVEN_CLASSPATH ));
      Process fitNesseProcess = processBuilder.start();
      Thread.sleep(10000);
      log.info( "FitNesse process started in: " + workingDir + " with root of: " + root + " on port: " + port );
      return fitNesseProcess;
   }

   public void launchFitNesseServer( final String port, final String workingDir, final String root, final String logDir ) throws Exception {
      Arguments arguments = processCommandLineArguments( port, workingDir, root, logDir );
      
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
         Thread.sleep( SHUTDOWN_WAIT_MS );
         destroyFitNesseProcess();
      }catch( ConnectException e ){
         this.log.info( "FitNesse already not running." );
      }catch( Exception e ){
         this.log.error( e );
      }
   }

   // protected, private helper methods
   private List<String> buildCommandLine( String port, String workingDir, String root, String classpath ) {
      List<String> commandLine = Lists.newArrayList();
      
      String javaHome = System.getProperty("java.home");
      String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
      String className = FitNesseMain.class.getCanonicalName();
      
      commandLine.add( javaBin );
      commandLine.add( "-cp" );
      commandLine.add( classpath );
      commandLine.add( className );
      commandLine.add( "-p" );
      commandLine.add( port );
      commandLine.add( "-d" );
      commandLine.add( workingDir );
      commandLine.add( "-r" );
      commandLine.add( root );
      
      return commandLine;
   }

   private ArrayList<String> buildCommandLineArgumentsAsArray( final String port, final String workingDir, final String root ) {
      ArrayList<String> commandLineArguments = Lists.newArrayList( "-e", "0", "-o", "-p", String.valueOf( port ), "-d", workingDir, "-r", root );
      return commandLineArguments;
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
      if( !linkPath.substring( linkPath.length() -1 ).equals( "/" )) {
         linkPath.append( "/" );
      }
      linkPath.append( testResourceDirectory );
      if( !linkPath.substring( linkPath.length() -1 ).equals( "/" )) {
         linkPath.append( "/" );
      }
      linkPath.append( linkName );
      return linkPath.toString();
   }
   
   private void destroyFitNesseProcess() throws IOException {
      String fitNessePID = findFitNesseProcessPID();
      if( fitNessePID != null ){
         String cmd = "taskkill /F /PID " + fitNessePID;
         Process taskKill = Runtime.getRuntime().exec(cmd);
         taskKill.destroy();
         log.info( "FitNesse process killed with PID: " + fitNessePID );
      }else{
         log.info( "FitNesse PID not found." );
      }
   }

   private void exit( int exitCode ) {
      System.exit( exitCode );
   }
   
   private String findFitNesseProcessPID() {
      String process;
      String fitNessePID = null;
      String pid = "";
      String mainClass = "";
      
      Process psProcess = null;
      BufferedReader input = null;
      try{
         psProcess = Runtime.getRuntime().exec( "jps -lv" );
         input = new BufferedReader( new InputStreamReader( psProcess.getInputStream() ));
         while( (process = input.readLine()) != null ){
            StringTokenizer st = new StringTokenizer( process );

            for( int i = 0; i <= st.countTokens(); i++) {
               String token = (String) st.nextElement();
               if( i == 0 ) pid = token;
               else if( i == 1 ) mainClass = token;
            }
            
            if( mainClass.equals( "fitnesseMain.FitNesseMain" )){
               fitNessePID = pid;
            }
         }
      }catch( IOException e ){
         log.error( "Spawn JPS process failed." );;
      }finally{
         try{
            input.close();
            psProcess.destroy();         
         }catch( IOException e ){
            log.error( "Closing input stream failed." );
         }
      }
      
      return fitNessePID;
   }

   private Arguments processCommandLineArguments( final String port, final String workingDir, final String root, final String logDir ) {
      ArrayList<String> commandLineArguments = buildCommandLineArgumentsAsArray( port, workingDir, root );
      
      if( logDir != null && !logDir.trim().equals( "" ) ){
         commandLineArguments.add( "-l" );
         commandLineArguments.add( logDir );
      }

      Arguments arguments = null;
      try{
         arguments = new Arguments( commandLineArguments.toArray( new String[commandLineArguments.size()] ));
      }catch( IllegalArgumentException e ){
         exit( 1 );
      }
      return arguments;
   }
}
