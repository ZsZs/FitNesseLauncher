package uk.co.javahelp.maven.plugin.fitnesse.reporting;

import java.io.File;

import org.apache.maven.plugins.surefire.report.AbstractSurefireReportMojo;
import org.apache.maven.project.MavenProject;

/**
 * @goal surefire-report
 * @phase site
 */
public class FitNesseSurefireReport extends AbstractSurefireReportMojo {

	/**
	 * See fitnesse-surefire-report.properties
	 */
	private static final String OUTPUT_NAME = "fitnesse-surefire-report";
	
    /**
     * This is where test results go.
     * 
     * @parameter property="fitnesse.results" default-value="${project.build.directory}/fitnesse/results"
     * @required
     */
    protected File resultsDir;


	@Override
	protected File getSurefireReportsDirectory(MavenProject subProject) {
		return this.resultsDir;
	}

	@Override
	public String getOutputName() {
		return OUTPUT_NAME;
	}

}
