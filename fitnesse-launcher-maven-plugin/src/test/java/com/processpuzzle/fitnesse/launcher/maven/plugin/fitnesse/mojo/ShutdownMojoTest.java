package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static com.processpuzzle.litest.matcher.TextContainsLine.containsLine;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ShutdownMojoTest extends MojoTest{
   private ShutdownMojo mojo;
   private Server server;

   @Before @Override public void beforeEachTest() throws Exception {
      super.beforeEachTest();
      mojo = new ShutdownMojo();
      mojo.setLog( log );
   }

   @After @Override public void afterEachTest() throws Exception {
      if( this.server != null ){
         this.server.stop();
      }
      
      super.afterEachTest();
   }

   @Ignore @Test
   public void testServerRunning() throws Exception {
      this.server = new Server( PORT );
      this.server.setHandler( new Handler() );
      this.server.start();

      mojo.execute();

      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ));
   }

   @Ignore @Test
   public void testServerNotRunning() throws Exception {
      mojo.execute();
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse already not running." ));
      assertThat( logStream.toString(), containsLine( "[INFO] FitNesse wiki server is shutdown." ) );
   }

   private static class Handler extends AbstractHandler {

      @Override
      public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {

         assertEquals( "/", request.getRequestURI() );
         assertEquals( "responder=shutdown", request.getQueryString() );

         response.addHeader( "Server", "FitNesse" );
         response.setStatus( HttpServletResponse.SC_OK );
         response.flushBuffer();
      }
   }
}
