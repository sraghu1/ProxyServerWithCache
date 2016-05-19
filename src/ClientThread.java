import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author sabar
 *
 */
public class ClientThread implements Runnable{

	private Socket clientSock;
	private String headers="";
	private String method;
	private String version;
	private String URI;
	private DataInputStream responseDis;
	private String host;
    private int port;
    final static int HTTP_PORT = 80;
	public static final String userAgent="Mozilla/5.0";
	final static String CRLF = "\r\n";
	
    /**
     * 
     * @param s
     * @throws IOException 
     */
	public ClientThread(Socket s) throws IOException
	{
		this.clientSock=s;
		CacheMain.log("Client socket created");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
		clientSock.setKeepAlive(true);	
		BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
		String line=fromClient.readLine();
		if(line!=null){
		String[] parts=line.split(" ");
		String method=parts[0];
		if(method.equalsIgnoreCase("get"))
		{
			URI=parts[1];
		
			version=parts[2];
			if(URI.startsWith("/"))
			{
				URI=URI.replaceFirst("/", "");
			}
			URI="http://"+URI;
			
			
			
			if(CacheMain.cache.containsKey(URI))
			{
				System.out.println("Cache Hit");
				CacheMain.log("URI:"+URI+" found in cache");
				BufferedReader reader=new BufferedReader(new FileReader(CacheMain.cache.get(URI)));
				
				DataOutputStream dout=new DataOutputStream(clientSock.getOutputStream());
				String mLine="";
				while((mLine=reader.readLine())!=null)
				{
					dout.writeBytes(mLine);
					dout.writeBytes("\n");
				}
				
				dout.writeByte(-1);
				dout.flush();
				dout.close();
				reader.close();
				CacheMain.log("Response sent to client");
			}
			else{
				
			sendGet(URI);
			
			}
			while(((line=fromClient.readLine())!=null)&&(!clientSock.isClosed()))
			{
				headers += line + CRLF;
				if (line.startsWith("Host:")) {
				    parts = line.split(" ");
				    if (parts[1].indexOf(':') > 0) {
					String[] tmp2 = parts[1].split(":");
					host = tmp2[0];
					port = Integer.parseInt(tmp2[1]);
				    } else {
					host = parts[1];
					port = HTTP_PORT;
				    }
				}
			}
			
		clientSock.close();
		}

			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * sends the request to the URI through the proxy server and get the response and put it in the cache
	 * @param uri
	 * @throws IOException
	 */
	public synchronized void sendGet(String uri) throws IOException{
		URL url=new URL(uri);
		//Opens a connection for the URI
		HttpURLConnection con=(HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", userAgent);
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		CacheMain.log("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code from "+uri+":"+ responseCode);
		CacheMain.log("Response Code from "+uri+": " + responseCode);
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		
		//create datainput stream for response
		responseDis=new DataInputStream(con.getInputStream());
		Map<String, List<String>> headerMap=con.getHeaderFields();
		String request="";
		String firstLine="";
		for(String key:headerMap.keySet())
		{
			if(key==null)
			{
				List<String> list=headerMap.get(key);
				for(String k:list)
				{
					firstLine+=k+" ";
				}
				firstLine.trim();
			}
			else
			{
				
				List<String> list=headerMap.get(key);
				request+=key+": ";
				for(String k:list)
				{
					request+=k+" ";
				}
				request.trim();
				request+=CRLF;
			}
		}
		request=firstLine+request;
		request+=CRLF;
		DataOutputStream dout=new DataOutputStream(clientSock.getOutputStream());
		String inputLine;
		String cacheFileName="cache/cached_"+System.currentTimeMillis();
		BufferedWriter bw=new BufferedWriter(new FileWriter(cacheFileName));
		StringBuffer response = new StringBuffer();
		
		dout.writeBytes(request);
		bw.write(request);
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			bw.write(inputLine);
			bw.write("\n");
			dout.writeBytes(inputLine);
			dout.writeBytes("\n");
		}
		in.close();
		bw.close();
		dout.writeByte(-1);
		dout.flush();
		dout.close();
		System.out.println("Added to cacche");
		CacheMain.log("Response cached");
		CacheMain.cache.put(uri,cacheFileName);
	}

}
