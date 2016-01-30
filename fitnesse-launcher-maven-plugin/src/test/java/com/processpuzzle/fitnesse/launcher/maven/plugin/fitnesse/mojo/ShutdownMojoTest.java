package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.ShutdownMojo;

public class ShutdownMojoTest {
   private static final int DEFAULT_COMMAND_PORT = 9123;
   private ShutdownMojo mojo;
   private ByteArrayOutputStream logStream;
   private Server server;

   @Before
   public void setUp() throws Exception {

      mojo = new ShutdownMojo();

      logStream = new ByteArrayOutputStream();
      mojo.setLog( new DefaultLog( new PrintStreamLogger( Logger.LEVEL_INFO, "test", new PrintStream( logStream ) ) ) );
   }

   @After
   public void tearDown() throws Exception {
      if( this.server != null ){
         this.server.stop();
      }
   }

   @Ignore @Test
   public void testServerRunning() throws Exception {
      this.server = new Server( DEFAULT_COMMAND_PORT );
      this.server.setHandler( new Handler() );
      this.server.start();

      mojo.execute();

      assertEquals( String.format( "[INFO] FitNesse wiki server is shutdown.%n" ), logStream.toString() );
   }

   @Ignore @Test
   public void testServerNotRunning() throws Exception {
      mojo.execute();
      assertEquals( String.format( "[INFO] FitNesse already not running.%n" + "[INFO] FitNesse wiki server is shutdown.%n" ), logStream.toString() );
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
