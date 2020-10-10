package com.delmesoft.httpserver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import com.delmesoft.httpserver.utils.DefaultExecutor;

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
public class HttpServerImpl implements HttpServer {
	
	public static final int DEFAULT_SOCKET_TIMEOUT = 5_000;

	private final Object lock = new Object();
	
	private final String host;
	private final int port;
	
	private long ids;
	
	private HttpListener httpListener;
	private Map<Long, HttpClient> httpClientMap;
	private int socketTimeout;
	
	private ServerSocket serverSocket;
	private Thread connectionThread;
	
	private Executor executor;
	
	public HttpServerImpl(String host, int port) {
		this(host, port, DEFAULT_SOCKET_TIMEOUT);
	}

	public HttpServerImpl(String host, int port, int socketTimeout) {
		this.host = host;
		this.port = port;
		this.socketTimeout = socketTimeout;
		this.httpClientMap = new HashMap<>();
		
		executor = new DefaultExecutor();
	}
	
	@Override
	public void connect() throws HttpException {
		connect(true);
	}

	@Override
	public void connect(boolean daemon) throws HttpException {
		synchronized (lock) {
			if (!isConnected()) {
				try {
					serverSocket = new ServerSocket();
					serverSocket.bind(new InetSocketAddress(InetAddress.getByName(host), port));
					connectionThread(daemon);
				} catch (Exception e) {
					disconnect();
					throw new HttpException("Error connecting Server", e);
				}
			}
		}
	}
	
	private void connectionThread(boolean daemon) {
		connectionThread = new Thread(HttpServerImpl.class.getName()) {
			public void run() {
				try {
					while (!isInterrupted()) {
						final Socket socket = serverSocket.accept();
						if(socketTimeout > 0) {
							socket.setSoTimeout(socketTimeout);
						}
						HttpClient httpClient;
						synchronized (lock) {
							httpClient = new HttpClient(ids++, socket, HttpServerImpl.this);
							httpClientMap.put(httpClient.getId(), httpClient);
						}
						executor.execute(httpClient);
					}
				} catch (Exception e) {
					HttpServer.log.log(Level.SEVERE, "Internal Server Error", e);
				} finally {
					disconnect();
				}
			}
		};
		connectionThread.setDaemon(daemon);
		connectionThread.start();
	}
	
	@Override
	public boolean isConnected() {
		synchronized (lock) {
			return serverSocket != null;
		}
	}

	@Override
	public void disconnect() {
		synchronized (lock) {
			if(isConnected()) {
				try {
					try { serverSocket.close(); } catch (Exception e) {}
					try { connectionThread.interrupt();   } catch (Exception e) {}
				} finally {
					serverSocket = null;
				}
			}
		}
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setHttpListener(HttpListener httpListener) {
		synchronized (lock) {
			this.httpListener = httpListener;
		}
	}

	@Override
	public HttpListener getHttpListener() {
		synchronized (lock) {
			return httpListener;
		}
	}

	public void removeClient(HttpClient httpClient) {
		synchronized (lock) {
			httpClientMap.remove(httpClient.getId());
		}
	}

	public void getHttpClients(List<HttpClient> result) {
		synchronized (lock) {
			result.addAll(httpClientMap.values());
		}
	}

	public int getHttpClientCount() {
		synchronized (lock) {
			return httpClientMap.size();
		}
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
	
}
