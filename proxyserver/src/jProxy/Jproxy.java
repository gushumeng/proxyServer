package jProxy;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.io.*;
import java.net.*;
import java.lang.reflect.Array;

public class Jproxy{

	
	public static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1024);
	
	
	public void startOperate(int portNumber){
		ServerSocket server = null;
		try{
			server = new ServerSocket(portNumber);
			while(true){
				Socket socketAtServer = server.accept();
				ProxyThread socketThread = new ProxyThread(socketAtServer, this);
				socketThread.start();
				
			}
			
		}catch(IOException e){
			System.out.println("Exception caught when trying to listen on port "
	                + portNumber + " or listening for a connection");
	        System.out.println(e.getMessage());
		}
		
		
	}
	
	public static void main(String[] args) throws IOException{
		
		int portNumber = 4443;
		
		for(int i = 0; i< 5; i++){
			Worker worker = new Worker();
			worker.start();
		}
		
		Jproxy proxyServer = new Jproxy();
		proxyServer.startOperate(portNumber);
	}
	
	
}