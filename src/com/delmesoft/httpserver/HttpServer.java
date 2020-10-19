package com.delmesoft.httpserver;

import java.util.logging.Logger;

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
public interface HttpServer {
	
	public static final Logger log = Logger.getLogger(HttpServer.class.getName());
	
	/**
	 * Connect Server
	 * @throws HttpException
	 */
	void connect() throws HttpException;

	/**
	 * Connect Server
	 * @param daemon set connection thread daemon
	 * @throws HttpException
	 */
	void connect(boolean daemon) throws HttpException;

	/**
	 * Check if server is connected
	 * @return true if connected
	 */
	boolean isConnected();

	/**
	 * Disconnect Server
	 */
	void disconnect();

	/**
	 * Return listening host
	 * @return host
	 */
	String getHost();

	/**
	 * Return listening port
	 * @return port
	 */
	int getPort();
	
	/**
	 * Set httpListener
	 * @param httpListener
	 */
	void setHttpListener(HttpListener httpListener);
	
	/**
	 * Get httpListener
	 * @return httpListener
	 */
	HttpListener getHttpListener();

}
