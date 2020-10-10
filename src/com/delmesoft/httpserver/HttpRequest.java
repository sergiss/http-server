package com.delmesoft.httpserver;

import java.io.EOFException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.delmesoft.httpserver.utils.LineReader;

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

	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> parameters = new HashMap<>();
	
	private final transient LineReader lineReader;
	
	public HttpRequest() {
		headers    = new HashMap<>();
		parameters = new HashMap<>();
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

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
//	public HttpRequest addHeader(String key, String value) {
//		headers.put(key, value);
//		return this;
//	}
	
	public String getHeader(String key) {
		return headers.get(key);
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
		parameters.clear();
		switch (method) {
		case "GET":    // retrieve data
			index = path.indexOf('?');
			if (index > -1) { // check if has parameters
				String query = path.substring(index + 1);
				queryToMap(query, parameters);
				path = path.substring(0, index);
			}
			break;
		case "POST": // modify/update resource
		case "PUT":  // create resource
			int length = Integer.parseInt(headers.get("Content-Length")); // body length
			byte[] body = new byte[length];
			int n = 0;
			while (n < length) { // read all bytes
				int count = is.read(body, n, length - n);
				if (count < 0)
					throw new EOFException(); // connection closed
				n += count;
			}
			queryToMap(new String(body, 0, length, "UTF-8"), parameters);
			break;
		default:
			break;
		}
		return true;
	}
	
	private Map<String, String> queryToMap(String query, Map<String, String> result) throws Exception {
		if(query != null) {
			int idx;
			for (String pair : query.split("&")) {
				idx = pair.indexOf('=');
				if(idx > -1) {
					result.put(URLDecoder.decode(pair.substring(0, idx) , "UTF-8"),
							   URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
				} else {
					result.put(URLDecoder.decode(pair, "UTF-8"), null);
				}
			}
		}
		return result;
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
		builder.append("]");
		return builder.toString();
	}
	
}
