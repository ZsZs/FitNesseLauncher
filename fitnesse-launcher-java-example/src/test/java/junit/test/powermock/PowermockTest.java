package junit.test.powermock;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class PowermockTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void test() {
		Iterator itr = Mockito.mock(Iterator.class);
		Mockito.when(itr.hasNext()).thenReturn(true);
		Assert.assertTrue(itr.hasNext());
	}
}
