import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 * @author sabar
 *
 */
public class CacheMain {

	private static final int PORT_NUM=8082;
	private static ServerSocket socket;
	public static ConcurrentHashMap<String,String> cache=new ConcurrentHashMap<>(); 
	public static BufferedWriter Logger;
	public static void main(String args[]) throws IOException 
	{
		try{
		File cacheDir=new File("cache/");
		File logDir=new File("log/");
		if (!cacheDir.exists()){cacheDir.mkdir();}
		
		if(!logDir.exists()){
			logDir.mkdir();
		}
		
		Logger=new BufferedWriter(new FileWriter(new File("log/log_"+System.currentTimeMillis())));
	
		socket=new ServerSocket(PORT_NUM);
		
		CacheMain.log("Server socket created");
		
		Socket client=null;
		while(true)
		{
			
			try{client=socket.accept();
			new Thread(new ClientThread(client)).start();			
			}
			catch(Exception e)
			{
				Logger.close();
				continue;
				
			}
		}
	
		}
		catch(Exception e)
		{
			Logger.write("Error"+e.getLocalizedMessage());
		}
		finally{
			try {
				Logger.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static synchronized void log(String str) throws IOException
	{
		Logger.write(str+"\n");
	}
	

}
