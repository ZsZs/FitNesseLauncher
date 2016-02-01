package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import static com.processpuzzle.litest.matcher.CauseMatcher.exceptionOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.processpuzzle.fitnesse.launcher.maven.plugin.rule.NoLogRule;

public class VerifyMojoTest extends MojoTest {
   private VerifyMojo mojo;
   @Rule public NoLogRule noLoggingRule = new NoLogRule();
   @Rule public final ExpectedException thrown = ExpectedException.none();

   @Before @Override public void beforeEachTest() throws Exception {
      super.beforeEachTest();
      noLoggingRule.setLog( log );
      mojo = new VerifyMojo();
      mojo.setLog( log );
   }

   @Test public void testSuccess() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-success.xml" ).getPath() );
      mojo.execute();
   }

   @Test public void testFailure() throws Exception {
      thrown.expect( MojoFailureException.class );
      thrown.expectMessage( equalTo( "There are test failures.\n\n" + String.format( "Please refer to %s for the individual test results.", mojo.reportsDir ) ) );
      mojo.summaryFile = new File( getClass().getResource( "verify-failure.xml" ).getPath() );

      mojo.execute();
   }

   @Test public void testNoTests() throws Exception {
      mojo.summaryFile = new File( getClass().getResource( "verify-no-tests.xml" ).getPath() );
      mojo.execute();
   }

   @Test public void testBadXml() throws Exception {
      thrown.expect( MojoExecutionException.class );
      thrown.expectMessage( startsWith( "org.xml.sax.SAXParseException" ) );
      thrown.expectMessage( endsWith( "XML document structures must start and end within the same entity." ) );

      mojo.summaryFile = new File( getClass().getResource( "verify-bad-xml.xml" ).getPath() );
      mojo.execute();
   }

   @Test public void testBadXml2() throws Exception {
      thrown.expect( MojoExecutionException.class );
      thrown.expectCause( exceptionOf( NullPointerException.class ) );

      mojo.summaryFile = new File( getClass().getResource( "verify-not-failsafe.xml" ).getPath() );
      mojo.execute();
   }
}
