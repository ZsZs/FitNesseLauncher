package com.processpuzzle.litest.rule;

import java.util.Locale;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class DefaultLocaleRule extends TestWatcher {
   private Locale originalDefault;
   private Locale currentDefault;

   public DefaultLocaleRule() {
      this( null );
   }

   public DefaultLocaleRule( Locale defaultForTests ) {
      currentDefault = defaultForTests;
   }

   public void setDefault( Locale locale ) {
      if( null == locale ){
         locale = originalDefault;
      }

      Locale.setDefault( locale );
   }

   //Properties
   public static DefaultLocaleRule de() { return new DefaultLocaleRule( Locale.GERMAN ); }
   public static DefaultLocaleRule en() { return new DefaultLocaleRule( Locale.ENGLISH ); }
   public static DefaultLocaleRule fr() { return new DefaultLocaleRule( Locale.FRENCH ); }
   
   //Protected, private helper methods
   @Override
   protected void starting( Description description ) {
      originalDefault = Locale.getDefault();

      if( null != currentDefault ){
         Locale.setDefault( currentDefault );
      }
   }

   @Override
   protected void finished( Description description ) {
      Locale.setDefault( originalDefault );
   }
}
