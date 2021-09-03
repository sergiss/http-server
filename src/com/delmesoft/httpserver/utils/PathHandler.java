package com.delmesoft.httpserver.utils;

import java.util.HashMap;
import java.util.Map;

import com.delmesoft.httpserver.HttpListener;
import com.delmesoft.httpserver.HttpRequest;
import com.delmesoft.httpserver.HttpResponse;
import com.delmesoft.httpserver.HttpResponse.Status;
import com.delmesoft.httpserver.handler.WebServerHandler;

public class PathHandler implements HttpListener {

	private Map<String, HttpListener> listenerMap = new HashMap<>();

	@Override
	public HttpResponse onHttpRequest(HttpRequest httpRequest) throws Exception {
		
		final String path = httpRequest.getPath();
		
		String tmp = "/", key = tmp;
		HttpListener httpListener = listenerMap.get(tmp);
		final String[] tokens = path.split("/");
		for (int i = 1; i < tokens.length; ++i) {
			tmp += tokens[i];
			HttpListener value = listenerMap.get(tmp);
			tmp += "/";
			if (value != null) {
				key = tmp;
				httpListener = value;
			}
		}

		if (httpListener != null) {
			if (httpListener instanceof WebServerHandler) {
				httpRequest.setPath(path.substring(key.length() - 1));
				if (httpRequest.getPath().isEmpty()) {
					httpRequest.setPath("/");
				}
			}
			return httpListener.onHttpRequest(httpRequest);
		}
		
		return HttpResponse.build(Status.NOT_FOUND);
	}

	public void addHttpListener(String path, HttpListener listener) {
		listenerMap.put(path, listener);
	}
	

}
