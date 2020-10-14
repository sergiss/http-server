package com.delmesoft.httpserver;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.delmesoft.httpserver.HttpResponse.Status;
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
public abstract class WebHandler implements HttpListener {
	
	public static final String DEFAULT_CONTENT_FOLDER = "WebContent";
	public static final String DEFAULT_INDEX = "/index.html";
		
	private String contentFolder;
	private Map<String, String> indexMap;
	
	private int gzipMinLength = 2048; // min content length for enable gzip
	
	public WebHandler() {
		this(DEFAULT_CONTENT_FOLDER);
	}

	public WebHandler(String contentFolder) {
		this.contentFolder = contentFolder;
		this.indexMap = new HashMap<>();
		indexMap.put("/", DEFAULT_INDEX); // Default
	}

	@Override
	public HttpResponse onHttpRequest(HttpRequest httpRequest) throws Exception {
		if (httpRequest.getParameters().isEmpty() 
		&& "GET".equals(httpRequest.getMethod())) {
			final String path = httpRequest.getPath();
			final String index = indexMap.get(path);
			final File file = new File(contentFolder, index != null ? index : path);
			return obtainResponse(httpRequest, file);
		} else {
			return handleQuery(httpRequest);
		}
	}
	
	protected HttpResponse obtainResponse(HttpRequest httpRequest, File file) throws Exception {
		final InputStream is = toStream(file);
		if (is != null) {
			String mimeType = Utils.getMimeType(file.getName());
			HttpResponse response;
			if(Utils.contains(httpRequest.getHeader("Accept-Encoding"), "gzip") && is.available() > gzipMinLength) {
				response = HttpResponse.build(Status.OK, mimeType, is, -1);
				response.addHeader("Content-Encoding", "gzip");
			} else {
				response = HttpResponse.build(Status.OK, mimeType, is, is.available());
			}
			return response;
		}
		return HttpResponse.build(Status.NOT_FOUND);
	}

	public abstract InputStream toStream(File file) throws Exception;

	public abstract HttpResponse handleQuery(HttpRequest httpRequest);

	public String getContentFolder() {
		return contentFolder;
	}

	public void setContentFolder(String contentFolder) {
		this.contentFolder = contentFolder;
	}

	public Map<String, String> getIndexMap() {
		return indexMap;
	}

	public void setIndexMap(Map<String, String> indexMap) {
		this.indexMap = indexMap;
	}

	public int getGzipMinLength() {
		return gzipMinLength;
	}

	public void setGzipMinLength(int gzipMinLength) {
		this.gzipMinLength = gzipMinLength;
	}

}
