package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo;

import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.AbstractFitNesseMojo;
import com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.mojo.Launch;

public class TestFitNesseMojo extends AbstractFitNesseMojo {
   public Launch[] calledWith = null;

   public TestFitNesseMojo() {
      this.launches = new Launch[0];
   }

   @Override
   protected void executeInternal( Launch... launches ) {
      this.calledWith = launches;
   }
}
