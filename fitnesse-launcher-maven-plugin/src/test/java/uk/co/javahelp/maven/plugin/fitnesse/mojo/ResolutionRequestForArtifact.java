package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public class ResolutionRequestForArtifact extends ArgumentMatcher<ArtifactResolutionRequest> {

    private final Artifact artifact;

    public ResolutionRequestForArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

	@Override
    public boolean matches(Object actual) {
        return actual != null && ((ArtifactResolutionRequest) actual).getArtifact().equals(this.artifact);
    }

    public void describeTo(Description description) {
        description.appendText("resolutionRequestFor(\"" + artifact + "\")");
    }
}
