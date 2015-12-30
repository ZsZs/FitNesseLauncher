package uk.co.javahelp.maven.plugin.fitnesse.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

class DisconnectingHandler extends AbstractHandler {

	private Server server;

	public DisconnectingHandler(Server server) {
		this.server = server;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {
			server.stop();
		} catch (Exception e) {
			// Swallow
		}
	}
}
