package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

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
         getLog().info( "FitNese is running with Process " + fitProcess.toString() );
         if( this.createSymLink ){
            this.fitNesseHelper.createSymLink( this.project.getBasedir(), this.testResourceDirectory, this.port, launches );
         }
      }catch( InterruptedException e ){
         getLog().info( "FitNesse wiki server interrupted!" );
      }catch( Exception e ){
         throw new MojoExecutionException( "Exception launching FitNesse", e );
      }finally{
         getLog().info( "FitNesse-Launcher is quitting." );
      }
   }
}
