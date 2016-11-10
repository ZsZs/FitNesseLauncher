package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.junit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.Launch;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.reporting.history.JunitReFormatter;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.responders.run.SuiteResponder.HistoryWriterFactory;
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
   @SuppressWarnings( "unused" ) private boolean debug = true;
   private FitNesseContext context;
   private SuiteHistoryFormatter suiteHistoryFormatter;

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
      MavenJavaFormatter htmlResultsFormatter = new MavenJavaFormatter( launch.getPageName() );
      htmlResultsFormatter.setResultsRepository( new MavenJavaFormatter.FolderResultsRepository( this.outputPath ) );

      WikiPage suiteRootPage = determineSuiteRootPage( launch.getPageName() );
      JunitReFormatter xmlResultsFormatter = new JunitReFormatter( context, context.getRootPage(), makeXmlReportWriter(), makeSuiteHistoryFormatter( suiteRootPage ));

      List<WikiPage> pagesToExecute = new SuiteContentsFinder( suiteRootPage, null, context.getRootPage() ).getAllPagesToRunForThisSuite();
      final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem( pagesToExecute, context.getRootPage() );
      MultipleTestsRunner testRunner = new MultipleTestsRunner( pagesByTestSystem, context.testSystemFactory );
      testRunner.addTestSystemListener( resultListener );
      testRunner.addTestSystemListener( htmlResultsFormatter );
      testRunner.addTestSystemListener( suiteHistoryFormatter );
      testRunner.addExecutionLogListener( suiteHistoryFormatter );
      testRunner.addTestSystemListener( xmlResultsFormatter );

      testRunner.executeTestPages();
      htmlResultsFormatter.close();
      return htmlResultsFormatter.getTotalSummary();
   }

   public void setDebugMode( final boolean enabled ) {
      this.debug = enabled;
   }

   private WikiPage determineSuiteRootPage( final String suiteName ) {
      WikiPagePath path = PathParser.parse( suiteName );
      PageCrawler crawler = context.getRootPage().getPageCrawler();
      return crawler.getPage( path );
   }

   private SuiteHistoryFormatter makeSuiteHistoryFormatter( WikiPage page ) {
      if( suiteHistoryFormatter == null ){
         HistoryWriterFactory source = new HistoryWriterFactory();
         suiteHistoryFormatter = new SuiteHistoryFormatter( context, page, source );
      }
      return suiteHistoryFormatter;
   }

   private Writer makeXmlReportWriter() throws IOException {
      OpenOption[] options = { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING };
      BufferedWriter writer = Files.newBufferedWriter( Paths.get( this.outputPath + "/junit-report.xml" ), StandardCharsets.UTF_8, options );
      return writer;
   }
}
