package com.processpuzzle.litest.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TextContainsLine extends TypeSafeMatcher<String> {
   private final String lineSearched;

   private TextContainsLine(  final String lineSearched ){
      this.lineSearched = lineSearched;
   }
   
   @Factory public static <T> Matcher<String> containsLine( final String lineSearched ){
      return new TextContainsLine( lineSearched );
   }
   
   @Override public void describeTo( Description description ) {
      description.appendText( lineSearched );
   }

   @Override protected boolean matchesSafely( final String sourceText ) {
      String[]sorceLines = sourceText.split(System.getProperty("line.separator"));
      for( String sourceLine : sorceLines ){
         if( sourceLine.trim().equals( lineSearched.trim() )) return true;
      }
      return false;
   }
}
