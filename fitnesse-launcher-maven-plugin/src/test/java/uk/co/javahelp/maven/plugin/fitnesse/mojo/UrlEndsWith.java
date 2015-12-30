package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.net.URL;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public class UrlEndsWith extends ArgumentMatcher<URL> {

    private final String suffix;

    public UrlEndsWith(String suffix) {
        this.suffix = suffix;
    }

	@Override
    public boolean matches(Object actual) {
        return actual != null && ((URL) actual).toExternalForm().endsWith(suffix);
    }

    public void describeTo(Description description) {
        description.appendText("endsWith(\"" + suffix + "\")");
    }
}
