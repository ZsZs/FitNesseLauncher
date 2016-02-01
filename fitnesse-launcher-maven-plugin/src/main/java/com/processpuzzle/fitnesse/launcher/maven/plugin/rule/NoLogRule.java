package com.processpuzzle.fitnesse.launcher.maven.plugin.rule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.maven.plugin.logging.Log;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class NoLogRule extends TestWatcher{
   private Log log;
   private String logTextAtStarting;

   public NoLogRule(){
      this( null );
   }
   
   public NoLogRule( Log log ){
      this.log = log;
   }

   //Properties
   public void setLog( Log log ) { 
      this.log = log; 
      logTextAtStarting = log.toString();
   }

   //Protected, private helper methods
   @Override protected void starting( Description description ) {
      if( log != null ){
         logTextAtStarting = log.toString();
      }
   }

   @Override protected void finished( Description description ) {
      assertThat( logTextAtStarting, equalTo( log.toString() ));
   }
}
