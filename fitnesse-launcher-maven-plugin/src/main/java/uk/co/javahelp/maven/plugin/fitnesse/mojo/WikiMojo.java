package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import uk.co.javahelp.maven.plugin.fitnesse.util.Interrupter;
import fitnesse.socketservice.SocketService;

/**
 * Goal that launches FitNesse as a wiki server.
 * Useful for manually running / developing / debugging FitNesse tests.
 * Once launched, just visit http://localhost:&lt;port&gt;/&lt;suite&gt;.
 * Use the 'shutdown' goal (from a different command line), or Ctrl+C to shutdown,
 * or send GET to http://localhost:&lt;port&gt;/?responder=shutdown.
 *
 * @goal wiki
 * @phase integration-test
 * @requiresDependencyResolution test
 */
public class WikiMojo extends AbstractFitNesseMojo {
	
	private static final String FITNESSE_SOCKET_SERVICE = SocketService.class.getName();

    /**
	 * Unfortunately, the FitNesse API does not expose a way to stop the wiki server programmatically,
	 * except via a sending "/?responder=shutdown" via HTTP, which is what the {@link fitnesse.Shutdown} object does.
	 * The object / method we need access to is {@link fitnesse.FitNesse#stop()}.
	 * This could easily have been returned from our public call
	 * to {@link fitnesseMain.FitNesseMain#launchFitNesse(fitnesse.Arguments)}
	 * <p>
	 * We need to discover the FitNesse thread running (which is not exposed either).
	 * This is not a daemon thread, but we need to join() it all the same,
	 * as Maven calls System.exit() once it's business is done.
	 */
	@Override
    protected final void executeInternal(final Launch... launches)
	        throws MojoExecutionException, MojoFailureException {
    	final String portString = this.port.toString();
        try {
        	Runtime.getRuntime().addShutdownHook(new Interrupter(Thread.currentThread(), 0L));
            this.fitNesseHelper.launchFitNesseServer(portString, this.workingDir, this.root, this.logDir);
    		if(this.createSymLink) {
	            this.fitNesseHelper.createSymLink(
	            		this.project.getBasedir(), this.testResourceDirectory, this.port, launches);
    		}
            final Thread fitnesseThread = findFitNesseServerThread();
            if(fitnesseThread != null) {
            	getLog().info("FitNesse wiki server launched.");
                fitnesseThread.join();
            }
        } catch (InterruptedException e) {
        	getLog().info("FitNesse wiki server interrupted!");
        } catch (Exception e) {
            throw new MojoExecutionException("Exception launching FitNesse", e);
        } finally {
        	this.fitNesseHelper.shutdownFitNesseServer(portString);
           	getLog().info("FitNesse wiki server is shutdown.");
        }
    }
    
    private Thread findFitNesseServerThread() {
    	final Thread[] activeThreads = findActiveThreads(3);
    	for( int i = activeThreads.length - 1 ; i >= 0 ; i-- ) {
    		final StackTraceElement[] trace = activeThreads[i].getStackTrace();
            for( int j = trace.length - 1 ; j >= 0 ; j-- ) {
            	if(FITNESSE_SOCKET_SERVICE.equals(trace[j].getClassName())) {
            		return activeThreads[i];
            	}
            }
    	}
       	getLog().warn("Could not identify FitNesse service Thread.");
    	return null;
    }
    
    private Thread[] findActiveThreads(final int arraySize) {
    	final Thread[] activeThreads = new Thread[arraySize];
    	final int threadsFound = Thread.currentThread().getThreadGroup().enumerate(activeThreads, false);
    	if(threadsFound < arraySize) {
        	final Thread[] foundThreads = new Thread[threadsFound];
        	System.arraycopy(activeThreads, 0, foundThreads, 0, threadsFound);
        	return foundThreads;
    	} 
    	return findActiveThreads(arraySize + arraySize);
    }
}
