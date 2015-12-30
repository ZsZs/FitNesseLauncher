package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class SetUpMojoTest {

	private SetupsMojoTestHelper helper;
	
    private SetUpMojo mojo;
	
	@Before
	public void setUp() throws Exception {
		
		helper = new SetupsMojoTestHelper(new SetUpMojo());
		mojo = (SetUpMojo) helper.mojo;
		
        helper.setupArtifact(FitNesse.groupId, FitNesse.artifactId, null, "jar");
		helper.setupArtifact("org.apache.maven.plugins", "maven-clean-plugin", "clean", "maven-plugin");
        helper.setupArtifact("org.apache.maven.plugins", "maven-dependency-plugin", "unpack", "maven-plugin");
		helper.setupArtifact("org.apache.maven.plugins", "maven-antrun-plugin", "run", "maven-plugin");
	}
	
	@After
	public void tearDown() throws Exception {
		FileUtils.deleteQuietly(helper.workingDir);
	}
	
	@Test
	public void testClean() throws Exception {
		
        final Xpp3Dom cleanConfig = Xpp3DomBuilder.build(SetUpMojoTest.class.getResourceAsStream("setup-clean-mojo-config.xml"), "UTF-8");
        cleanConfig.getChild("filesets").getChild(0).getChild("directory").setValue(helper.workingDir.getCanonicalPath());
        
        doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(cleanConfig, 
				    ((MojoExecution) invocation.getArguments()[1]).getConfiguration());
				return null;
			}
        }).when(mojo.pluginManager).executeMojo(eq(mojo.session), any(MojoExecution.class));
		
		mojo.clean();
	}
	
	@Test
	public void testUnpack() throws Exception {
		
        final Xpp3Dom unpackConfig = Xpp3DomBuilder.build(SetUpMojoTest.class.getResourceAsStream("unpack-mojo-config.xml"), "UTF-8");
        unpackConfig.getChild("artifactItems").getChild(0).getChild("outputDirectory").setValue(helper.workingDir.getCanonicalPath());
        unpackConfig.getChild("artifactItems").getChild(1).getChild("outputDirectory").setValue(helper.workingDir.getCanonicalPath() + "/FitNesseRoot/files");
        
        doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(unpackConfig,
				    ((MojoExecution) invocation.getArguments()[1]).getConfiguration());
				return null;
			}
        }).when(mojo.pluginManager).executeMojo(eq(mojo.session), any(MojoExecution.class));
		
		mojo.unpack();
	}
	
	@Test
	public void testMove() throws Exception {
		
        final Xpp3Dom antrunConfig = Xpp3DomBuilder.build(SetUpMojoTest.class.getResourceAsStream("antrun-mojo-config.xml"), "UTF-8");
        // Because the tmp directory differs by OS
        antrunConfig.getChild("target").getChild(0).setAttribute("todir", helper.workingDir.getCanonicalPath());
        antrunConfig.getChild("target").getChild(0).setAttribute("file", helper.workingDir.getCanonicalPath() + "/" + SetUpMojo.FIT_ROOT);
        antrunConfig.getChild("target").getChild(1).setAttribute("todir", helper.workingDir.getCanonicalPath() + "/" + FitNesseHelper.DEFAULT_ROOT + "/files");
        antrunConfig.getChild("target").getChild(1).getChild("fileset").setAttribute("dir",
        		helper.workingDir.getCanonicalPath() + "/" + FitNesseHelper.DEFAULT_ROOT + "/files/" + SetUpMojo.FIT_FILES);
        
        doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				assertEquals(antrunConfig,
				    ((MojoExecution) invocation.getArguments()[1]).getConfiguration());
				return null;
			}
        }).when(mojo.pluginManager).executeMojo(eq(mojo.session), any(MojoExecution.class));
		
		mojo.move();
	}
	
	@Test
	public void testExecute1() throws Exception {
		mojo.execute();
        verify(mojo.pluginManager, times(2)).executeMojo(eq(mojo.session), any(MojoExecution.class));
	}
	
	@Test
	public void testExecute2() throws Exception {
		mojo.deletePluginsProperties = true;
		mojo.execute();
        verify(mojo.pluginManager, times(3)).executeMojo(eq(mojo.session), any(MojoExecution.class));
	}
}
