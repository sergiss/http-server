package com.delmesoft.httpserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;

import com.delmesoft.httpserver.HttpResponse.Status;

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
public class HttpClient implements Runnable {
	
	private final long id;
	private final Socket socket;
	private final HttpServerImpl httpServer;
	private boolean connected;
	
	public HttpClient(long id, Socket socket, HttpServerImpl httpServer) {
		this.id = id;
		this.socket = socket;
		this.httpServer = httpServer;
	}

	@Override
	public synchronized void run() {
		if (!isConnected()) {
			try {
				connected = true;
				final InputStream  is = socket.getInputStream();
				final OutputStream os = socket.getOutputStream();
				HttpResponse httpResponse;
				HttpRequest httpRequest = new HttpRequest();
				httpRequest.setRemoteAddress(socket.getInetAddress());
				boolean keepAlive = true;
				while(keepAlive && httpRequest.read(is)) {
					try {
						httpResponse = httpServer.getHttpListener().onHttpRequest(httpRequest);
						keepAlive = handleConnection(httpRequest, httpResponse);
					} catch (Exception e) {
						HttpServer.log.log(Level.WARNING, "Internal Server Error", e);
						httpResponse = HttpResponse.build(Status.INTERNAL_SERVER_ERROR);
						httpResponse.addHeader("Connection", "close");
						keepAlive = false;
					}
					httpResponse.write(os);
				}
			} catch (Exception e) { // ignore
			} finally {
				disconnect();
			}
		}
	}
	
	private boolean handleConnection(HttpRequest httpRequest, HttpResponse httpResponse) {
		String connection = httpResponse.getHeader("Connection");
		if (connection == null) {
			connection = httpRequest.getHeader("Connection");
			if (connection == null || !connection.equals("keep-alive")) {
				httpResponse.addHeader("Connection", "close");
				return false;
			}
			httpResponse.addHeader("Connection", "keep-alive");
			return true;
		}
		return connection.equals("keep-alive");
	}

	public synchronized boolean isConnected() {
		return connected;
	}

	public synchronized void disconnect() {
		if (isConnected()) {
			try {
				socket.close(); // Closing this socket will also close the socket's InputStream and OutputStream
			} catch (Exception ignore) {
			} finally {
				connected = false;
				httpServer.removeClient(this);
			}
		}
	}

	public long getId() {
		return id;
	}

}
