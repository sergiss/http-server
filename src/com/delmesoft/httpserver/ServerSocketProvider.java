package com.delmesoft.httpserver;

import java.io.InputStream;
import java.net.ServerSocket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public interface ServerSocketProvider {
	
	public static final ServerSocketProvider DEFAULT = new ServerSocketProvider() {
		@Override
		public ServerSocket createServerSocket() throws Exception {
			return new ServerSocket();
		}
	};
	
	/**
	 * Create new SSL ServerSocketProvider instance
	 * @param inputStream the input stream from which the keystore is loaded, or null
	 * @param password the password used to check the integrity of the keystore, the password used to unlock the keystore, or null
	 */
	public static ServerSocketProvider sslInstance(InputStream inputStream, String password) {
		return sslInstance(inputStream, password, password);
	}
	
	/**
	 * Create new SSL ServerSocketProvider instance
	 * @param inputStream the input stream from which the keystore is loaded, or null
	 * @param keyStorePassword the password used to check the integrity of the keystore, the password used to unlock the keystore, or null
	 * @param keyPassword the password for recovering keys in the KeyStore
	 * @return
	 */
	public static ServerSocketProvider sslInstance(InputStream inputStream, String keyStorePassword, String keyPassword) {
		return new ServerSocketProvider() {
			@Override
			public ServerSocket createServerSocket() throws Exception {
				
	            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	            keyStore.load(inputStream, keyStorePassword.toCharArray());
	            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	            keyManagerFactory.init(keyStore, keyPassword.toCharArray());
				TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init(keyStore);
				SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
				sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
				SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
				return sslServerSocketFactory.createServerSocket();
			}
		};
	}

	ServerSocket createServerSocket() throws Exception;

}
