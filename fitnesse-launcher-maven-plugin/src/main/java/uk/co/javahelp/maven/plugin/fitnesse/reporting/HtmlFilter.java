package uk.co.javahelp.maven.plugin.fitnesse.reporting;

import java.io.File;
import java.io.FilenameFilter;

public class HtmlFilter implements FilenameFilter {
	
	public static final FilenameFilter INSTANCE = new HtmlFilter();
	
	private HtmlFilter() {
	}

	@Override
	public boolean accept(final File dir, final String name) {
		return name.endsWith(".html");
	}
}
