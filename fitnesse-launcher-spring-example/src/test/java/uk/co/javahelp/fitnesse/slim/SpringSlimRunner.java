package uk.co.javahelp.fitnesse.slim;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import uk.co.javahelp.fitnesse.Util;

public class SpringSlimRunner {

	private final ApplicationContext context;
	
	private String beanName;
	
	private String methodName;
	
	public SpringSlimRunner(String resources) {
		this.context = new GenericXmlApplicationContext(resources);
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String exec() throws SecurityException, NoSuchMethodException {
		return Util.exec(context, beanName, methodName);
	}
}
