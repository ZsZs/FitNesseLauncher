package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

class OkHandler extends AbstractHandler {

	private String expectedRequestUri;

	private String expectedQueryString;

	public OkHandler(String expectedRequestUri, String expectedQueryString) {
		this.expectedRequestUri = expectedRequestUri;
		this.expectedQueryString = expectedQueryString;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		assertEquals(expectedRequestUri, request.getRequestURI());
		assertEquals(expectedQueryString, request.getQueryString());

		response.addHeader("Server", "FitNesse");
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}
}
