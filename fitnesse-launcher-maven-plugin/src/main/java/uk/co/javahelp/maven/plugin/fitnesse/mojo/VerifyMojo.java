package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.ReaderFactory;
import org.apache.maven.surefire.suite.RunResult;
import org.codehaus.plexus.util.IOUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fitnesse.slim.SlimServer;

/**
 * Goal that provides summary report on FitNesse tests run with 'run-tests'
 * goal. Intended to be bound to the 'verify' phase. Will fail the build if
 * there are test failures.
 * 
 * @goal verify
 * @phase verify
 */
public class VerifyMojo extends RunTestsMojo {
	
	private static final String HTML = ".html";
	
    private static final String ERR_MSG = "FITNESSE ERROR in %s:%n%s";

    /**
     * @parameter property="fitnesse.maxErrorsToConsole"
     */
    protected int maxErrorsToConsole = 0;

    private int countErrorsToConsole = 0;

	public VerifyMojo() {
		super(false);
	}

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		if(logErrorsToConsole()) {
	        logExceptions();
		}
		final RunResult summary = readSummary();
		SurefireHelper.reportExecution(this, summary, getLog());
	}
	
	private void logExceptions() throws MojoExecutionException {
        final Launch[] launches = super.launches();
        for(int i = 0 ; i < launches.length && logErrorsToConsole() ; i++) {
        	final File indexFile =  new File(this.reportsDir, launches[i].getPageName() + HTML);
        	final Document indexHtml = parseHtml(indexFile);
        	final List<Element> anchors = indexHtml.getElementsByTag("a");
        	for(int j = 0 ; j < anchors.size() && logErrorsToConsole() ; j++) {
        		final String test = anchors.get(j).attr("href");
            	final File testFile =  new File(this.reportsDir, test);
            	final Document testHtml = parseHtml(testFile);
            	final List<Element> errors = testHtml.getElementsByClass("error");
            	for(int k = 0 ; k < errors.size() && logErrorsToConsole() ; k++) {
            		final Element error = errors.get(k);
            		final Elements fitLabel = error.getElementsByClass("fit_label");
            		if(!fitLabel.isEmpty()) {
            			logFitNesseError(test, fitLabel.text());
            		}
            		final Elements fitStacktrace = error.getElementsByClass("fit_stacktrace");
            		if(!fitStacktrace.isEmpty()) {
            			logFitNesseError(test, fitStacktrace.text());
            		}
            	}
            	final List<Element> details = testHtml.getElementsByClass("exception-detail");
            	for(int k = 0 ; k < details.size() && logErrorsToConsole() ; k++) {
            		final Element detail = details.get(k);
            		if(detail.hasText()) {
            			logFitNesseError(test, StringUtils.substringAfter(detail.text(), SlimServer.EXCEPTION_TAG));
            		}
            	}
        	}
        }
	}
	
	private boolean logErrorsToConsole() {
		return (this.countErrorsToConsole < this.maxErrorsToConsole);
	}
	
	private void logFitNesseError(final String testName, final String errorText) {
		if(logErrorsToConsole()) {
		    getLog().error(String.format(ERR_MSG, StringUtils.substringBeforeLast(testName, HTML), errorText));
			this.countErrorsToConsole++;
		}
	}
	
	private Document parseHtml(final File file) throws MojoExecutionException {
       	try {
        	final Document html = Jsoup.parse(file, ReaderFactory.UTF_8);
			return html;
		} catch (final IOException e) {
			throw new MojoExecutionException("IOException: " + file, e);
		} finally {
			// JSoup closes the stream for us
		}
	}

	/**
	 * @see org.apache.maven.plugin.failsafe.VerifyMojo
	 */
	private RunResult readSummary() throws MojoExecutionException {
		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
		try {
			fileInputStream = new FileInputStream(this.summaryFile);
			bufferedInputStream = new BufferedInputStream(fileInputStream);
			return RunResult.fromInputStream(bufferedInputStream, ReaderFactory.UTF_8);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} finally {
			IOUtil.close(bufferedInputStream);
			IOUtil.close(fileInputStream);
		}
	}
}
