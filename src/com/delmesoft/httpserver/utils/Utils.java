package com.delmesoft.httpserver.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
	
	public static final Random random = new Random();
	
	public static final Map<String, String> mimeTypeMap = new HashMap<>();
	static {
		mimeTypeMap.put(".htm", "text/html");
		mimeTypeMap.put(".html", "text/html");
		mimeTypeMap.put(".js"  , "application/javascript");
		mimeTypeMap.put(".css" , "text/css");
		mimeTypeMap.put(".csv" , "text/csv");
		mimeTypeMap.put(".gif" , "image/gif");
		mimeTypeMap.put(".ico" , "image/x-icon");
		mimeTypeMap.put(".jpeg", "image/jpeg");
		mimeTypeMap.put(".jpg" , "image/jpeg");
		mimeTypeMap.put(".png" , "image/png");
		mimeTypeMap.put(".web" , "image/web");
		mimeTypeMap.put(".svg" , "image/svg+xml");
		mimeTypeMap.put(".json", "application/json");
		mimeTypeMap.put(".xml" , "application/xml");
		mimeTypeMap.put(".pdf" , "application/pdf");
		mimeTypeMap.put(".zip" , "application/zip");
		mimeTypeMap.put(".rar" , "application/x-rar-compressed");
		mimeTypeMap.put(".tar" , "application/x-tar");
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
			/*result = Files.probeContentType(Paths.get(fileName));
			if(result == null) {*/
				result = "application/octet-stream";
			/*}*/
		}
		return result;
	}

	public static boolean contains(String a, String b) {
		return a == null ? false : a.contains(b);
	}
	
	public static void stringToMap(String value, String splitRegex, Map<String, String> result) {
		stringToMap(value, 0, splitRegex, result);
	}
	
	public static void stringToMap(String value, int offset, String splitRegex, Map<String, String> result) {
		if (value != null) {
			int idx;
			value = value.substring(offset);
			for (String pair : value.split(splitRegex)) {
				idx = pair.indexOf('=');
				if (idx > -1) {
					String key = pair.substring(0, idx).trim();
					String val = pair.substring(idx + 1).trim(); 
					result.put(key, val);
				} else {
					result.put(pair, null);
				}
			}
		}
	}
	
	public static void readFully(InputStream inputStream, byte[] data) throws IOException {
		readFully(inputStream, data, 0, data.length);
	}
	
	public static void readFully(InputStream inputStream, byte[] data, int off, int len) throws IOException {
		int r, n = 0;
		while(n < len) {
			r = inputStream.read(data, off + n, len - n);
			if(r < 0) throw new EOFException();
			n += r;
		}
	}

	public static byte readByte(InputStream is) throws IOException {
		int result = is.read();
		if(result < 0) {
			throw new EOFException();
		}
		return (byte) result;
	}

	public static int readShort(InputStream is) throws IOException {
		return readByte(is) << 8 
			| (readByte(is) & 0xFF);
	}

	public static long readLong(InputStream is) throws IOException {
		return readByte(is) << 56 
			| (readByte(is) & 0xFF) << 48 
		    | (readByte(is) & 0xFF) << 40 
			| (readByte(is) & 0xFF) << 32 
			| (readByte(is) & 0xFF) << 24 
			| (readByte(is) & 0xFF) << 16 
			| (readByte(is) & 0xFF) << 8 
			| (readByte(is) & 0xFF);
	}

	public static void writeShort(int value, OutputStream os) throws IOException {
		os.write((byte) (0xFF & (value >>> 8)));
		os.write((byte) (0xFF & value));
	}
	
	public static void writeLong(long value, OutputStream os) throws IOException {
		os.write((byte) (0xFF & (value >>> 56)));
		os.write((byte) (0xFF & (value >>> 48)));
		os.write((byte) (0xFF & (value >>> 40)));
		os.write((byte) (0xFF & (value >>> 32)));
		os.write((byte) (0xFF & (value >>> 24)));
		os.write((byte) (0xFF & (value >>> 16)));
		os.write((byte) (0xFF & (value >>> 8)));
		os.write((byte) (0xFF & value));
	}
	
}
