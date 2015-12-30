package uk.co.javahelp.maven.plugin.artifact.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

public final class OptionalArtifactFilter implements ArtifactFilter {
	
	public static final OptionalArtifactFilter INSTANCE = new OptionalArtifactFilter();
	
	private OptionalArtifactFilter() {
	}

	@Override
	public boolean include(final Artifact artifact) {
		return !artifact.isOptional();
	}
}
