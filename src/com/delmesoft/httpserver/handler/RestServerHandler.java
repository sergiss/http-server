package com.delmesoft.httpserver.handler;

import java.util.HashSet;
import java.util.Set;

import com.delmesoft.httpserver.HttpListener;
import com.delmesoft.httpserver.HttpRequest;
import com.delmesoft.httpserver.HttpResponse;

public class RestServerHandler implements HttpListener {
	
	private Set<String> indexSet = new HashSet<>();

	@Override
	public HttpResponse onHttpRequest(HttpRequest httpRequest) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getIndexSet() {
		return indexSet;
	}

	public void setIndexSet(Set<String> indexSet) {
		this.indexSet = indexSet;
	}

}
