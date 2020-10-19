package com.delmesoft.httpserver;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static Pattern CONTENT_DISPOSITION_MATCHER = Pattern.compile("(\s?Content-Disposition\s?:\s?)(.*)");
	
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
					
			final String contentType = getHeader("Content-Type");
			if(contentType != null && contentType.contains("multipart/")) {
				// TODO : Multipart Implement https://developer.mozilla.org/es/docs/Web/HTTP/Headers/Content-Type
				handleMultipart(is, contentType);
			} else {
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
				if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
					String query = new String(body, 0, contentLength, "UTF-8");
					Utils.stringToMap(query, "&", parameters);
				} else if (contentLength > 0) {
					this.body = body;
				}
			}
					
			break;
		default:
			break;
		}
		return true;
	}
	// https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
	private void handleMultipart(InputStream is, String contentType) throws Exception { // TODO : multiple boundaries support
		String line;
		String[] params = contentType.split(";");
		String boundary = params[1].split("=")[1];
		while ((line = lineReader.readLine(is)) != null) {
			if(line.isEmpty()) continue;
			if (!line.contains(boundary)) {
				throw new RuntimeException("Multipart boundary error: " + line);
			}
			if (line.endsWith("--"))
				break; // end
			line = lineReader.readLine(is);
			Matcher matcher = HttpRequest.CONTENT_DISPOSITION_MATCHER.matcher(line);
			String contentDisposition;
			Map<String, String> paramMap = new HashMap<String, String>();
			if (matcher.matches()) {
				String entryGroup = matcher.group(2);
				String[] entries = entryGroup.split(";");
				contentDisposition = entries[0];
				for(int i = 1; i < entries.length; ++i) {
					String tmp = entries[i];
					String[] entry = tmp.split("=");
					String key   = entry[0].trim();
					String value = entry[1].trim();
					paramMap.put(key, value);
				}
			}
			
			line = lineReader.readLine(is);
			String fileName = paramMap.get("filename");
			if(fileName != null) {
				fileName = Utils.removeBoundary(fileName, "\"");
				File file = new File(Constants.TMP_FOLDER, fileName);
				Map<String, String> fileMap = new HashMap<String, String>();
				int index;
				while((line = lineReader.readLine(is)) != null 
				   && (index = line.indexOf(':')) > -1) {
					String key   = line.substring(0, index);
					String value = line.substring(index + 2);
					fileMap.put(key, value);
				}
				String value = fileMap.get("Content-Length");
				final int contentLength = value != null ? Integer.parseInt(value) : 0; // body length
				byte[] buffer = new byte[1024];
				try(OutputStream os = new FileOutputStream(file)) {
					int n = contentLength, r;
					while(n > 0) {
						r = is.read(buffer, 0, Math.min(n, buffer.length));
						if(r < 0) throw new EOFException();
						os.write(buffer, 0, r);
						n -= r;
					}
				}
				parameters.put(fileName, file.getAbsolutePath());
				
			} else {
				String name = paramMap.get("name");
				name = Utils.removeBoundary(name, "\"");
				line = lineReader.readLine(is);
				parameters.put(name, line);
			}

		}
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
