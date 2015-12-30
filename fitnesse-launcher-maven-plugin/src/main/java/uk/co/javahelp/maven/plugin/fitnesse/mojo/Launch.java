package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.util.Arrays;

import uk.co.javahelp.maven.plugin.fitnesse.util.Utils;

/**
 * @see fitnesse.responders.run.SuiteFilter
 * @see fitnesse.junit.TestHelper
 */
public class Launch {
   public static final String PAGE_TYPE_SUITE = fitnesse.junit.JUnitHelper.PAGE_TYPE_SUITE;
   public static final String PAGE_TYPE_TEST = fitnesse.junit.JUnitHelper.PAGE_TYPE_TEST;

   /**
    * @parameter property="fitnesse.suite"
    */
   private String suite;

   /**
    * @parameter property="fitnesse.test"
    */
   private String test;

   /**
    * @see <a href="http://fitnesse.org/FitNesse.FullReferenceGuide.UserGuide.WritingAcceptanceTests.TestSuites.TagsAndFilters">Suite Tags</a>
    * @parameter property="fitnesse.suiteFilter"
    */
   private String suiteFilter;

   /**
    * @see <a href="http://fitnesse.org/FitNesse.FullReferenceGuide.UserGuide.WritingAcceptanceTests.TestSuites.TagsAndFilters">Suite Tags</a>
    * @parameter property="fitnesse.excludeSuiteFilter"
    */
   private String excludeSuiteFilter;

   /**
    * @see <a href="http://fitnesse.org/FitNesse.FullReferenceGuide.UserGuide.WritingAcceptanceTests.TestSuites.TagsAndFilters">Suite Tags</a>
    * @parameter property="fitnesse.runTestsMatchingAllTags"
    */
   private String runTestsMatchingAllTags;

   private String pageName;

   private String pageType;

   public Launch() {}

   public Launch( final String suite, final String test ) {
      this( suite, test, null, null, null );
   }

   public Launch( final String suite, final String test, final String suiteFilter, final String excludeSuiteFilter, final String runTestsMatchingAllTags ) {
      this.suite = suite;
      this.test = test;
      this.suiteFilter = suiteFilter;
      this.excludeSuiteFilter = excludeSuiteFilter;
      this.runTestsMatchingAllTags = runTestsMatchingAllTags;
   }

   public String getSuite() {
      return this.suite;
   }

   public String getTest() {
      return this.test;
   }

   public String getSuiteFilter() {
      return this.suiteFilter;
   }

   public String getExcludeSuiteFilter() {
      return this.excludeSuiteFilter;
   }

   public String getRunTestsMatchingAllTags() {
      return this.runTestsMatchingAllTags;
   }

   public String getPageName() {
      if( this.pageName == null ){
         calcPageNameAndType();
      }
      return this.pageName;
   }

   public String getPageType() {
      if( this.pageType == null ){
         calcPageNameAndType();
      }
      return this.pageType;
   }

   private void calcPageNameAndType() {
      final boolean haveSuite = !Utils.isBlank( this.suite );
      final boolean haveTest = !Utils.isBlank( this.test );
      if( !haveSuite && !haveTest ){
         throw new IllegalArgumentException( "No suite or test page specified" );
      }else if( haveSuite && haveTest ){
         throw new IllegalArgumentException( "Suite and test page parameters are mutually exclusive" );
      }

      this.pageName = (haveSuite) ? this.suite : this.test;
      this.pageType = (haveSuite) ? PAGE_TYPE_SUITE : PAGE_TYPE_TEST;
   }

   private static final String COMMON_ARGS = "&nohistory=true&format=java";

   private static final String DEBUG_ARG = "&debug=true";

   /**
    * @see fitnesse.junit.TestHelper
    */
   public String getCommand( final boolean debug ) {
      final StringBuilder cmdBuilder = new StringBuilder( getPageName() );
      cmdBuilder.append( "?" );
      cmdBuilder.append( getPageType() );
      if( debug ){
         cmdBuilder.append( DEBUG_ARG );
      }
      cmdBuilder.append( COMMON_ARGS );
      if( this.suiteFilter != null ){
         cmdBuilder.append( "&suiteFilter=" );
         cmdBuilder.append( this.suiteFilter );
      }
      if( this.runTestsMatchingAllTags != null ){
         cmdBuilder.append( "&runTestsMatchingAllTags=" );
         cmdBuilder.append( this.runTestsMatchingAllTags );
      }
      if( this.excludeSuiteFilter != null ){
         cmdBuilder.append( "&excludeSuiteFilter=" );
         cmdBuilder.append( this.excludeSuiteFilter );
      }
      return cmdBuilder.toString();
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode( getArray() );
   }

   @Override
   public boolean equals( final Object that ) {
      if( this == that )
         return true;
      if( that == null )
         return false;
      if( !Launch.class.isInstance( that ) )
         return false;
      return Arrays.equals( this.getArray(), ((Launch) that).getArray() );
   }

   private String[] getArray() {
      return toArray( this.suite, this.test, this.suiteFilter, this.excludeSuiteFilter );
   }

   private String[] toArray( final String... array ) {
      return array;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      appendField( sb, "suite", this.suite );
      appendField( sb, "test", this.test );
      appendField( sb, "suiteFilter", this.suiteFilter );
      appendField( sb, "excludeSuiteFilter", this.excludeSuiteFilter );
      return sb.toString();
   }

   private void appendField( final StringBuilder sb, final String name, final String field ) {
      if( field != null ){
         sb.append( name );
         sb.append( ":" );
         sb.append( field );
         sb.append( " " );
      }
   }
}
