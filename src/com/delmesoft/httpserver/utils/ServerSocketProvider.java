package com.delmesoft.httpserver.utils;

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
	public static ServerSocketProvider newSslInstance(InputStream inputStream, String password) {
		return newSslInstance(inputStream, password, password);
	}
	
	/**
	 * Create new SSL ServerSocketProvider instance
	 * @param inputStream the input stream from which the keystore is loaded, or null
	 * @param keyStorePassword the password used to check the integrity of the keystore, the password used to unlock the keystore, or null
	 * @param keyPassword the password for recovering keys in the KeyStore
	 * @return
	 */
	public static ServerSocketProvider newSslInstance(InputStream inputStream, String keyStorePassword, String keyPassword) {
		return new ServerSocketProvider() {
			@Override
			public ServerSocket createServerSocket() throws Exception {
	            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // Default keyStore
	            keyStore.load(inputStream, keyStorePassword.toCharArray()); // Loads this KeyStore from the given input stream
	            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); // Default keyManagerFactory
	            keyManagerFactory.init(keyStore, keyPassword.toCharArray()); // Initializes this factory with a source of key material
				TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); // Default trustManagerFactory
				trustManagerFactory.init(keyStore); // Initializes this factory with a source of certificate authorities and related trust material
				SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); // Returns a SSLContext object that implements the specified secure socket protocol
				sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null); // Initializes this context.
				SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory(); // Returns a ServerSocketFactory object forthis context
				return sslServerSocketFactory.createServerSocket(); // Create server socket
			}
		};
	}

	ServerSocket createServerSocket() throws Exception;

}
