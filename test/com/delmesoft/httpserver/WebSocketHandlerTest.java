package com.delmesoft.httpserver;

import java.util.Arrays;
import java.util.Scanner;

import com.delmesoft.httpserver.websocket.WebSocketHandler;

public class WebSocketHandlerTest {

	public static void main(String[] args) throws HttpException {

		String host = "127.0.0.1";
		int port = 8081;

		System.out.printf("Websocket Server listening, host: %s, port: %d\n", host, port);
		
		HttpServer httpServer = new HttpServerImpl(host, port);
		
		// set HTTP listener
		httpServer.setHttpListener(new WebSocketHandler() {

			@Override
			public void onOpen(Session session) {
				System.out.println("onOpen: " + session);
			}
			@Override
			public void onClose(Session session) {
				System.out.println("onClose: " + session);
			}
			
			@Override
			public void onData(byte[] data, int len, Session session) {
				try { // Echo data test
					byte[] tmp = new byte[len];
					System.arraycopy(data, 0, tmp, 0, len);
					System.out.printf("onText(id: %d): %s\n", session.getId(), Arrays.toString(tmp));
					sendData(data, 0, len, session);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onText(String text, Session session) {
				try { // Echo text test
					System.out.printf("onText(id: %d): %s\n", session.getId(), text);
					sendText(text, session); // echo test
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		
		// connect server
		httpServer.connect();
				
		try(Scanner scanner = new Scanner(System.in)){
			System.out.println("Press Enter to exit...");
			scanner.nextLine();
		}
		
		httpServer.disconnect();
		
	}

}
