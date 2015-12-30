package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.javahelp.maven.plugin.fitnesse.mojo.LaunchTest.assertLaunch;

import java.io.IOException;
import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

public class FitNesseMojoTest {

	private FitNesseMojoTestHelper helper;

	@Before
	public void setUp() {
		helper = new FitNesseMojoTestHelper();
	}

	@Test
	public void testExecute() throws MojoExecutionException,
			MojoFailureException, IOException {
		assertExecute();
		assertEquals(0, ((TestFitNesseMojo) helper.mojo).calledWith.length);
	}

	@Test
	public void testExecuteLaunch() throws MojoExecutionException, MojoFailureException, IOException {
		helper.mojo.launches = new Launch[] { new Launch("testExecuteLaunch", null) };
		assertExecute();
		assertEquals(1, ((TestFitNesseMojo) helper.mojo).calledWith.length);
		assertLaunch("testExecuteLaunch", null, null, null, null,
				((TestFitNesseMojo) helper.mojo).calledWith[0]);
	}

	@Test
	public void testExecuteSuite() throws MojoExecutionException, MojoFailureException, IOException {
		helper.mojo.suite = "testExecuteSuite";
		helper.mojo.suiteFilter = "abc";
		helper.mojo.excludeSuiteFilter = "def";
		helper.mojo.runTestsMatchingAllTags = "xyz";
		assertExecute();
		assertEquals(1, ((TestFitNesseMojo) helper.mojo).calledWith.length);
		assertLaunch("testExecuteSuite", null, "abc", "def", "xyz",
				((TestFitNesseMojo) helper.mojo).calledWith[0]);
	}

	@Test
	public void testExecuteTest() throws MojoExecutionException,
			MojoFailureException, IOException {
		helper.mojo.test = "testExecuteTest";
		helper.mojo.suiteFilter = "abc";
		helper.mojo.excludeSuiteFilter = "def";
		helper.mojo.runTestsMatchingAllTags = "xyz";
		assertExecute();
		assertEquals(1, ((TestFitNesseMojo) helper.mojo).calledWith.length);
		assertLaunch(null, "testExecuteTest", "abc", "def", "xyz",
				((TestFitNesseMojo) helper.mojo).calledWith[0]);
	}

	private void assertExecute() throws MojoExecutionException, MojoFailureException, IOException {
		String expected = IOUtils.toString(FitNesseMojoTest.class.getResourceAsStream("exec-output.log"));

		helper.mojo.execute();

		assertNotNull(((TestFitNesseMojo) helper.mojo).calledWith);

		String actual = format(newlines(helper.logStream.toString(), "%n"));
		assertEquals(format(newlines(expected, ""),
				new File(this.getClass().getResource("/dummy.jar").getFile()),
				helper.mojo.project.getBasedir()),
				actual);
	}

	private String newlines(String string, String token) {
		return string.replaceAll("[\n\r]+", token);
	}
}
