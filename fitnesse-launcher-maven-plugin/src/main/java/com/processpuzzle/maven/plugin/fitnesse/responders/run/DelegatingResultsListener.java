package com.processpuzzle.maven.plugin.fitnesse.responders.run;

import java.io.IOException;
import java.util.ArrayList;

import com.google.common.collect.Lists;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class DelegatingResultsListener implements TestSystemListener {
   private final ArrayList<TestSystemListener> delegates;

   @SafeVarargs
   public DelegatingResultsListener( final TestSystemListener... delegates ) {
      this.delegates = Lists.newArrayList(delegates);
   }

/*   public final void allTestingComplete( final TimeMeasurement totalTimeMeasurement ) throws IOException {
      for( TestSystemListener<PageType> delegate : this.delegates ){
         delegate.allTestingComplete( totalTimeMeasurement );
      }
   }

   public final void announceNumberTestsToRun( final int testsToRun ) {
      for( TestSystemListener<PageType> delegate : this.delegates ){
         delegate.announceNumberTestsToRun( testsToRun );
      }
   }

   public final void errorOccured() {
      for( TestSystemListener<PageType> delegate : this.delegates ){
         delegate.errorOccured();
      }
   }

   public final void newTestStarted( final TestPage test, final TimeMeasurement timeMeasurement ) throws IOException {
      for( TestSystemListener<PageType> delegate : this.delegates ){
         delegate.newTestStarted( test, timeMeasurement );
      }
   }

   public final void setExecutionLogAndTrackingId( final String stopResponderId, final CompositeExecutionLogListener log ) {
      for( TestSystemListener<PageType> delegate : this.delegates ){
         delegate.setExecutionLogAndTrackingId( stopResponderId, log );
      }
   }
*/

   @Override
   public void testAssertionVerified( final Assertion assertion, final TestResult testResult ) {
      for( TestSystemListener delegate : this.delegates ){
         delegate.testAssertionVerified( assertion, testResult );
      }
   }

   @Override
   public final void testComplete( final TestPage testPage, final TestSummary testSummary ) throws IOException {
      for( TestSystemListener delegate : this.delegates ){
         delegate.testComplete( testPage, testSummary );
      }
   }

   @Override
   public void testExceptionOccurred( final Assertion assertion, final ExceptionResult exceptionResult ) {
      for( TestSystemListener delegate : this.delegates ){
         delegate.testExceptionOccurred( assertion, exceptionResult );
      }
   }

   @Override
   public final void testOutputChunk( final String output ) throws IOException {
      for( TestSystemListener delegate : this.delegates ){
         delegate.testOutputChunk( output );
      }
   }

   @Override
   public final void testSystemStarted( final TestSystem testSystem ) throws IOException {
      for( TestSystemListener delegate : this.delegates ){
         delegate.testSystemStarted( testSystem );
      }
   }

   @Override
   public void testStarted( TestPage testPage ) throws IOException {
      for( TestSystemListener delegate : delegates ){
         delegate.testStarted( testPage );
      }
   }

   @Override
   public void testSystemStopped( TestSystem testSystem, Throwable cause ) {
      for( TestSystemListener delegate : delegates ){
         delegate.testSystemStopped( testSystem, cause );
      }
   }
}
