package com.processpuzzle.maven.plugin.fitnesse.junit;

import java.util.List;

import com.processpuzzle.maven.plugin.fitnesse.mojo.Launch;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class TestHelper {
   private final String fitNesseRootPath;
   private final String outputPath;
   private final TestSystemListener resultListener;
   private boolean debug = true;
   private FitNesseContext context;

   public TestHelper( final String fitNesseRootPath, final String outputPath, final TestSystemListener resultListener ) {
      this.fitNesseRootPath = fitNesseRootPath;
      this.outputPath = outputPath;
      this.resultListener = resultListener;
   }

   public TestSummary run( final int port, final Launch... launches ) throws Exception {
      ContextConfigurator contextConfigurator = ContextConfigurator.systemDefaults();
      contextConfigurator.withPort( port );
      contextConfigurator.withRootPath( this.fitNesseRootPath );
      context = contextConfigurator.makeFitNesseContext();

      final TestSummary global = new TestSummary();
      
      for( final Launch launch : launches ){
         global.add( run( launch, port ) );
      }
      return global;
   }

   public TestSummary run( final Launch launch, final int port ) throws Exception {
      MavenJavaFormatter testFormatter = new MavenJavaFormatter( launch.getPageName() );
      testFormatter.setResultsRepository( new MavenJavaFormatter.FolderResultsRepository( this.outputPath ) );

      List<WikiPage> pagesToExecute = new SuiteContentsFinder( getSuiteRootPage( launch.getPageName() ), null, context.getRootPage() ).getAllPagesToRunForThisSuite();
      final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem( pagesToExecute, context.getRootPage() );
      MultipleTestsRunner testRunner = new MultipleTestsRunner( pagesByTestSystem, context.testSystemFactory );
      testRunner.addTestSystemListener( resultListener );
      testRunner.addTestSystemListener( testFormatter );
      
      testRunner.executeTestPages();
      testRunner.stop();
      testFormatter.close();
      return testFormatter.getTotalSummary();
      
      /*
       * testFormatter.setListener( resultListener ); ArrayList<String> commandLineArguments = Lists.newArrayList( "-e", "0", "-o", "-p", String.valueOf( port
       * ), "-d", this.fitNesseRootPath ); Arguments arguments = new Arguments( commandLineArguments.toArray( new String[commandLineArguments.size()] ));
       * Integer exitCode = 0; try{ FitNesseMain fitnesseMain = new FitNesseMain(); exitCode = fitnesseMain.launchFitNesse( arguments ); }catch( Exception e ){
       * e.printStackTrace( System.out ); exitCode = 1; } if( exitCode != null ){ exit( exitCode ); }
       */
   }

   public void setDebugMode( final boolean enabled ) {
      debug = enabled;
   }

   private WikiPage getSuiteRootPage( final String suiteName ) {
      WikiPagePath path = PathParser.parse( suiteName );
      PageCrawler crawler = context.getRootPage().getPageCrawler();
      return crawler.getPage( path );
   }

}
