package uk.co.javahelp.fitnesse.fit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import uk.co.javahelp.fitnesse.Util;

import fit.ColumnFixture;

public class SpringColumnFixture extends ColumnFixture {

	private static final ApplicationContext context = new GenericXmlApplicationContext("classpath:META-INF/spring/*.xml");
	
	public String beanName;
	
	public String methodName;
	
	public String exec() throws SecurityException, NoSuchMethodException {
		return Util.exec(context, beanName, methodName);
	}
}
