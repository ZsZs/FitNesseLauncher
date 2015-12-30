package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;

public class CalcWikiFormatClasspathTest {
	
	private static final String USING_PLUGIN_CONFIG = format("[INFO] Using dependencies specified in plugin config%n");

	private FitNesseMojoTestHelper helper;
	
	@Before
	public void setUp() {
		helper = new FitNesseMojoTestHelper();
	}
	
	@Test
	public void testStandAloneMode() throws MojoExecutionException {
		helper.mojo.project.getBuild().getPlugins().clear();
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(format("[INFO] Running standalone - launching vanilla FitNesse%n"), helper.logStream.toString());
	    
	    helper.classRealmAssertions();
	}
	
	@Test
	public void testNoDependenciesNoFitNesseArtifact() throws MojoExecutionException {
		helper.mojo.pluginDescriptor.setArtifacts(null);
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(format("[ERROR] Lookup for artifact [org.fitnesse:fitnesse] failed%n"), helper.logStream.toString());
	    
	    helper.classRealmAssertions(0);
	}
	
	@Test
	public void testNoDependenciesNoFitNesseJarFile() throws MojoExecutionException {
		
        helper.fitnesseArtifact.setFile(null);
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(format("[WARNING] File for artifact [org.fitnesse:fitnesse:jar:20130530:compile] is not found%n"), helper.logStream.toString());
	    
	    helper.classRealmAssertions(0);
	}
	
	@Test
	public void testNoDependenciesFitNesseOk() throws MojoExecutionException {
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
	    
	    helper.classRealmAssertions();
	}
	
	@Test
	public void testOneDependencyOneArtifact() throws MojoExecutionException {
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(g1a1));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(USING_PLUGIN_CONFIG, helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
	    
	    helper.classRealmAssertions(2);
	}
	
	@Test
	public void testOneDependencyTwoArtifacts() throws MojoExecutionException {
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g1a2 = helper.createArtifact("g1", "a2");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g1a1, g1a2)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(USING_PLUGIN_CONFIG, helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
	    
	    helper.classRealmAssertions(3);
	}
	
	@Test
	public void testTwoDependenciesTwoArtifacts() throws MojoExecutionException {
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g2a1 = helper.createArtifact("g2", "a1");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1, g2a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(g1a1))
		    .thenReturn(helper.createArtifactResolutionResult(g2a1));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(USING_PLUGIN_CONFIG, helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
	    
	    helper.classRealmAssertions(3);
	}
	
	@Test
	public void testMultiDependenciesManyArtifactsEach() throws MojoExecutionException {
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g1a2 = helper.createArtifact("g1", "a2");
        Artifact g1a3 = helper.createArtifact("g1", "a3");
        Artifact g2a1 = helper.createArtifact("g2", "a1");
        Artifact g3a1 = helper.createArtifact("g3", "a1");
        Artifact g3a2 = helper.createArtifact("g3", "a2");
        Artifact g3a3 = helper.createArtifact("g3", "a3");
        Artifact g3a4 = helper.createArtifact("g3", "a4");
        Artifact g3a5 = helper.createArtifact("g3", "a5");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.plugin.addDependency(createDependecy("g3","a3"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g1a1, g1a2, g1a3)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g2a1)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g3a1, g3a2, g3a3, g3a4, g3a5)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(USING_PLUGIN_CONFIG, helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	    
	    helper.classRealmAssertions(10);
	}
	
