package hackforsweden.team4.main;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import scripters.api.network.DeviceEvent;
import scripters.api.network.DeviceListener;
import scripters.api.network.ServerInstance;
import scripters.api.network.ServerLog;

public class Stockholm {
	public Stockholm() {
		try {
			/*
			Accept: application/json, text/javascript, * /*; q=0.01
			Accept-Encoding: gzip, deflate
			Accept-Language: sv-SE,sv;q=0.8,en-US;q=0.5,en;q=0.3
			Connection: keep-alive
			Cookie: _ga=GA1.2.1605425376.1488999736; ASP.NET_SessionId=5pnyfbbqbfsc5tvha2hojwbf; BIGipServer~Sthlmbr~STHLMBR_www.visitstockholm.com_HTTP_Pool=1720517898.20480.0000; _gat=1
			Host: www.visitstockholm.com
			Referer: http://www.visitstockholm.com/
			User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0
			X-Requested-With: XMLHttpRequest
			*/
			
			ServerLog.setWriteLogs(false);
			ServerInstance instance = ServerInstance.createInstance(7777, 10);
			
			//instance.addPath("C:/Users/Admin/Desktop/htmlscripttest/testsetset.html", "/");
			
			instance.addDeviceListener(new DeviceListener() {
				public void clientConnection(DeviceEvent event) {
					System.out.println(new String(event.getPacket().getRaw()));
					event.consume();
				}
				public void clientDisconnect(DeviceEvent event) {
				}
				public void clientTimeout(DeviceEvent event) {
				}
			});
			//instance.startServer();
			URL url = new URL("http://www.visitstockholm.com/en/See--do/?format=json");
			
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			connection.setRequestProperty("Accept-Language", "sv-SE,sv;q=0.8,en-US;q=0.5,en;q=0.3");
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Cookie", "_ga=GA1.2.1605425376.1488999736; ASP.NET_SessionId=5pnyfbbqbfsc5tvha2hojwbf; BIGipServer~Sthlmbr~STHLMBR_www.visitstockholm.com_HTTP_Pool=1720517898.20480.0000; _gat=1");
			connection.setRequestProperty("Host", "www.visitstockholm.com");
			connection.setRequestProperty("Referer", "http://www.visitstockholm.com/");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
			connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			connection.setDoOutput(true);
			connection.connect();
			
			String msg = "";
			{ int i = 0; while((i = connection.getInputStream().read()) != -1) msg += (char)i; }
			
			System.out.println(msg);
			
			Object o = get(msg, "Grid", 0, 0);
			
			System.out.println(o + ", " + o.getClass().getName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object get(String json, Object... var) {
		Object o = null;
		try { o = new JSONObject(json); } catch(Exception e) { e.printStackTrace(); return null; }
		try {
			System.out.println(o.getClass().getName());
			for(int i = 0; i < var.length; i++) {
				switch(o.getClass().getName()) {
					case "org.json.JSONObject":
						o = ((JSONObject)o).get((String)var[i]);
						
						break;
					case "org.json.JSONArray":
						o = ((JSONArray)o).get((int)var[i]);
						
						break;
				}
				System.out.println(o.getClass().getName());
			}
		} catch(Exception e) { e.printStackTrace(); }
		return o;
	}
	
	public String uncompress(InputStream stream) throws Exception {
		String ret = "";
		byte[] buffer = new byte[1024];
		
		GZIPInputStream gZIPInputStream = new GZIPInputStream(stream);
		int bytes_read;
		while((bytes_read = gZIPInputStream.read(buffer)) > 0) ret += new String(buffer, 0, bytes_read);
		gZIPInputStream.close();
		
		return ret; //System.out.println("The file was decompressed successfully!");
	}
}
