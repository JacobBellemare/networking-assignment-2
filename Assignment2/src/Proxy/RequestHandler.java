package Proxy;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class RequestHandler extends Thread {

	Socket clientSocket;

	InputStream inFromClient;

	OutputStream outToClient;
	
	byte[] request = new byte[1024];

	BufferedReader proxyToClientBufferedReader;

	BufferedWriter proxyToClientBufferedWriter;

	
	private ProxyServer server;


	public RequestHandler(Socket clientSocket, ProxyServer proxyServer) {

		this.clientSocket = clientSocket;
		try {
			clientSocket.setSoTimeout(2000);
			inFromClient = clientSocket.getInputStream();
			outToClient = clientSocket.getOutputStream();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	@Override
	
	public void run() {

		/**
			 * To do
			 * Process the requests from a client. In particular, 
			 * (1) Check the request type, only process GET request and ignore others
			 * (2) If the url of GET request has been cached, respond with cached content
			 * (3) Otherwise, call method proxyServertoClient to process the GET request
			 *
		*/
		//get the request from the client/browser
		String requestString;
		try {
			requestString = proxyToClientBufferedReader.readLine();
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error reading request from client");
			return;
		}
		
		//now Parse out URL successfully read from client/browser
		System.out.println("Request Recieved" + requestString);
		//get the request type
		String requestType = requestString.substring(0, requestString.indexOf(' '));
		//remove the request type and space
		String urlString = requestString.substring(requestString.indexOf(' ')+1);
		//remove everything past next space
		urlString = urlString.substring(0,urlString.indexOf(' '));
		//Prepend http:// if needed to create correct URL
		if(!urlString.substring(0,4).contentEquals("http")) {
			String temp = "http://";
			urlString = temp + urlString;
		}
		
	}

	
	private boolean proxyServertoClient(byte[] clientRequest) throws IOException{
		
		FileOutputStream fileWriter = null;
		Socket serverSocket = null;
		InputStream inFromServer;
		OutputStream outToServer;
		// Create Buffered output stream to write to cached copy of file
		String fileName = "cached/" + generateRandomFileName() + ".dat";
		
		// to handle binary content, byte is used
		byte[] serverReply = new byte[4096];
		
			
		/**
		 * To do
		 * (1) Create a socket to connect to the web server (default port 80)
		 * (2) Send client's request (clientRequest) to the web server, you may want to use fluch() after writing.
		 * (3) Use a while loop to read all responses from web server and send back to client
		 * (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
		 * (5) close file, and sockets.
		*/
		
		//create a socket to the webserver
		
			InetAddress address = InetAddress.getByName("www.cs.ndsu.nodak.edu");
			serverSocket = new Socket(address,80);
			serverSocket.setSoTimeout(5000);
			//send clients request to the web server. use flush after writing.
			inFromServer =  new DataInputStream(clientSocket.getInputStream());
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			fileWriter = new FileOutputStream(clientSocket.getOutputStream());
			//use a while loop to read all responses from webserver and send back to the client
			int read; 
			do { 
				 read = serverSocket.getInputStream().read(clientRequest);
					if (read > 0) {
						clientSocket.getOutputStream().write(serverReply, 0, read);
						
						if (serverSocket.getInputStream().available() < 1) {
							clientSocket.getOutputStream().flush();
						}	 
					}	
			 
			 }while(read >= 0);
				 
			//write the web server's response to a cache file, put the request url and cache
			
			//close the file and the sockets.
		
			serverSocket.close();
		
		return serverSocket.getKeepAlive();
		
		
	
	}
	
	
	
	// Sends the cached content stored in the cache file to the client
	private void sendCachedInfoToClient(String fileName) {

		try {

			byte[] bytes = Files.readAllBytes(Paths.get(fileName));

			outToClient.write(bytes);
			outToClient.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			if (clientSocket != null) {
				clientSocket.close();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	
	// Generates a random file name  
	public String generateRandomFileName() {

		String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
		SecureRandom RANDOM = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 10; ++i) {
			sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}
	
}
