package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.PrintStream;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * Copied from {@link org.codehaus.plexus.logging.console.ConsoleLogger} because
 * that class is 'final' and cannot be extended.
 * Default constructor of this class achieves exactly the same thing.
 */
public class PrintStreamLogger extends AbstractLogger {
	
	private final PrintStream printStream;

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String[] TAGS = { "[DEBUG] ", "[INFO] ", "[WARNING] ", "[ERROR] ", "[FATAL ERROR] " };

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PrintStreamLogger( final int threshold, final String name, final PrintStream printStream )
    {
        super( threshold, name );
        this.printStream = printStream;
    }

    public PrintStreamLogger()
    {
        this( Logger.LEVEL_INFO, "console", System.out );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void debug( final String message, final Throwable throwable )
    {
        if ( isDebugEnabled() )
        {
            log( LEVEL_DEBUG, message, throwable );
        }
    }

    public void info( final String message, final Throwable throwable )
    {
        if ( isInfoEnabled() )
        {
            log( LEVEL_INFO, message, throwable );
        }
    }

    public void warn( final String message, final Throwable throwable )
    {
        if ( isWarnEnabled() )
        {
            log( LEVEL_WARN, message, throwable );
        }
    }

    public void error( final String message, final Throwable throwable )
    {
        if ( isErrorEnabled() )
        {
            log( LEVEL_ERROR, message, throwable );
        }
    }

    public void fatalError( final String message, final Throwable throwable )
    {
        if ( isFatalErrorEnabled() )
        {
            log( LEVEL_FATAL, message, throwable );
        }
    }

    public Logger getChildLogger( final String name )
    {
        return this;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void log( final int level, final String message, final Throwable throwable )
    {
        this.printStream.println( TAGS[level].concat( message ) );
        if ( throwable != null )
        {
            throwable.printStackTrace( this.printStream );
        }
    }
}
