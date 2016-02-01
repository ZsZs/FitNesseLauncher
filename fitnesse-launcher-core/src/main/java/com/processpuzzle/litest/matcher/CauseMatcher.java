package com.processpuzzle.litest.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CauseMatcher extends TypeSafeMatcher<Throwable> {
   private final Class<? extends Throwable> type;
   private final String expectedMessage;

   private CauseMatcher( Class<? extends Throwable> type, String expectedMessage ) {
      this.type = type;
      this.expectedMessage = expectedMessage;
   }
   
   @Factory public static <T> Matcher<Throwable> exceptionOf( Class<? extends Throwable> type, String expectedMessage ){
      return new CauseMatcher( type, expectedMessage ); 
   }

   @Factory public static <T> Matcher<Throwable> exceptionOf( Class<? extends Throwable> type ){
      return new CauseMatcher( type, null ); 
   }
   
   @Override protected boolean matchesSafely( Throwable item ) {
      boolean matches = item.getClass().isAssignableFrom( type );
      if( expectedMessage != null ){
         matches = matches && item.getMessage().contains( expectedMessage );
      }
      return matches;
   }

   @Override public void describeTo( Description description ) {
      description.appendText( "expects type " ).appendValue( type ).appendText( " and a message " ).appendValue( expectedMessage );
   }
}
