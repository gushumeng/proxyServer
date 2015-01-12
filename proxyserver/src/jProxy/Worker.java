package jProxy;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.*;



public class Worker extends Thread {
	Cache cache = Cache.getInstance();
	
	@Override
	public void run(){
		while(true){
			String urlString = null;
			try {
				urlString = Jproxy.queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			String getResponse = "";
	        if(Math.abs(urlString.hashCode() % 2)==0){
	        	synchronized(cache){
	        		
		        	if(cache.isContains(urlString)){
		        		if(cache.isTimeOut(urlString)){
		        			cache.remove(urlString);
		        		}else{
		        			getResponse = cache.get(urlString);
		        		}
		        	}
	        	}
	        	
	        	if(getResponse.equals("")){
	        		try {
						getResponse = getHTTPResponseByURL(urlString);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		
	        	}
	        	
	        	synchronized(cache){
	        		if(cache.isContains(urlString)){
	        			cache.remove(urlString);
	        			cache.set(urlString, getResponse);
	        		}else{
	        			cache.set(urlString, getResponse);
	        		}
	        		
	        	}
	      
	        }		
			
		}
	}
	
	public static String getHTTPResponseByURL(String urlString) throws MalformedURLException{
		String HTTPResponse = "";
		URL url = new URL(urlString);
		String host = url.getHost();
		String path = url.getPath();
		StringBuffer httpContent=  new StringBuffer();
		httpContent.append("GET ");
		if(path.equals("")){
			httpContent.append("/");
		}else{
			httpContent.append(path);
		}

		httpContent.append(" HTTP/1.0\r\nHOST: ");
		httpContent.append(host);
		httpContent.append("\r\n\r\n");
		HTTPResponse = getHTTPResponse(host, 80, httpContent.toString());
		
		System.out.println(urlString);
		return HTTPResponse;
	}
	
	
	public static String getHTTPResponse(String host, int port, String httpContent){
	
		StringBuffer httpResult = new StringBuffer();
		try(
				Socket proxyToRemote = new Socket(host, port);
				DataOutputStream out = new DataOutputStream(proxyToRemote.getOutputStream());
			    BufferedReader in = new BufferedReader(new InputStreamReader(proxyToRemote.getInputStream()));
				){
			out.write(httpContent.getBytes(), 0, httpContent.length());
			
			String inputLine;
			int length = 0;
			while((inputLine= in.readLine())!=null){
				if(inputLine.equals("\r\n")){
					httpResult.append("\r\n");
					break;
				}
				if(inputLine.startsWith("Content-Length: ")){
					int index = inputLine.indexOf(':') + 1;
					String len = inputLine.substring(index).trim();
					length = Integer.parseInt(len);
				}
				httpResult.append(inputLine + "\r\n"); 
			}
			if (length > 0) {
                int read;
                StringBuffer message = new StringBuffer();
                while ((read = in.read()) != -1) {
                    message.append((char) read);
                    if (message.length() == length)
                        break;
                }
                httpResult.append(message + "\r\n");
            }
			

			}catch(IOException e){
				e.printStackTrace();
			}
		return httpResult.toString();
		}
}
