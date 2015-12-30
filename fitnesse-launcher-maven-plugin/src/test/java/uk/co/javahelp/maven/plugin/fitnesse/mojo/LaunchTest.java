package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LaunchTest {
	
	@Test
	public void testLaunch() {
		
		Launch launch = new Launch("S", null, "sf", "esf", "rtmat");
		assertLaunch("S", null, "sf", "esf", "rtmat", launch);
		assertEquals(Launch.PAGE_TYPE_SUITE, launch.getPageType());
		assertEquals("S?suite&nohistory=true&format=java&suiteFilter=sf&runTestsMatchingAllTags=rtmat&excludeSuiteFilter=esf", launch.getCommand(false));
		assertEquals("S?suite&debug=true&nohistory=true&format=java&suiteFilter=sf&runTestsMatchingAllTags=rtmat&excludeSuiteFilter=esf", launch.getCommand(true));
		assertEquals("suite:S suiteFilter:sf excludeSuiteFilter:esf ", launch.toString());
		assertTrue(launch.hashCode() == new Launch("S", null, "sf", "esf", "rtmat").hashCode());
		assertFalse(launch.hashCode() == new Launch(null, "T", "sf", "esf", "rtmat").hashCode());
		assertTrue(launch.equals(new Launch("S", null, "sf", "esf", "rtmat")));
		assertFalse(launch.equals(new Launch(null, "T", "sf", "esf", "rtmat")));
		assertFalse(launch.equals(new Object()));
		assertFalse(launch.equals(null));
	}

	public static void assertLaunch(String suite, String test, String suiteFilter,
			String excludeSuiteFilter, String runTestsMatchingAllTags,
			Launch launch) {
		assertEquals(suite, launch.getSuite());
		assertEquals(test, launch.getTest());
		assertEquals(suiteFilter, launch.getSuiteFilter());
		assertEquals(excludeSuiteFilter, launch.getExcludeSuiteFilter());
		assertEquals(runTestsMatchingAllTags,
				launch.getRunTestsMatchingAllTags());
	}

	@Test
	public void testCalcPageNameAndTypeSuite() {
		
		Launch launch = new Launch("SuiteName", null);
		assertEquals("SuiteName", launch.getPageName());
		assertEquals(Launch.PAGE_TYPE_SUITE, launch.getPageType());
	}
		
	@Test
	public void testCalcPageNameAndTypeTest() {
		
		Launch launch = new Launch(null, "SuiteName.NestedSuite.TestName");
		assertEquals("SuiteName.NestedSuite.TestName", launch.getPageName());
		assertEquals(Launch.PAGE_TYPE_TEST, launch.getPageType());
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalBoth() {
		try {
			Launch launch = new Launch("SuiteName", "SuiteName.NestedSuite.TestName");
			launch.getPageName();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Suite and test page parameters are mutually exclusive", e.getMessage());
		}
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalNeither() {
	    assertCalcPageNameAndTypeIllegalNeither(null, null);
	    assertCalcPageNameAndTypeIllegalNeither(" ", " ");
	}
	
	private void assertCalcPageNameAndTypeIllegalNeither(String suite, String test) {
		try {
			Launch launch = new Launch(suite, test);
			launch.getPageName();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("No suite or test page specified", e.getMessage());
		}
	}
}
