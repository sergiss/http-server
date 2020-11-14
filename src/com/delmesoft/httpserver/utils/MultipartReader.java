package com.delmesoft.httpserver.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.delmesoft.httpserver.HttpRequest;

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
public class MultipartReader {
	
	// https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html
	public static void handleMultipart(HttpRequest httpRequest, DataListener listener) throws Exception {
		String contentType = httpRequest.getHeader("Content-Type");
		handleMultipart(contentType, httpRequest.getSession().getInputSream(), listener);
	}
	
	private static void handleMultipart(String contentType, InputStream is, DataListener listener) throws Exception {
		LineReader lineReader = new LineReader();
		String line, boundary = unwrap(contentType, "boundary=", ";", true);
		while((line = lineReader.readLine(is)) != null) {
			if(!line.contains(boundary)) {
				throw new RuntimeException("Error handling multipart");
			}
			if(line.endsWith("--")) return;
			Map<String, String> paramMap = new HashMap<String, String>();
			while(!(line = lineReader.readLine(is)).isEmpty()) {
				int off = line.indexOf(";") + 1;
				Utils.stringToMap(line, off, ";", paramMap);
			}
			String name = paramMap.remove("name").trim();
			if(paramMap.containsKey("filename")) {
				listener.onData(name, paramMap, is);
			} else {
				String value = lineReader.readLine(is);
				listener.onData(name, value, paramMap);
			}
		}
		
	}
	
	private static String unwrap(String value, String a, String b, boolean ignoreCase) {
		String tmp;
		if(ignoreCase) {
			tmp = value.toLowerCase();
			a = a.toLowerCase();
			b = b.toLowerCase();
		} else {
			tmp = value;
		}
		int ai = tmp.indexOf(a);
		if(ai == -1) return null;
		ai += a.length();
		int bi = tmp.indexOf(b, ai);
		if(bi == -1) bi = value.length();
		return value.substring(ai, bi);
	}

	public static interface DataListener {
		void onData(String name, String value, Map<String, String> paramMap);
		void onData(String name, Map<String, String> paramMap, InputStream is);
	}

}
