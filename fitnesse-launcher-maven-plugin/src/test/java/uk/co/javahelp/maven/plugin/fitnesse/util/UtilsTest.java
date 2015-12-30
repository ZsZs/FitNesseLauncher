package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.apache.commons.io.FilenameUtils.getPrefix;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testIsBlank() {
		assertTrue(Utils.isBlank(null));
		assertTrue(Utils.isBlank(""));
		assertTrue(Utils.isBlank("    "));
		assertTrue(Utils.isBlank("\n"));
		
		assertFalse(Utils.isBlank("."));
		assertFalse(Utils.isBlank(" . "));
		assertFalse(Utils.isBlank("\n."));
	}
	
	@Test
	public void testIsWindows() {
		// Save the real os.name
		String os = System.getProperty("os.name");
		
	    assertIsWindows("Windows XP", true);
	    assertIsWindows("Windows NT", true);
	    assertIsWindows("Windows NT 4.0", true);
	    assertIsWindows("Windows CE 2.0", true);
	    assertIsWindows("Windows 95", true);
	    assertIsWindows("Windows95", true);
	    assertIsWindows("WindowsNT", true);
	    
	    assertIsWindows("Mac OS", false);
	    assertIsWindows("Linux", false);
	    assertIsWindows("Solaris", false);
	    assertIsWindows("OS/2", false);
	    assertIsWindows("MPE/iX", false);
	    assertIsWindows("HP-UX", false);
	    assertIsWindows("AIX", false);
	    assertIsWindows("FreeBSD", false);
	    assertIsWindows("Irix", false);
	    assertIsWindows("Netware 4.11", false);
	    
		// Restore the real os.name (to prevent side-effects on other tests)
		System.setProperty("os.name", os);
	}
	
	private void assertIsWindows(String osname, boolean expected) {
		System.setProperty("os.name", osname);
		assertEquals(expected, Utils.isWindows());
	}
	
	@Test
	public void testGetRelativePath() throws IOException {
		assertGetRelativePath(".",               "/x/y/z",     "/x/y/z");
		assertGetRelativePath("/",               "/a/b/c",     "/");
		assertGetRelativePath("/x/y/z",          "/",          "/x/y/z");
		assertGetRelativePath("/x/y/z",          "/a/b/c",     "/x/y/z");
		assertGetRelativePath("../../y/z",       "/x/a/b",     "/x/y/z");
		assertGetRelativePath("../../../../y/z", "/x/a/b/c/d", "/x/y/z");
		assertGetRelativePath("../z",            "/w/x/y/b",   "/w/x/y/z");
		assertGetRelativePath("..",              "/w/x/y/z",   "/w/x/y");
		assertGetRelativePath("../..",           "/w/x/y/z",   "/w/x");
		assertGetRelativePath("z",               "/w/x/y",     "/w/x/y/z");
		assertGetRelativePath("y/z",             "/w/x",       "/w/x/y/z");
	}
	
	private void assertGetRelativePath(String expected, String from, String to) throws IOException {
		String path = Utils.getRelativePath(new File(from), new File(to));
		String prefix = separatorsToUnix(getPrefix(path));
		if(expected.startsWith("/") && prefix.length() > 0) {
			expected = expected.replaceFirst("/", prefix);
		}
		assertEquals(expected, separatorsToUnix(path));
	}

	@Test
	public void testUtilityClass() throws InstantiationException, IllegalAccessException, InvocationTargetException {
		assertUtilityClass(Utils.class);
	}
	
	/**
	 * http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
	 */
	public static <C> void assertUtilityClass(Class<C> clazz)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		assertTrue("Utility class should be 'final'", Modifier.isFinal(clazz.getModifiers()));
		assertTrue("Utility class should be 'public'", Modifier.isPublic(clazz.getModifiers()));
		Constructor<?>[] ctors = clazz.getDeclaredConstructors();
		assertEquals("Utility class should only have one constructor", 1, ctors.length);
		Constructor<?> ctor = ctors[0];
		assertFalse("Utility class constructor should be inaccessible", ctor.isAccessible());
		ctor.setAccessible(true); // obviously we'd never do this in production
		assertEquals("You'd expect the construct to return the expected type", clazz, ctor.newInstance().getClass());
	}
}