	@Test
	public void testWithoutDependecyAddedToPluginArtifactsAreNotResolved() throws MojoExecutionException {
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g1a2 = helper.createArtifact("g1", "a2");
        Artifact g1a3 = helper.createArtifact("g1", "a3");
        Artifact g2a1 = helper.createArtifact("g2", "a1");
        Artifact g3a1 = helper.createArtifact("g3", "a1");
        Artifact g3a2 = helper.createArtifact("g3", "a2");
        Artifact g3a3 = helper.createArtifact("g3", "a3");
        Artifact g3a4 = helper.createArtifact("g3", "a4");
        Artifact g3a5 = helper.createArtifact("g3", "a5");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
        // g3 is not added as a dependency
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g1a1, g1a2, g1a3)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g2a1)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g3a1, g3a2, g3a3, g3a4, g3a5)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(USING_PLUGIN_CONFIG, helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	    
	    helper.classRealmAssertions(5);
	}
	
	@Test
	public void testMissingArtifacts() throws MojoExecutionException {
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g1a2 = helper.createArtifact("g1", "a2");
        Artifact g1a3 = helper.createArtifact("g1", "a3");
        Artifact g2a1 = helper.createArtifact("g2", "a1");
        Artifact g3a1 = helper.createArtifact("g3", "a1");
        Artifact g3a2 = helper.createArtifact("g3", "a2");
        Artifact g3a3 = helper.createArtifact("g3", "a3");
        Artifact g3a4 = helper.createArtifact("g3", "a4");
        Artifact g3a5 = helper.createArtifact("g3", "a5");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.plugin.addDependency(createDependecy("g3","a3"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g1a1, g1a3), asList(g1a2)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g2a1)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g3a1, g3a3, g3a4), asList(g3a2, g3a5)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(format(
		    USING_PLUGIN_CONFIG +
		    "[ERROR] Could not resolve artifact: [g1:a2:jar:1.0.0:compile]%n" +
        	"[ERROR] Could not resolve artifact: [g3:a2:jar:1.0.0:compile]%n" +
			"[ERROR] Could not resolve artifact: [g3:a5:jar:1.0.0:compile]%n"), helper.logStream.toString());

		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	    
	    helper.classRealmAssertions(7);
	}
	
	@Test
	public void testArtifactResolutionExceptions() throws MojoExecutionException {
		
		helper.mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_DEBUG, "test", new PrintStream(helper.logStream))));
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g1a2 = helper.createArtifact("g1", "a2");
        Artifact g1a3 = helper.createArtifact("g1", "a3");
        Artifact g2a1 = helper.createArtifact("g2", "a1");
        Artifact g3a1 = helper.createArtifact("g3", "a1");
        Artifact g3a2 = helper.createArtifact("g3", "a2");
        Artifact g3a3 = helper.createArtifact("g3", "a3");
        Artifact g3a4 = helper.createArtifact("g3", "a4");
        Artifact g3a5 = helper.createArtifact("g3", "a5");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.plugin.addDependency(createDependecy("g3","a3"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(helper.createArtifactResolutionResult(helper.fitnesseArtifact))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g1a1, g1a3), new ArtifactResolutionException("TEST", g1a2)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g2a1)))
		    .thenReturn(helper.createArtifactResolutionResult(asList(g3a1, g3a3, g3a4),
	    		new MultipleArtifactsNotFoundException(g3a3, asList(g3a1, g3a4), asList(g3a2, g3a5), null)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		//System.out.print(logStream.toString());
		assertTrue(helper.logStream.toString().contains(format("org.apache.maven.artifact.resolver.ArtifactResolutionException: TEST%n  g1:a2:jar:1.0.0")));
		assertTrue(helper.logStream.toString().contains(format("org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException: Missing:\n----------\n1) g3:a2:jar:1.0.0%n%n")));
		assertTrue(helper.logStream.toString().contains(format("1) g3:a2:jar:1.0.0%n%n  Try downloading the file manually from the project website.")));
		assertTrue(helper.logStream.toString().contains(format("2) g3:a5:jar:1.0.0%n%n  Try downloading the file manually from the project website.")));
		assertTrue(helper.logStream.toString().contains(format("\n----------\n2 required artifacts are missing.\n\nfor artifact: %n  g3:a3:jar:1.0.0")));
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	    
	    helper.classRealmAssertions(7);
	}
	
	@Test
	public void testProjectDependencyScopes() throws MojoExecutionException {
	    assertProjectDependencyScopes(4, "compile");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(4, "test");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(4, "runtime");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(4, "provided");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(4, "system");
	    
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(7, "compile", "test");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(10, "compile", "test", "runtime");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(10, "compile", "runtime", "system");
		helper = new FitNesseMojoTestHelper();
	    assertProjectDependencyScopes(13, "compile", "test", "runtime", "system");
		helper = new FitNesseMojoTestHelper();
		// Let's include the 3 optional dependencies
		helper.mojo.excludeOptionalDependencies = false;
	    assertProjectDependencyScopes(16, true, "compile", "test", "runtime", "system");
	}
	
	private void assertProjectDependencyScopes(int artifactCount, String... scopes) throws MojoExecutionException {
	    assertProjectDependencyScopes(artifactCount, false, scopes);
	}
	
	private void assertProjectDependencyScopes(int artifactCount, boolean optional, String... scopes) throws MojoExecutionException {
		
		helper.mojo.useProjectDependencies = new LinkedHashSet<String>(asList(scopes));
		
		String expectedLogLine2 = format("[INFO] Including transitive dependencies which are optional%n");
		String expectedLog = format("[INFO] Using dependencies in the following scopes: %s%n%s",
				Arrays.toString(scopes), (optional) ? expectedLogLine2 : "");
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(expectedLog, helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(artifactCount))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), any(Artifact.class));
	    
	    helper.classRealmAssertions(artifactCount);
	}
	
	@Test
	public void testPluginDependenciesAndProjectDependencies() throws MojoExecutionException {
		
		helper.mojo.useProjectDependencies = new LinkedHashSet<String>(asList("compile", "test", "runtime"));
		
        Artifact g1a1 = helper.createArtifact("g1", "a1");
        Artifact g2a1 = helper.createArtifact("g2", "a1");
		List<Artifact> artifacts = asList(helper.fitnesseArtifact, g1a1, g2a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(argThat(new ResolutionRequestForArtifact(g1a1))))
		    .thenReturn(helper.createArtifactResolutionResult(g1a1));
		when(helper.artifactResolver.resolve(argThat(new ResolutionRequestForArtifact(g2a1))))
		    .thenReturn(helper.createArtifactResolutionResult(g2a1));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(
				USING_PLUGIN_CONFIG +
				format("[INFO] Using dependencies in the following scopes: [compile, test, runtime]%n"),
				helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(12))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), any(Artifact.class));
	    
	    helper.classRealmAssertions(12);
	}
	
	private static final String WHITESPACE_DIR = "white space test";
	
	@Test
	public void testWhitespaceHandling1() throws IOException {
	    assertWhitespaceHandling(new File(System.getProperty("java.io.tmpdir"), WHITESPACE_DIR));
		assertEquals("", helper.logStream.toString());
	}
	
	@Test
	public void testWhitespaceHandling2() throws IOException {
		File whitespace = new File(new File(".").getCanonicalFile(), WHITESPACE_DIR);
		
		// Save the real user.dir
		String dir = System.getProperty("user.dir");
		whitespace.mkdir();
		System.setProperty("user.dir", whitespace.getCanonicalPath());
	    Build build = assertWhitespaceHandling(whitespace);
	    
		String warning1 = "[WARNING] THERE IS WHITESPACE IN CLASSPATH ELEMENT [%s]%n";
		String warning2 = "Attempting relative path workaround%n";
		assertEquals(format(warning1 + warning2 + warning1 + warning2,
				build.getTestOutputDirectory(), build.getOutputDirectory()),
				helper.logStream.toString());
	    
		// Restore the real user.dir (to prevent side-effects on other tests)
		System.setProperty("user.dir", dir);
	}
	
	private Build assertWhitespaceHandling(File whitespace) throws IOException {
		// Save the real os.name
		String os = System.getProperty("os.name");
		System.setProperty("os.name", "linux");
		
		Build build = helper.mojo.project.getBuild();
		build.setOutputDirectory(mkdir(whitespace, build.getOutputDirectory()));
		build.setTestOutputDirectory(mkdir(whitespace, build.getTestOutputDirectory()));
		helper.mojo.project.setFile(new File(whitespace, "pom.xml"));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
	    
	    helper.classRealmAssertions();
	    
		FileUtils.deleteQuietly(whitespace);
	    
		// Restore the real os.name (to prevent side-effects on other tests)
		System.setProperty("os.name", os);
	    return build;
	}
	
	private String mkdir(File whitespace, String dir) throws IOException {
		File dirFile = new File(whitespace, dir);
		dirFile.mkdirs();
		return dirFile.getCanonicalPath();
	}
	
	private Dependency createDependecy(String groupId, String artifactId) {
	    return helper.createDependecy(groupId, artifactId, null);
	}
}
