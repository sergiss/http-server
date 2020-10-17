package com.delmesoft.httpserver;

import java.io.EOFException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.delmesoft.httpserver.utils.LineReader;
import com.delmesoft.httpserver.utils.Utils;

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
public class HttpRequest {
	
	private String method;
	private String path;
	private String protocol;

	private Map<String, String> headers;
	private Map<String, String> parameters;
	private Map<String, String> cookies;
	
	private byte[] body;
	
	private InetSocketAddress remoteAddress;
	
	private final transient LineReader lineReader;
	
	public HttpRequest() {
		headers    = new HashMap<>();
		parameters = new HashMap<>();
		cookies    = new HashMap<>();
		lineReader = new LineReader();
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	protected Map<String, String> getHeaders() {
		return headers;
	}

	protected void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public HttpRequest addHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}
	
	public String getHeader(String key) {
		String result = headers.get(key);
		if(result == null && key != null) {
			result = headers.get(key.toLowerCase());
		}
		return result;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	
	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/**
	 * Decode HTTP request
	 * @param is Client InputStream
	 * @return true if connection must remain open
	 * @throws Exception Connection error
	 */
	public boolean read(InputStream is) throws Exception {
		String line = lineReader.readLine(is);
		if(line == null) { 
			return false;
		}
		String[] requestParam = new String[2];
		int j, i, index = 0;
		for (j = 0, i = 0; i < line.length(); ++i) {
			if (line.charAt(i) == 32) { // 32 = ASCII code (white space)
				requestParam[index++] = line.substring(j, i);
				j = i + 1;
			}
		}
		protocol = line.substring(j, i);
		method   = requestParam[0];
		path     = requestParam[1]; 
		// Read headers
		headers.clear();
		while((line = lineReader.readLine(is)) != null 
		   && (index = line.indexOf(':')) > -1) {
			String key   = line.substring(0, index);
			String value = line.substring(index + 2);
			headers.put(key, value);
		}
		// Cookies
		cookies.clear();
		String value;
		if((value = getHeader("Cookie")) != null) {
			Utils.stringToMap(value, ";", cookies);
		}
		// Handle method
		parameters.clear();
		switch (method) {
		case "GET":  // retrieve data
		case "HEAD": // retrieve only head 
			index = path.indexOf('?');
			if (index > -1) { // check if has parameters
				String query = path.substring(index + 1);
				Utils.stringToMap(query, "&", parameters);
				path = path.substring(0, index); // remove parameters from path
			}
			break;
		case "POST": // modify/update resource
		case "PUT":  // create resource
			
			// TODO : Multipart Implement https://developer.mozilla.org/es/docs/Web/HTTP/Headers/Content-Type
			
			value = getHeader("Content-Length");
			final int contentLength = value != null ? Integer.parseInt(value) : 0; // body length
			byte[] body = new byte[contentLength];
			int n = 0;
			while (n < contentLength) { // read all bytes
				int count = is.read(body, n, contentLength - n);
				if (count < 0)
					throw new EOFException(); // connection closed
				n += count;
			}
			String contentType = getHeader("Content-Type");
			if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
				String query = new String(body, 0, contentLength, "UTF-8");
				Utils.stringToMap(query, "&", parameters);
			} else if (contentLength > 0) {
				this.body = body;
			}
			
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HttpRequest [method=");
		builder.append(method);
		builder.append(", path=");
		builder.append(path);
		builder.append(", protocol=");
		builder.append(protocol);
		builder.append(", headers=");
		builder.append(headers);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append(", cookies=");
		builder.append( cookies);
		builder.append("]");
		return builder.toString();
	}
	
}
