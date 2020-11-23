package com.delmesoft.httpserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import com.delmesoft.httpserver.utils.ChunkedOutputStream;

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
public class HttpResponse {
	
	private static final byte[] NEW_LINE = { 13, 10 };
	
	public enum Status {
		SWITCHING_PROTOCOL(101, "Switching Protocol"),
		OK          (200, "OK"), 
		CREATED     (201, "Created"), 
		NO_CONTENT  (204, "No Content"), 
		NOT_MODIFIED(303, "Not Modified"), 
		BAD_REQUEST (400, "Bad Request"), 
		UNAUTHORIZED(401, "Unauthorized"),
		FORBIDDEN   (403, "Forbidden"),
		NOT_FOUND   (404, "Not Found"),
		NOT_ALLOWED (405, "Method Not Allowed"),
		CONFLICT    (409, "Conflict"),
		INTERNAL_SERVER_ERROR(500, "Internal Server Error");
		
		private final int code;
		private final String message;
		
		Status(int code, String message) {
			this.code = code;
			this.message = message;
		}
		public int getCode() {
			return code;
		}
		
		public String getMessage() {
			return message;
		}
	}
	
	private int code;
	private String message;
	
	private Map<String, String> headers;
	private List<Cookie> cookies;
		
	private InputStream content;
	private int contentLength;
	
	private Session session;
	
	public HttpResponse() {
		headers = new HashMap<>();
		cookies = new ArrayList<>();
	}
	
	public HttpResponse(int code, String message) {
		this();
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent(InputStream content) {
		this.content = content;
	}
	
	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	
	protected Map<String, String> getHeaders() {
		return headers;
	}

	protected void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public HttpResponse addHeader(String key, String value) {
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
		
	public List<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}
	
	public HttpResponse addCookie(Cookie cookie) {
		this.cookies.add(cookie);
		return this;
	}
	
	public void write() throws Exception {
		write(session.getOutputStream());
	}

	protected void write(OutputStream os) throws Exception {	
		// write <protocol> <code> <message>
		String line = String.format("%s %d %s\r\n", Constants.PROTOCOL, code, message);
		os.write(line.getBytes());
		writeCookies(os);		
		final boolean gzip = "gzip".equals(headers.get("Content-Encoding"));
		if(!gzip || contentLength == -1) { // without gzip or chunked
			writeHeaders(os, contentLength);
		}
		writeBodyContent(os, gzip);
		os.flush();
	}

	private void writeCookies(OutputStream os) throws IOException {
		if(cookies.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Cookie cookie : cookies) {
				sb.append(String.format("Set-Cookie: %s\r\n", cookie));
			}
			// write cookies
			os.write(sb.toString().getBytes());
		}
	}

	private void writeHeaders(OutputStream os, int contentLength) throws IOException {
		// add Date to headers
		addHeader("Date", Constants.DATE_FORMAT.format(new Date()));
		// add content length to headers
		addHeader("Content-Length", Integer.toString(contentLength));
		// add Transfer-Encoding chunked
		if(contentLength == -1) {
			addHeader("Transfer-Encoding", "chunked");
		}
		// write headers
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : headers.entrySet()) {
			sb.append(String.format("%s: %s\r\n", entry.getKey(), entry.getValue()));
		}
		os.write(sb.toString().getBytes());
		// write empty line <CR><LF>
		os.write(NEW_LINE);
	}

	private final byte[] buffer = new byte[1024 << 4];
	private void writeBodyContent(OutputStream os, boolean gzip) throws IOException {
		if(contentLength != 0) { // if has content
			// write body content
			if(contentLength == -1) { // chunked
				ChunkedOutputStream cos = new ChunkedOutputStream(os);
				if (gzip) { // Encoding gzip
					GZIPOutputStream out = new GZIPOutputStream(cos);
					writeFully(content, out);
					out.finish();
				} else {
					writeFully(content, cos);
				}
				cos.write(buffer, 0, 0); // 0/r/n/r/n
			} else if(gzip) { // Encoding gzip
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final GZIPOutputStream out = new GZIPOutputStream(baos);
				write(content, contentLength, out);
				out.finish();
				final byte[] data = baos.toByteArray();
				int contentLength = data.length;
				writeHeaders(os, contentLength);
				write(new ByteArrayInputStream(data), contentLength, os);
			} else {
				write(content, contentLength, os);
			}
		}
	}

	private void write(InputStream is, int n, OutputStream os) throws IOException {
		int r;
		while (n > 0) {
			r = is.read(buffer);
			if (r < 0)
				throw new EOFException();
			os.write(buffer, 0, r);
			n -= r;
		}
	}

	private void writeFully(InputStream is, OutputStream os) throws IOException {
		int r;
		while ((r = is.read(buffer)) > -1) {
			os.write(buffer, 0, r);
		}
	}
	
	public static HttpResponse build(int code, String message) {
		return new HttpResponse(code, message);
	}
	
	public static HttpResponse build(int code, String message, String contentType, byte[] contentData) {
		HttpResponse result = build(code, message);
		result.getHeaders().put("Content-Type", contentType);
		result.setContent(new ByteArrayInputStream(contentData));
		result.setContentLength(contentData.length);
		return result;
	}
	
	public static HttpResponse build(int code, String message, String contentType, InputStream content, int contentLength) {
		HttpResponse result = build(code, message);
		result.getHeaders().put("Content-Type", contentType);
		result.setContent(content);
		result.setContentLength(contentLength);
		return result;
	}
	
	public static HttpResponse build(Status status) {
		return build(status.code, status.message);
	}

	public static HttpResponse build(Status status, String contentType, byte[] contentData) {
		return build(status.code, status.message, contentType, contentData);
	}
	
	public static HttpResponse build(Status status, String contentType, InputStream content, int contentLength) {
		return build(status.code, status.message, contentType, content, contentLength);
	}
	
}
