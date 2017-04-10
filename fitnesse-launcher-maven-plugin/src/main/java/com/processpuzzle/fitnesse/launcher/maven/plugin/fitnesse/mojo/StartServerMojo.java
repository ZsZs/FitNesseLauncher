package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal that launches FitNesse as a wiki server and quits the Maven process. Useful for manually running / developing / debugging FitNesse tests. Once launched,
 * just visit http://localhost:&lt;port&gt;/&lt;suite&gt;. Use the 'shutdown' goal, or send GET to http://localhost:&lt;port&gt;/?responder=shutdown.
 *
 * @goal server
 * @phase integration-test
 * @requiresDependencyResolution test
 */
public class StartServerMojo extends AbstractFitNesseMojo {
   @Override protected final void executeInternal( final Launch... launches ) throws MojoExecutionException, MojoFailureException {
      final String portString = this.port.toString();
      try{
         Process fitProcess = this.fitNesseHelper.forkFitNesseServer( portString, this.workingDir, this.root, this.logDir, this.fitNesseClasspath );
         getLog().info( "FitNese is running with Process " + fitProcess );
         if( pingURL( "http://localhost:" + portString + "/root", 10000 )){
            if( this.createSymLink ){
               this.fitNesseHelper.createSymLink( this.project.getBasedir(), this.testResourceDirectory, this.port, launches );
            }
         }else{
            getLog().error( "Ping FitNesse host timed out!" );            
         }
      }catch( InterruptedException e ){
         getLog().info( "FitNesse wiki server interrupted!" );
      }catch( Exception e ){
         throw new MojoExecutionException( "Exception launching FitNesse", e );
      }finally{
         getLog().info( "FitNesse-Launcher is quitting." );
      }
   }

   private boolean pingURL( String url, int timeout ) {
      url = url.replaceFirst( "^https", "http" ); // Otherwise an exception may be thrown on invalid SSL certificates.

      try{
         HttpURLConnection connection = (HttpURLConnection) new URL( url ).openConnection();
         connection.setConnectTimeout( timeout );
         connection.setReadTimeout( timeout );
         connection.setRequestMethod( "GET" );
         int responseCode = connection.getResponseCode();
         return(200 <= responseCode && responseCode <= 399);
      }catch( IOException exception ){
         return false;
      }
   }
}
