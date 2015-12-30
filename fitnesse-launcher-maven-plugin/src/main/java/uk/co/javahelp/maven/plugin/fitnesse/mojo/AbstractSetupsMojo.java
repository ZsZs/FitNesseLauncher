package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;

public abstract class AbstractSetupsMojo extends org.apache.maven.plugin.AbstractMojo {
    
    /**
     * @parameter property="plugin"
     * @required
     */
    protected PluginDescriptor pluginDescriptor;
    
    /**
     * Maven project, to be injected by Maven itself.
     * @parameter property="project"
     * @required
     */
    protected MavenProject project;
	
    /**
     * The Maven Session Object
     *
     * @parameter property="session"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * The Maven BuildPluginManager Object
     *
     * @component
     * @required
     */
    protected BuildPluginManager pluginManager;
    
    /**
     * @parameter property="fitnesse.working" default-value="${project.build.directory}/fitnesse"
     */
    protected String workingDir;
    
    /**
     * @parameter property="fitnesse.root" default-value="FitNesseRoot"
     */
    protected String root;

    protected final Plugin plugin(final String key) {
       	final Artifact artifact = this.pluginDescriptor.getArtifactMap().get(key);
        final Plugin plugin = new Plugin();
        plugin.setGroupId(artifact.getGroupId());
        plugin.setArtifactId(artifact.getArtifactId());
        plugin.setVersion(artifact.getVersion());
        return plugin;
    }
}
