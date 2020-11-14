package com.delmesoft.httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import com.delmesoft.httpserver.HttpResponse.Status;
import com.delmesoft.httpserver.webserver.WebServerHandler;

/*
 * Copyright (c) 2020, Sergio S.- sergi.ss4@gmail.com http://sergiosoriano.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *    	
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
public class WebServerHandlerTest {

	public static void main(String[] args) throws HttpException {
		
		String host = "127.0.0.1";
		int port = 8080;

		System.out.printf("HttpServer listening, host: %s, port: %d\n", host, port);
		
		HttpServer httpServer = new HttpServerImpl(host, port);
		WebServerHandler webHandler = new WebServerHandler() {
			@Override
			public HttpResponse handleQuery(HttpRequest httpRequest) {
				String value = httpRequest.getParameters().get("message");
				if("getTitle".equals(value)) {
					return HttpResponse.build(Status.OK, "text/plain", "Fast and lightweight HTTP Server".getBytes());
				}
				return HttpResponse.build(Status.NOT_FOUND);
			}
			@Override
			public InputStream toStream(File file) throws Exception {
				return new FileInputStream(file); // Convert content to input stream
			}
		};
		
		// set web content folder
		webHandler.setContentFolder("WebContent");
		// add context
		webHandler.getIndexMap().put("/", "/index.html"); 
		// set HTTP listener
		httpServer.setHttpListener(webHandler);
		// connect server
		httpServer.connect();
				
		try(Scanner scanner = new Scanner(System.in)){
			System.out.println("Press Enter to exit...");
			scanner.nextLine();
		}
		
		httpServer.disconnect();
				
	}

}
