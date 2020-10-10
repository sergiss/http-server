package com.delmesoft.httpserver;

import java.util.Date;

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
public class Cookie {

	public enum SameSite {
		STRICT("Strict"), LAX("Lax"), NONE("None");
		private final String name;
		SameSite(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}

	private String name;
	private String value;
	private String path;
	private Long maxAge;
	private Date expires;
	private boolean secure;
	private boolean httpOnly;
	private SameSite sameSite;
	
	public Cookie(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public Cookie(String name, String value, String path, Long maxAge) {
		super();
		this.name = name;
		this.value = value;
		this.path = path;
		this.maxAge = maxAge;
	}
	
	public Cookie(String name, String value, String path, Date expires) {
		super();
		this.name = name;
		this.value = value;
		this.path = path;
		this.expires = expires;
	}
	
	public Cookie(String name, String value, String path, Long maxAge, Date expires, boolean secure, boolean httpOnly, SameSite sameSite) {
		super();
		this.name = name;
		this.value = value;
		this.path = path;
		this.maxAge = maxAge;
		this.expires = expires;
		this.secure = secure;
		this.httpOnly = httpOnly;
		this.sameSite = sameSite;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Long maxAge) {
		this.maxAge = maxAge;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	public SameSite getSameSite() {
		return sameSite;
	}

	public void setSameSite(SameSite sameSite) {
		this.sameSite = sameSite;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name).append('=').append(value).append(';');
		if (maxAge != null) {
			builder.append("Max-Age=").append(maxAge).append(';');
		}
		if (expires != null) {
			String value = Constants.DATE_FORMAT.format(expires);
			builder.append("Expires=").append(value).append(';');
		}
		if (path != null) {
			builder.append("Path=").append(path).append(';');
		}
		if (secure) {
			builder.append("Secure").append(';');
		}
		if (httpOnly) {
			builder.append("HttpOnly").append(';');
		}
		if (sameSite != null) {
			builder.append("SameSite=").append(sameSite.name).append(';');
		}
		return builder.toString();
	}

}
