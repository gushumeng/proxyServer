package jProxy;
import java.io.*;
import java.net.*;
import java.lang.reflect.Array;



import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


class ProxyThread extends Thread
{
	Socket socketAtServer = null;
	Jproxy proxyServer = null;
	Cache cache = Cache.getInstance();
	
	public ProxyThread(Socket socketAtServer, Jproxy proxyServer){
		super("ProxyThread");
		this.socketAtServer = socketAtServer;
		this.proxyServer = proxyServer;
	}
	public void run(){
		

		try (
				PrintWriter out = new PrintWriter(socketAtServer.getOutputStream(),true);
			    BufferedReader in = new BufferedReader(new InputStreamReader(socketAtServer.getInputStream()));
		    ){
			
			String inputLine;
			StringBuffer outputLine = new StringBuffer();
			StringBuffer url = new StringBuffer();
			String path = "";
			String host = "";
			
			
			
			while ((inputLine = in.readLine()) != null) {
				
                if (inputLine.equals("")) { // last line of request message
                                        // header is a
                                        // blank line (\r\n\r\n)
                	
                	outputLine.append("\r\n");
                	
                    break; // quit while loop when last line of header is
                            // reached
                    
                }
                else if(inputLine.startsWith("GET ")){
                	String[] tokens = inputLine.split(" ");
                    path = tokens[1];
                    
                }
                else if(inputLine.startsWith("HOST: ")){
                	int index = inputLine.indexOf(':') +1;
                	host = inputLine.substring(index).trim();
                	url.append(host);
                	url.append(path);                	
                }         
                System.out.println(inputLine);
                outputLine.append(inputLine + "\r\n"); // append the request                     
            }
		
			//this part is to check whether the url exists in the caches 
	        String urlString = url.toString();
	       
	        String getResponse = "";
	        if(Math.abs(urlString.hashCode())%2 == 0){
	        	synchronized(cache){
	        		
		        	if(cache.isContains(urlString)){
		        		if(cache.isTimeOut(urlString)){
		        			cache.remove(urlString);
		        		}else{
		        			System.out.println("the result is from cached!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		        			getResponse = cache.get(urlString);
		        		}
		        	}
	        	}
	        	
	        	if(getResponse.equals("")){
	        		getResponse = getHTTPResponse(host, 80, outputLine.toString());
	        		
	        	}
	        	
	        	synchronized(cache){
	        		if(cache.isContains(urlString)){
	        			cache.remove(urlString);
	        			cache.set(urlString, getResponse);
	        		}else{
	        			cache.set(urlString, getResponse);
	        			int length = getResponse.indexOf("\r\n\r\n");
		        		System.out.println("the length is" +length);
		        		String html = getResponse.substring(length+4);
		        		Document doc = Jsoup.parse(html);
		        		Elements links = doc.select("a[href]");
		        		System.out.println("now it is the link");
		        		
		        		for (Element link : links) {
		        			Jproxy.queue.put(link.attr("abs:href"));	                   
		                }       		
	        		}
	        		
	        	}
	      
	        }else{
	        	getResponse = getHTTPResponse("ChenlinDu-THINK", 4444, outputLine.toString());
	        }
	       
	        System.out.println(getResponse);
	        
	       out.println(getResponse);
		}catch(IOException e){
            	e.printStackTrace();// end of while to read headers
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
        	
        }
      
     
	public static String getHTTPResponseByURL(String urlString){
		String HTTPResponse = "";
		try {
			URL url = new URL(urlString);
			String host = url.getHost();
			String path = url.getPath();
			StringBuffer httpContent=  new StringBuffer();
			httpContent.append("GET ");
			httpContent.append(path);
			httpContent.append(" HTTP/1.0\r\nHOST: ");
			httpContent.append(host);
			httpContent.append("\r\n\r\n");
			HTTPResponse = getHTTPResponse(host, 80, httpContent.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	
