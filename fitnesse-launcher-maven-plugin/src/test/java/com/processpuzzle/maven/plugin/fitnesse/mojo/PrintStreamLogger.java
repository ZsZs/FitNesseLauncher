package com.processpuzzle.maven.plugin.fitnesse.mojo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * Copied from {@link org.codehaus.plexus.logging.console.ConsoleLogger} because that class is 'final' and cannot be extended. Default constructor of this class
 * achieves exactly the same thing.
 */
public class PrintStreamLogger extends AbstractLogger {
   private final PrintStream printStream;
   private static final String[] TAGS = { "[DEBUG] ", "[INFO] ", "[WARNING] ", "[ERROR] ", "[FATAL ERROR] " };

   public PrintStreamLogger( final int threshold, final String name, final PrintStream printStream ) {
      super( threshold, name );
      this.printStream = printStream;
   }

   public PrintStreamLogger() {
      this( Logger.LEVEL_INFO, "console", System.out );
   }

   public static Log createDefaultLog() {
      ByteArrayOutputStream logStream = new ByteArrayOutputStream();
      return createDefaultLog( logStream );
   }

   public static Log createDefaultLog(ByteArrayOutputStream logStream) {
      return new DefaultLog( new PrintStreamLogger( Logger.LEVEL_INFO, "test", new PrintStream( logStream ) ) );
   }

   public void debug( final String message, final Throwable throwable ) {
      if( isDebugEnabled() ){
         log( LEVEL_DEBUG, message, throwable );
      }
   }

   public void info( final String message, final Throwable throwable ) {
      if( isInfoEnabled() ){
         log( LEVEL_INFO, message, throwable );
      }
   }

   public void warn( final String message, final Throwable throwable ) {
      if( isWarnEnabled() ){
         log( LEVEL_WARN, message, throwable );
      }
   }

   public void error( final String message, final Throwable throwable ) {
      if( isErrorEnabled() ){
         log( LEVEL_ERROR, message, throwable );
      }
   }

   public void fatalError( final String message, final Throwable throwable ) {
      if( isFatalErrorEnabled() ){
         log( LEVEL_FATAL, message, throwable );
      }
   }

   public Logger getChildLogger( final String name ) {
      return this;
   }

   // ----------------------------------------------------------------------
   // Implementation methods
   // ----------------------------------------------------------------------

   private void log( final int level, final String message, final Throwable throwable ) {
      this.printStream.println( TAGS[level].concat( message ) );
      if( throwable != null ){
         throwable.printStackTrace( this.printStream );
      }
   }
}
