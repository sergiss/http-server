package com.delmesoft.httpserver.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
public class Utils {
	
	public static Map<String, String> mimeTypeMap = new HashMap<>();
	static {
		mimeTypeMap.put(".html", "text/html");
		mimeTypeMap.put(".js"  , "application/javascript");
		mimeTypeMap.put(".css" , "text/css");
	}
	
	public static String getMimeType(String fileName) throws Exception {
		int i = fileName.lastIndexOf('.');
		String extension;
		if (i > 0) {
			extension = fileName.substring(i);
			extension = extension.trim();
			extension = extension.toLowerCase();
		} else {
			extension = null;
		}
		String result = mimeTypeMap.get(extension);
		if(result == null) {
			result = Files.probeContentType(Paths.get(fileName));
			if(result == null) {
				result = "application/octet-stream";
			}
		}
		return result;
	}

	public static boolean contains(String a, String b) {
		return a == null ? false : a.contains(b);
	}
	
	public static void stringToMap(String value, String splitRegex, Map<String, String> result) {
		if (value != null) {
			int idx;
			for (String pair : value.split(splitRegex)) {
				idx = pair.indexOf('=');
				if (idx > -1) {
					result.put(pair.substring(0, idx), pair.substring(idx + 1)); // TODO : .trim()?
				} else {
					result.put(pair, null);
				}
			}
		}
	}

	public static String removeBoundary(String value, String boundary) {
		int i = value.startsWith(boundary) ? boundary.length() : 0;
		int j = value.endsWith(boundary) ? value.length() - boundary.length() : value.length();
		return value.substring(i, j);
	}

}
