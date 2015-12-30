package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ExportPropertiesTest {
	
	private FitNesseMojoTestHelper helper;

	@Before
	public void setUp() {
		System.clearProperty("username");
		System.clearProperty("password");
		
		helper = new FitNesseMojoTestHelper();
		
		when(helper.mojo.fitNesseHelper.formatAndAppendClasspathArtifact(
			any(StringBuilder.class), eq(helper.fitnesseArtifact)))
				.then(new Answer<StringBuilder>() {
					@Override
					public StringBuilder answer(InvocationOnMock invocation) {
						StringBuilder sb = (StringBuilder) invocation.getArguments()[0];
						sb.append("TEST.CLASSPATH\n");
						return sb;
					}
				});
	}
	
	@Test
	public void testExportPropertiesBasic() throws MojoExecutionException {
		
		helper.mojo.exportProperties();
		
	    commonAssertions();
	}
	
	@Test
	public void testExportPropertiesExtraProperties() throws MojoExecutionException {
		helper.mojo.project.getModel().addProperty("username", "batman");
		helper.mojo.project.getModel().addProperty("password", "Holy Mashed Potato!");
		
		helper.mojo.exportProperties();
		
	    commonAssertions();
	    
		assertEquals("batman", System.getProperty("username"));
		assertEquals("Holy Mashed Potato!", System.getProperty("password"));
		
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [username] to [batman]"));
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [password] to [Holy Mashed Potato!]"));
	}
	
	@Test
	public void testExportPropertiesPropertiesOverride() throws MojoExecutionException {
		helper.mojo.project.getModel().addProperty("username", "batman");
		helper.mojo.project.getModel().addProperty("password", "Holy Mashed Potato!");
		
		helper.systemProperties.setProperty("username", "robin");
		helper.systemProperties.setProperty("version", "NOT OVERRIDDEN");
		
		helper.mojo.exportProperties();
		
	    commonAssertions();
	    
		assertEquals("robin", System.getProperty("username"));
		assertEquals("Holy Mashed Potato!", System.getProperty("password"));
		
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [username] to [robin]"));
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [password] to [Holy Mashed Potato!]"));
	}
	
	@Test
	public void testExportPropertiesBlankPropertyIgnored() throws MojoExecutionException {
		
		helper.mojo.project.getModel().addProperty("   ", "   ");
		helper.mojo.project.getModel().addProperty(" ", "notempty");
		helper.mojo.project.getModel().addProperty("notempty", "   ");
		
		helper.mojo.exportProperties();
		
	    commonAssertions();
	}
	
	@Test
	public void testExportPropertiesBasedirException() throws MojoExecutionException {
		
		helper.mojo.project.setFile(null);
		helper.mojo.exportProperties();
		
		assertTrue(helper.logStream.toString().contains(String.format(
		"[ERROR] %njava.lang.NullPointerException%n" +
		"	at uk.co.javahelp.maven.plugin.fitnesse.mojo.AbstractFitNesseMojo.exportProperties(AbstractFitNesseMojo.java:")));
	}
	
	private void commonAssertions() {
		String expectedBasedir = helper.mojo.project.getFile().getParent();
		assertEquals("\nTEST.CLASSPATH\n", System.getProperty("maven.classpath"));
		assertEquals("ARTIFACT_ID", System.getProperty("artifact"));
		assertEquals("VERSION", System.getProperty("version"));
		assertEquals(expectedBasedir, System.getProperty("basedir"));
		assertTrue(helper.logStream.toString().startsWith("[INFO] ------------------------------------------------------------------------"));
		assertTrue(helper.logStream.toString().endsWith(String.format("[INFO] ------------------------------------------------------------------------%n")));
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [maven.classpath] to [\nTEST.CLASSPATH\n]"));
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [artifact] to [ARTIFACT_ID]"));
		assertTrue(helper.logStream.toString().contains("[INFO] Setting FitNesse variable [version] to [VERSION]"));
		assertTrue(helper.logStream.toString().contains(String.format("[INFO] Setting FitNesse variable [basedir] to [%s]", expectedBasedir)));
	    
        helper.classRealmAssertions();
	}
}
