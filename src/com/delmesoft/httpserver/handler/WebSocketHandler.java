package com.delmesoft.httpserver.handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import com.delmesoft.httpserver.HttpListener;
import com.delmesoft.httpserver.HttpRequest;
import com.delmesoft.httpserver.HttpResponse;
import com.delmesoft.httpserver.HttpResponse.Status;
import com.delmesoft.httpserver.Session;
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
public abstract class WebSocketHandler implements HttpListener {
	
	public static final String UUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	private Set<String> indexSet = new HashSet<>();
	
	public WebSocketHandler() {
		indexSet.add("/"); // Default
	}
	
	@Override
	public HttpResponse onHttpRequest(HttpRequest httpRequest) throws Exception {
		final String key = httpRequest.getHeader("Sec-WebSocket-Key");
		if (key != null) { // finalPoint is empty || finalPoint contains path
			 if (indexSet.contains(httpRequest.getPath())) {
				HttpResponse response = HttpResponse.build(Status.SWITCHING_PROTOCOL);
				byte[] src = MessageDigest.getInstance("SHA-1").digest((key + UUID).getBytes("UTF-8"));
				String secWebSocketAccept = Base64.getEncoder().encodeToString(src);
				response.addHeader("Sec-WebSocket-Accept", secWebSocketAccept);
				response.addHeader("Upgrade", "websocket");
				response.addHeader("Connection", "Upgrade");
				return response;
			}
		}
		return HttpResponse.build(Status.NOT_FOUND);
	}
	
	// https://developer.mozilla.org/es/docs/Web/API/WebSockets_API/Escribiendo_servidores_con_WebSocket
	@Override
	public void onHttpResponse(HttpResponse httpResponse) throws Exception {

		httpResponse.write();

		if(httpResponse.getCode() == Status.SWITCHING_PROTOCOL.getCode()) {

			final Session session = httpResponse.getSession();
			final InputStream is = session.getInputStream();

			session.getSocket().setSoTimeout(0);
			onOpen(session);

			final byte[] mask = new byte[4];
			byte[] buffer = new byte[125];
			int index = 0;
			try {

				while (true) {

					final byte head = Utils.readByte(is);

					//  RSV1, RSV2, RSV3
					if((head & 0x70) > 0) {
						throw new RuntimeException("RSV1, RSV2, RSV3 must be 0");
					}

					byte dataInfo = Utils.readByte(is);
					int len = (dataInfo & 0x7F); // payload len
					switch (len) { // check Extended payload length 
					case 126:
						len = Utils.readShort(is);
						break;
					case 127:
						len = (int) Utils.readLong(is);
						break;
					}

					if(buffer.length - index < len) { 
						buffer = new byte[index + len]; // resize buffer
					}

					if ((dataInfo & 0x80) == 0x80) { // check mask
						Utils.readFully(is, mask); // read mask
						Utils.readFully(is, buffer, index, len); // read data
						for (int i = 0; i < len; ++i) { // Reading and Unmasking the Data
							buffer[i] ^= mask[i % 4]; // ENCODED[i] ^ MASK[i % 4];
						}
					} else {
						Utils.readFully(is, buffer, index, len); // read data
					}

					index += len;
					if((head & 0x80) == 0x80) { // fin
						switch (head & 0xF) { // opcode
						case 0: // continue frame
							break;
						case 1: // text frame
							onText(new String(buffer, 0, index, "UTF-8"), session);
							break;
						case 2: // binary frame
							onData(buffer, index, session);
							break;
						case 8: // connection close
							return;
						case 9: // ping
							send(buffer, 0, index, (byte) 0xA, true, session); // send pong
							break;
						case 0xA: // pong
							onPong(buffer, index, session);
							break;
						default:
							break;
						}
						index = 0;
					}

				}

			} finally {
				onClose(session);
			}

		}
		
	}
	
	public void onOpen(Session session) {
		// TODO Auto-generated method stub
	}

	public void onClose(Session session) {
		// TODO Auto-generated method stub
	}
	
	private void onPong(byte[] buffer, int length, Session session) {
		// TODO Auto-generated method stub
	}

	public abstract void onText(String text, Session session);

	public abstract void onData(byte[] data, int len, Session session);
	
	public void sendPing(byte[] data, Session session) throws Exception {
		sendPing(data, 0, data.length, session);
	}
	
	public void sendPing(byte[] data, int offset, int len, Session session) throws Exception {
		send(data, offset, len, (byte) 0x9, true, session); // send pong
	}
	
	public void sendText(String text, Session session) throws Exception {
		byte[] data = text.getBytes("UTF-8");
		send(data, 0, data.length, (byte) 1, true, session); // text
	}
	
	public void sendData(byte[] data, Session session) throws Exception {
		sendData(data, 0, data.length, session);
	}
	
	public void sendData(byte[] data, int off, int len, Session session) throws Exception {
		send(data, off, len, (byte) 2, true, session); // binary
	}
	
	private void send(byte[] data, int off, int len, byte opcode, boolean fin, Session session) throws Exception {
		final OutputStream os = session.getOutputStream();
		byte header = opcode;
		if (fin) {
			header |= 0x80;
		}
		os.write(header);
		// payload len
		if (len < 126) {
			os.write((byte) len);
		} else if (len <= 0xFFFF) { // 16 bits
			os.write((byte) 126);
			Utils.writeShort(len, os);
		} else { // 64 bits
			os.write((byte) 126);
			Utils.writeShort(len, os);
		}
		os.write(data, off, len);
		os.flush();
	}

	public Set<String> getIndexSet() {
		return indexSet;
	}

	public void setIndexSet(Set<String> indexSet) {
		this.indexSet = indexSet;
	}
	
}
