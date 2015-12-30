package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;

import org.apache.maven.plugin.MojoExecutionException;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

/**
 * Cleanup debris after FitNesse runs.
 * See {@link fitnesse.responders.run.formatters.PageInProgressFormatter}
 * and {@link fitnesse.slim.test.TestsInProgress},
 * wherein they have hard-coded the directory where they track test progress.
 * 
 * @goal tear-down
 * @phase post-integration-test
 */
public class TearDownMojo extends AbstractSetupsMojo {

	/**
	 * <pre>
	 * {@code
	 * <plugin>
	 * 		<artifactId>maven-clean-plugin</artifactId>
	 * 		<executions>
	 * 			<execution>
	 * 				<phase>post-integration-test</phase>
	 * 				<goals>
	 * 					<goal>clean</goal>
	 * 				</goals>
	 * 				<configuration>
	 * 					<excludeDefaultDirectories>true</excludeDefaultDirectories>
	 * 					<filesets>
	 * 						<fileset>
	 * 							<directory>FitNesseRoot</directory>
	 * 							<followSymlinks>false</followSymlinks>
	 * 						</fileset>
	 * 					</filesets>
	 * 				</configuration>
	 * 			</execution>
	 * 		</executions>
	 * </plugin>
	 * }
	 * </pre>
	 */
	@Override
	public final void execute() throws MojoExecutionException {
		executeMojo(
			plugin("org.apache.maven.plugins:maven-clean-plugin"),
			goal("clean"),
			configuration(
				element("excludeDefaultDirectories", "true"),
				element("filesets",
					element("fileset",
						element("directory", FitNesseHelper.DEFAULT_ROOT),
						element("followSymlinks", "false")))),
			executionEnvironment(project, session, pluginManager)
		);
	}
}
