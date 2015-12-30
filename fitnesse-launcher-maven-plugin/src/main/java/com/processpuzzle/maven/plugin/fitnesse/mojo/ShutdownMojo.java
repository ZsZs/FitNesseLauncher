package com.processpuzzle.maven.plugin.fitnesse.mojo;

import com.processpuzzle.maven.plugin.fitnesse.util.FitNesseHelper;

/**
 * Goal that shuts down FitNesse. This is done by sending GET to http://localhost:&lt;port&gt;/?responder=shutdown.
 *
 * @goal shutdown
 */
public class ShutdownMojo extends org.apache.maven.plugin.AbstractMojo {

   /**
    * @parameter property="fitnesse.port" default-value="9123"
    */
   private String port = "9123";

   @Override
   public final void execute() {
      new FitNesseHelper( getLog() ).shutdownFitNesseServer( port );
      getLog().info( "FitNesse wiki server is shutdown." );
   }
}
