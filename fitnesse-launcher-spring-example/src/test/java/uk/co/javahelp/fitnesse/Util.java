package uk.co.javahelp.fitnesse;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

public class Util {

	public static String exec(final ApplicationContext context, final String beanName, final String methodName)
	        throws SecurityException, NoSuchMethodException {
		final Object bean = context.getBean(beanName);
		final Method method = bean.getClass().getMethod(methodName);
		return String.valueOf(ReflectionUtils.invokeMethod(method, bean));
	}
}
