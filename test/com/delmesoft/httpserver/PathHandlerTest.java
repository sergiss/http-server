package com.delmesoft.httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import com.delmesoft.httpserver.HttpResponse.Status;
import com.delmesoft.httpserver.handler.WebServerHandler;
import com.delmesoft.httpserver.utils.PathHandler;

public class PathHandlerTest {

	public static void main(String[] args) throws HttpException {

		int port = 8080;

		System.out.printf("HttpServer listening at port: %d\n", port);
		
		HttpServer httpServer = new HttpServerImpl(port);
		WebServerHandler webServerHandler = new WebServerHandler() {
			@Override
			public HttpResponse onHttpRequest(HttpRequest httpRequest) throws Exception {
				// Rest API example
				if(!httpRequest.getParameters().isEmpty()) {
					String value = httpRequest.getParameters().get("message");
					if("getTitle".equals(value)) {
						return HttpResponse.build(Status.OK, "text/plain", "Fast and lightweight HTTP Server".getBytes());
					}
				}
				// Web Server
				return super.onHttpRequest(httpRequest);
			}
			
			@Override
			public InputStream toStream(File file) throws Exception {
				return new FileInputStream(file); // Convert content to input stream
			}
		};
		
		// set web content folder
		webServerHandler.setContentFolder("WebContent");
		// add index page
		webServerHandler.setIndexPage("index.html"); 
		
		PathHandler pathHandler = new PathHandler();
		pathHandler.addHttpListener("/test", webServerHandler);
		pathHandler.addHttpListener("/", webServerHandler);
		
		pathHandler.addHttpListener("/test/services/v1", new HttpListener() {

			@Override
			public HttpResponse onHttpRequest(HttpRequest httpRequest) throws Exception {
				System.out.println("holaaaaaaaaaaaa");
				return HttpResponse.build(Status.OK);
			}
			
		});
		
		// set HTTP listener
		httpServer.setHttpListener(pathHandler);
		
		// SSL example
		// ((HttpServerImpl) httpServer).setServerSocketProvider(ServerSocketProvider.sslInstance(WebServerHandlerTest.class.getResourceAsStream("keystore/keystore.jks"), "password"));
		
		// connect server
		httpServer.connect();
				
		try(Scanner scanner = new Scanner(System.in)){
			System.out.println("Press Enter to exit...");
			scanner.nextLine();
		}
		
		httpServer.disconnect();
				
	}

}
