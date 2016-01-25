package com.processpuzzle.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SameTextAs extends TypeSafeMatcher<String> {
   private String canonicalTextExpected;
   
   private SameTextAs( final String textExpected ){
      this.canonicalTextExpected = textExpected.replace( "\r\n", "\n" );
   }
   @Factory
   public static <T> Matcher<String> sameTextAs( final String textExpected ){
      return new SameTextAs( textExpected );
   }
   
   @Override
   public void describeTo( Description description ) {
      description.appendText( "should be the same text as: " ).appendText( canonicalTextExpected );
   }

   @Override
   protected boolean matchesSafely( final String textToCompare ) {
      String canonicalTextToCompare = textToCompare.replace( "\r\n", "\n" );
      return canonicalTextExpected.equals( canonicalTextToCompare );
   }

}
