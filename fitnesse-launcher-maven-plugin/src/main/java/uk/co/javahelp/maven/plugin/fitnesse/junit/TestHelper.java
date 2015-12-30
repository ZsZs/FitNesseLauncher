package uk.co.javahelp.maven.plugin.fitnesse.junit;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.Launch;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;

public class TestHelper {
   private final String fitNesseRootPath;
   private final String outputPath;
   private final TestSystemListener resultListener;
   private boolean debug = true;

   public TestHelper( final String fitNesseRootPath, final String outputPath, final TestSystemListener resultListener ) {
      this.fitNesseRootPath = fitNesseRootPath;
      this.outputPath = outputPath;
      this.resultListener = resultListener;
   }

   public TestSummary run( final int port, final Launch... launches ) throws Exception {
      final TestSummary global = new TestSummary();
      for( final Launch launch : launches ){
         global.add( run( launch, port ) );
      }
      return global;
   }

   public TestSummary run( final Launch launch, final int port ) throws Exception {
      MavenJavaFormatter testFormatter = new MavenJavaFormatter( launch.getPageName() );
      testFormatter.setResultsRepository( new MavenJavaFormatter.FolderResultsRepository( this.outputPath ) );
//      testFormatter.setListener( resultListener );
      
      ArrayList<String> commandLineArguments = Lists.newArrayList( "-e", "0", "-o", "-p", String.valueOf( port ), "-d", this.fitNesseRootPath );
      Arguments arguments = new Arguments( commandLineArguments.toArray( new String[commandLineArguments.size()] ));

      Integer exitCode = 0;
      try{
         FitNesseMain fitnesseMain = new FitNesseMain();
         exitCode = fitnesseMain.launchFitNesse( arguments );
      }catch( Exception e ){
         e.printStackTrace( System.out );
         exitCode = 1;
      }
      if( exitCode != null ){
         exit( exitCode );
      }
      
      testFormatter.close();
      return testFormatter.getTotalSummary();
   }

   public void setDebugMode( final boolean enabled ) {
      debug = enabled;
   }
   
   private void exit( int exitCode ) {
      System.exit( exitCode );
   }
}
