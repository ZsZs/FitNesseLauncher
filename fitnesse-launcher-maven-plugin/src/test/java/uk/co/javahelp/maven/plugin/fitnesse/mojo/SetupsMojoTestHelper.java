package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.DuplicateMojoDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystemSession;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class SetupsMojoTestHelper {

	AbstractSetupsMojo mojo;
	
    ByteArrayOutputStream logStream;
    
    File workingDir;
	
	public SetupsMojoTestHelper(AbstractSetupsMojo mojo) throws IOException {
		this.workingDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_working");
		
		this.mojo = mojo;
		
		this.mojo.workingDir = this.workingDir.getCanonicalPath();
		this.mojo.root = FitNesseHelper.DEFAULT_ROOT;
		this.mojo.project = new MavenProject();
		this.mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		this.mojo.pluginDescriptor = new PluginDescriptor();
		this.mojo.pluginManager = mock(BuildPluginManager.class);
		this.mojo.session = mock(MavenSession.class);
        
		this.logStream = new ByteArrayOutputStream();
		this.mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(this.logStream))));
	}

	@SuppressWarnings("unchecked")
	void setupArtifact(String groupId, String artifactId, String goal, String type)
	        throws DuplicateMojoDescriptorException, PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException, InvalidPluginDescriptorException {
		
		DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, "DUMMY", "compile", type, "", null);
	    MojoDescriptor mojoDescriptor = new MojoDescriptor();
		mojoDescriptor.setGoal(goal);
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
		pluginDescriptor.addMojo(mojoDescriptor);
		
		Plugin plugin = new Plugin();
		plugin.setGroupId(groupId);
		plugin.setArtifactId(artifactId);
		
        when(this.mojo.pluginManager.loadPlugin(eq(plugin), anyList(), any(RepositorySystemSession.class))).thenReturn(pluginDescriptor);
		
        this.mojo.pluginDescriptor.getArtifactMap().put(String.format("%s:%s", groupId, artifactId), artifact);
	}
}
