package com.processpuzzle.maven.plugin.fitnesse.reporting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @goal html-report
 * @phase site
 */
public class FitNesseHtmlReport extends AbstractMavenReport {
	
	/**
	 * See fitnesse-html-report.properties
	 */
	private static final String OUTPUT_NAME = "fitnesse-html-report";
	
    /**
     * Directory where reports will go.
     *
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;
 
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
 
    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    /**
     * This is where reports go.
     * 
     * @parameter property="fitnesse.reports" default-value="${project.build.directory}/fitnesse/reports"
     * @required
     */
    protected File reportsDir;

	@Override
	public String getOutputName() {
		return OUTPUT_NAME;
	}

	@Override
	public String getName(Locale locale) {
		return getBundle( locale ).getString( "report.fitnesse.name" );
	}

	@Override
	public String getDescription(Locale locale) {
		return getBundle( locale ).getString( "report.fitnesse.description" );
	}

	@Override
	protected Renderer getSiteRenderer() {
		return this.siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return this.outputDirectory;
	}

	@Override
	protected MavenProject getProject() {
		return this.project;
	}
	
	private ResourceBundle getBundle( Locale locale ) {
	    return ResourceBundle.getBundle( OUTPUT_NAME, locale, this.getClass().getClassLoader() );
	}

	/**
	 * Be aware of http://maven.apache.org/plugin-developers/common-bugs.html#Determining_the_Output_Directory_for_a_Site_Report
	 */
	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
        try {
			String[] reports = this.reportsDir.list(HtmlFilter.INSTANCE);
			//boolean styleDone = false;
				
			ResourceBundle bundle = getBundle( locale );
			Sink sink = this.getSink();
            sinkBeginReport(sink, bundle);
            sink.sectionTitle1();
        	sink.text( bundle.getString( "report.fitnesse.name" ) );
        	sink.sectionTitle1_();
			for(String report : reports) {
                //String html = html(report);
                Document document = Jsoup.parse(new File(this.reportsDir, report), null);
                Elements tables = document.getElementsByTag("table");
                // Is it a top-level index page?
                if(tables.size() == 1 &&
                    testTdElements(tables.get(0).getElementsByTag("td"), "name", "right", "wrong", "exceptions")) {
                    	
                	Elements hrefs = document.getElementsByTag("a");
                	for(Element href : hrefs) {
                		String extant = href.attr("href");
                		href.attr("href", OUTPUT_NAME + "." + extant);
                	}
                    sink.rawText(document.html());
					/*
                	if(!styleDone) {
                        addElements(sink, document.head().getElementsByTag("link"));
                        addElements(sink, document.head().getElementsByTag("script"));
                        styleDone = true;
                	}
                    addElements(sink, document.body().children());
                    */
                	
                } else {
			    	String mavenName = OUTPUT_NAME + "." + report;
			    	createReport(mavenName, locale, document);
                }
			}
            sinkEndReport(sink);
			
			copyResources("css", "images", "javascript");
		} catch (IOException e) {
			getLog().error(e);
			throw new MavenReportException("Exception generating " + getBundle( locale ).getString( "report.fitnesse.name" ), e);
		}
	}
	
	private boolean testTdElements(List<Element> tds, String... expected) {
		if(tds.size() < expected.length) {
			return false;
		}
		for(int i = 0 ; i < expected.length ; i++) {
			if(!expected[i].equalsIgnoreCase(tds.get(i).text())) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private void addElements(Sink sink, List<Element> elements) {
		for(Element element : elements) {
            sink.rawText(element.html());
		}
	}
	
	private void createReport(String reportName, Locale locale, Document document) throws IOException {
        SinkFactory factory = getSinkFactory(); 
        Sink sink = factory.createSink(getReportOutputDirectory(), reportName);
        sinkBeginReport(sink, getBundle( locale ));
        sink.rawText(document.html());
        //addElements(sink, document.head().getElementsByTag("link"));
		//addElements(sink, document.head().getElementsByTag("script"));
		//addElements(sink, document.body().children());
        sinkEndReport(sink);
        sink.close();
	}
	
	@SuppressWarnings("unused")
	private String html(String reportName) throws IOException {
		Reader reader = null;
        try {
			reader = new FileReader(new File(this.reportsDir, reportName));
			return IOUtils.toString(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	private void copyResources(String... resources) throws IOException {
		for(String resource : resources) {
			FileUtils.copyDirectory(new File(this.reportsDir, resource), new File(getReportOutputDirectory(), resource));
		}
	}
	
	private void sinkBeginReport( Sink sink, ResourceBundle bundle ) {
        sink.head();
        String title = bundle.getString( "report.fitnesse.name" );
        sink.title();
        sink.text( title );
        sink.title_();
        sink.head_();
        sink.body();
        sink.section1();
	}
	
    private void sinkEndReport( Sink sink ) {
        sink.section1_();
        sink.body_();
        sink.flush();
        sink.close();
    }
}
