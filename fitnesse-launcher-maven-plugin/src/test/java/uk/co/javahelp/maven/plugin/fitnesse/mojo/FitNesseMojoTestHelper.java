package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.Logger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.co.javahelp.maven.plugin.artifact.resolver.OptionalArtifactFilter;
import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class FitNesseMojoTestHelper {

	AbstractFitNesseMojo mojo;
	
	ArtifactHandler artifactHandler;
	
    ArtifactResolver artifactResolver;
    
    Artifact pluginArtifact;
    
    Artifact fitnesseArtifact;
    
    Plugin plugin;
	
    ClassRealm realm;
    
    ByteArrayOutputStream logStream;
    
    Properties systemProperties;
    
    FitNesseMojoTestHelper() {
		this.artifactHandler = mock(ArtifactHandler.class);
		this.artifactResolver = mock(ArtifactResolver.class);
		this.realm = mock(ClassRealm.class);
		
        this.pluginArtifact = new DefaultArtifact(
        	getClass().getPackage().getName(), getClass().getSimpleName(),
        	"version", "scope", "type", "classifier", this.artifactHandler);
        
        this.fitnesseArtifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20130530", "compile", "jar", null, this.artifactHandler);
        this.fitnesseArtifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        
		when(this.artifactResolver.resolve(argThat(new ResolutionRequestForArtifact(this.fitnesseArtifact))))
		    .thenReturn(createArtifactResolutionResult(this.fitnesseArtifact));
        
        this.plugin = new Plugin();
		this.plugin.setGroupId(this.pluginArtifact.getGroupId());
		this.plugin.setArtifactId(this.pluginArtifact.getArtifactId());
		
        Build build = new Build();
        build.addPlugin(this.plugin);
        
		this.mojo = new TestFitNesseMojo();
		this.mojo.project = new MavenProject();
		this.mojo.resolver = this.artifactResolver;
		this.mojo.fitNesseHelper = mock(FitNesseHelper.class);
		this.mojo.useProjectDependencies = new HashSet<String>();
		
		this.mojo.pluginDescriptor = new PluginDescriptor();
		this.mojo.pluginDescriptor.setGroupId(this.pluginArtifact.getGroupId());
		this.mojo.pluginDescriptor.setArtifactId(this.pluginArtifact.getArtifactId());
		this.mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
		this.mojo.pluginDescriptor.setClassRealm(this.realm);
		this.mojo.project.setPluginArtifacts(Collections.singleton(this.pluginArtifact));
		this.mojo.project.setBuild(build);
		this.mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		this.mojo.project.setArtifactId("ARTIFACT_ID");
		this.mojo.project.setVersion("VERSION");
		this.mojo.project.getBuild().setTestOutputDirectory("target/test-classes");
		this.mojo.project.getBuild().setOutputDirectory("target/classes");
		this.mojo.project.setDependencyArtifacts(new HashSet<Artifact>());
		
		this.systemProperties = new Properties();
		this.mojo.session = mock(MavenSession.class);
		when(this.mojo.session.getSystemProperties()).thenReturn(this.systemProperties);
		
        addDependency("cg1", "ca1", Artifact.SCOPE_COMPILE);
        addDependency("cg1", "ca2", Artifact.SCOPE_COMPILE);
        addDependency("cg2", "ca3", Artifact.SCOPE_COMPILE);
		
        addDependency("tg1", "ta1", Artifact.SCOPE_TEST);
        addDependency("tg1", "ta2", Artifact.SCOPE_TEST);
        addDependency("tg2", "ta3", Artifact.SCOPE_TEST);
		
        addDependency("rg1", "ra1", Artifact.SCOPE_RUNTIME);
        addDependency("rg1", "ra2", Artifact.SCOPE_RUNTIME);
        addDependency("rg2", "ra3", Artifact.SCOPE_RUNTIME);
		
        addDependency("pg1", "pa1", Artifact.SCOPE_PROVIDED);
        addDependency("pg1", "pa2", Artifact.SCOPE_PROVIDED);
        addDependency("pg2", "pa3", Artifact.SCOPE_PROVIDED);
		
        addDependency("sg1", "sa1", Artifact.SCOPE_SYSTEM);
        addDependency("sg1", "sa2", Artifact.SCOPE_SYSTEM);
        addDependency("sg2", "sa3", Artifact.SCOPE_SYSTEM);
		
        addDependency("og1", "oa1", Artifact.SCOPE_COMPILE, true);
        addDependency("og1", "oa2", Artifact.SCOPE_COMPILE, true);
        addDependency("og2", "oa3", Artifact.SCOPE_COMPILE, true);
		
		this.logStream = new ByteArrayOutputStream();
		this.mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(this.logStream))));
	}
    
    private void addDependency(String groupId, String artifactId, String scope) {
        addDependency(groupId, artifactId, scope, false);
	}
    
    private void addDependency(String groupId, String artifactId, String scope, boolean optional) {
        final Artifact artifact = createArtifact(groupId, artifactId);
        artifact.setOptional(optional);
		artifact.setScope(scope);
		if(!optional) {
		    this.mojo.project.getArtifacts().add(artifact);
		}
		this.mojo.project.getDependencies().add(createDependecy(groupId, artifactId, scope));
		this.mojo.project.getDependencyArtifacts().add(artifact);
		final ResolutionRequestForArtifact requestMatcher = new ResolutionRequestForArtifact(artifact);
		when(this.artifactResolver.resolve(argThat(requestMatcher))).thenAnswer(new Answer<ArtifactResolutionResult>() {
			@Override
			public ArtifactResolutionResult answer(InvocationOnMock invocation) throws Throwable {
				ArtifactResolutionRequest request = (ArtifactResolutionRequest)invocation.getArguments()[0];
		        if(requestMatcher.matches(request)) {
                    return createArtifactResolutionResult(artifact, request.getCollectionFilter());
		        }
		        // Should never happen?
				return null;
			}
		});
    }
	
	Dependency createDependecy(String groupId, String artifactId, String scope) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(groupId);
		dependency.setArtifactId(artifactId);
		dependency.setScope(scope);
		return dependency;
	}
	
	Artifact createArtifact(String groupId, String artifactId) {
        Artifact artifact = new DefaultArtifact(
            groupId, artifactId, "1.0.0", "compile", "jar", null, artifactHandler);
        artifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        return artifact;
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Artifact artifact) {
	    return createArtifactResolutionResult(Collections.singleton(artifact), (ArtifactFilter) null);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Artifact artifact, ArtifactFilter filter) {
	    return createArtifactResolutionResult(Collections.singleton(artifact), filter);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts) {
	    return createArtifactResolutionResult(artifacts, null, null, null);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, ArtifactFilter filter) {
	    return createArtifactResolutionResult(artifacts, null, null, filter);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, List<Artifact> missingArtifacts) {
	    return createArtifactResolutionResult(artifacts, missingArtifacts, null, null);
	}
	
	ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, ArtifactResolutionException exception) {
	    return createArtifactResolutionResult(artifacts, null, exception, null);
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(
			Collection<Artifact> artifacts, List<Artifact> missingArtifacts,
			ArtifactResolutionException exception, ArtifactFilter filter) {
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		Set<Artifact> strippedArtifacts = new HashSet<Artifact>(artifacts);
		if(OptionalArtifactFilter.INSTANCE.equals(filter)) {
    		Iterator<Artifact> artifactItr = strippedArtifacts.iterator();
    		while(artifactItr.hasNext()) {
    			boolean include = OptionalArtifactFilter.INSTANCE.include(artifactItr.next());
    			if(!include) {
    				artifactItr.remove();
    			}
			}
		}
		result.setArtifacts(new HashSet<Artifact>(strippedArtifacts));
		if(missingArtifacts != null) {
			result.setUnresolvedArtifacts(missingArtifacts);
		}
		if(exception != null) {
			result.addErrorArtifactException(exception);
		}
		return result;
	}
	void classRealmAssertions() {
    	classRealmAssertions(1);
	}
	
	void classRealmAssertions(int artifactCount) {
		verify(this.realm, times(2 + artifactCount)).addURL(any(URL.class));
	    verify(this.realm, times(1)).addURL(argThat(new UrlEndsWith("/target/test-classes/")));
		verify(this.realm, times(1)).addURL(argThat(new UrlEndsWith("/target/classes/")));
		verify(this.realm, times(artifactCount)).addURL(argThat(new UrlEndsWith("/target/test-classes/dummy.jar")));
	}
}
