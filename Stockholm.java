package hackforsweden.team4.main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;

import scripters.api.network.DeviceEvent;
import scripters.api.network.DeviceListener;
import scripters.api.network.ServerInstance;
import scripters.api.network.ServerLog;

public class Stockholm {
	public static String COOKIE = "";
	public static Random random = null;
	public static String Langua = "en";
	public Stockholm() {
		try {
			random = new Random();
			ServerLog.setWriteLogs(false);
			ServerInstance instance = ServerInstance.createInstance(7777, 10);
			
			//instance.addPath("C:/Users/Admin/Desktop/htmlscripttest/testsetset.html", "/");
			instance.setConnectionTimeout(1000);
			instance.addDeviceListener(new DeviceListener() {
				public void clientConnection(DeviceEvent event) {
					String r = event.getPacket().resource;
					if(r.length() < 2) { event.consume(); return; }
					System.out.println("\nRequest: " + r);
					//System.out.println(new String(event.getPacket().getRaw()));
					//System.out.println("-----------------------------------------------------------------");
					//System.out.println(event.getPacket().resource);
					double d_lat, d_lon;//, y_lat, y_lon;
					boolean en;
					{	String[] arr = r.substring(2).split("&");
						
						d_lat = 0; try { d_lat = Double.valueOf(oNum(arr[0].substring(0))); } catch(Exception e) {}
						d_lon = 0; try { d_lon = Double.valueOf(oNum(arr[1].substring(0))); } catch(Exception e) {}
						//y_lat = 0; try { y_lat = Double.valueOf(oNum(arr[2].substring(0))); } catch(Exception e) {}
						//y_lon = 0; try { y_lon = Double.valueOf(oNum(arr[3].substring(0))); } catch(Exception e) {}
						en = false; try { en = Boolean.valueOf(arr[3].substring(8)); } catch(Exception e) {}
						System.out.println("Before: [" + d_lon + ", " + d_lat + "]");//, [" + y_lon + ", " + y_lat + "]");
						
						if(en) Langua = "en"; else Langua = "sv";
						
						{	d_lat += 90;
						/**/d_lat = (d_lat < 0 ? (180 - (d_lat % 180)):d_lat) % 180;
						/**/d_lat -= 90; }
						d_lon = (d_lon < 0 ? (360 - (d_lon % 360)):d_lon) % 360;
						
						/*{	y_lat += 90;
						/**y_lat = (y_lat < 0 ? (180 - (y_lat % 180)):y_lat) % 180;
						/**y_lat -= 90; }
						y_lon = (y_lon < 0 ? (360 - (y_lon % 360)):y_lon) % 360;*/
					}
					//double radi = 1000;
					double m = 111319.44;
					//double km= 111.31944;
					//double mi= 11.131944; 0.1
					System.out.println("After : [" + d_lon + ", " + d_lat + "]");//, [" + y_lon + ", " + y_lat + "]");
					
					double la_m = m * d_lat;
					double lo_m = m * d_lon;
					
					System.out.println(la_m + "," + lo_m);
					String msg = "";
					try {
						List<String> a = getRequest(false,false,a("0","1","2","3","4","5","6","7"),0,a(),10);
						
						String s = "{\"List\":[";
						for(int i = 0; i < a.size(); i++) s += a.get(i) + (i + 1 < a.size() ? ",":"");
						s += "]}";
						JSONObject object = new JSONObject(s);
						msg = object.toString();
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					try {
						msg = new String(msg.getBytes(StandardCharsets.ISO_8859_1));
					} catch(Exception e1) {
						e1.printStackTrace();
					}
					
					for(int i = 0x2080; i <= 0x209F; i++) msg = msg.replace("" + (char)i, "" + (char)(i % 256));
					for(int i = 0xA0;   i <= 0xFF;   i++) msg = msg.replace("" + (char)i, "&#" + i + ";");
					
					byte[] gzip_msg = null; {
						byte[] gzip_data = msg.getBytes(StandardCharsets.ISO_8859_1);
						ByteArrayOutputStream b_stream = new ByteArrayOutputStream();
						try(GZIPOutputStream gzip_out = new GZIPOutputStream(b_stream)) {
							gzip_out.write(gzip_data);
						} catch(Exception e) {
							e.printStackTrace();
						}
						gzip_msg = b_stream.toByteArray();
					}
					
					event.getDevice().WriteToClient(("HTTP/2.0 200 OK\r\nAccess-Control-Allow-Origin: *\r\nContent-Length:"+gzip_msg.length+"\r\nContent-Encoding: gzip\r\nContent-Type: application/json\r\n\r\n" + new String(gzip_msg, StandardCharsets.ISO_8859_1) + "\r\n").getBytes(StandardCharsets.ISO_8859_1));;
					event.consume();
				}
				public void clientDisconnect(DeviceEvent event) {}
				public void clientTimeout(DeviceEvent event) {
					//System.out.println(new String(event.getPacket().getRaw()));
					event.consume();
				}
			});
			instance.startServer();
			
			initCookie();
			
			/* 0 = "Art & architecture"
			 * 1 = "Bars", "Clubs"
			 * 2 = "Classic", "Trendy", "Cozy"
			 * 3 = "Budget", "Mid-price", "Gourmet"
			 * 4 = "Nearby towns", "Culture & history"
			 * 5 = "Fashion", "Vintage", "Design", "Food"
			 * 6 = "Outdoors", "Adventures"
			 * 7 = "Stockholm card", "Art & architecture", "Parks & viewpoints", "Family friendly", "Culture & history", ""
			 * 8 = "Art & architecture", "Culture & history", "Mid-price", "Design"
			 * 9 = "Family friendly"
			 */
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object getImage(Object o, Object b) throws Exception {
		String path = o.toString();
		
		URL url = new URL(path + (b.equals("A") ? "rectangle_small_retina.jpg":"square_small_retina.jpg"));
		
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Cookie", "_ga=GA1.2.1605425376.1488999736; _gat=1");
		connection.setRequestProperty("Host", "images.visitstockholm.com");
		connection.setRequestProperty("Referer", "http://www.visitstockholm.com/en/see--do/");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
		connection.setDoOutput(true);
		connection.connect();
		
		BufferedImage bi = ImageIO.read(url);
		if(bi.getWidth() > bi.getHeight()) bi = bi.getSubimage(bi.getWidth() / 4, 0, bi.getWidth() / 2, bi.getHeight());
		
		BufferedImage gi = new BufferedImage(160, 160, BufferedImage.TYPE_INT_RGB);
		gi.createGraphics().drawImage(bi, 0, 0, 160, 160, null);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(gi, "jpg", stream);
		byte[] bytes = Base64.getEncoder().encode(stream.toByteArray());
		
		//bytes = "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".getBytes();
		return new String(bytes);
	}
	
	public void initCookie() throws Exception {
		URL url = new URL("http://www.visitstockholm.com/en/See--do/?format=json");
		
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "sv-SE,sv;q=0.8,en-US;q=0.5,en;q=0.3");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Host", "www.visitstockholm.com");
		connection.setRequestProperty("Referer", "http://www.visitstockholm.com/");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		connection.setDoOutput(true);
		
		List<String> s = connection.getHeaderFields().get("Set-Cookie");
		if(s.size() > 0) COOKIE = s.get(0);
	}
	
	public String oNum(String s) {
		String ret = "";
		for(char c : s.toCharArray()) if(c >= 48 && c <= 57 || c == 46) ret += c;
		return ret;
	}
	public void printHeader(Map<String, List<String>> header) {
		for(String s : header.keySet()) {
			if(s != null) System.out.print("  " + s + ": "); else System.out.print("  ");
			for(String s2 : header.get(s)) System.out.print(s2 + ", "); System.out.println();
		}
	}
	
	public List<String> a(String... a) { List<String> l = new ArrayList<>(); for(String s : a) l.add(s); return l; }
	
	public Object POST(boolean seeAndDoo, boolean EatAndDrink, List<String> TYPES, String Language, Integer Offset, List<String> ID) throws Exception {
		/**
		 * {"request":{"SeeAndDo":false,"EatAndDrink":false,"Types":["7","5","6","4","0"],"Language":"en","Offset":0},"currentEntitiIds":[],"offset":0}
		 * {"request":{"SeeAndDo":false,"EatAndDrink":false,"Types":["7","5","6","4","0"],"Language":"en","Offset":0},"currentEntitiIds":[],"offset":0}
		 */
		String t_a = "["; for(int i = 0; i < TYPES.size(); i++) t_a += "\"" + TYPES.get(i) + "\"" + (i < TYPES.size() - 1 ? ",":""); t_a += "]";
		String i_a = "["; for(int i = 0; i < ID.size()   ; i++) i_a += "\"" + ID.get(i)    + "\"" + (i < ID.size()    - 1 ? ",":""); i_a += "]";
		
		String s =
		"{\"request\":{\"SeeAndDo\":" + seeAndDoo + ",\"EatAndDrink\":" + EatAndDrink +
		",\"Types\":" + t_a + ",\"Language\":\"" + Language + "\",\"Offset\":"
		+ Offset + "},\"currentEntitiIds\":" + i_a + ",\"offset\":" + Offset + "}";
		
		URL url = new URL("http://www.visitstockholm.com/Services/Visit/StyxWebService.svc/GetNonDuplicateEntityList");
		
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "text/plain, */*; q=0.01");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "sv-SE,sv;q=0.8,en-US;q=0.5,en;q=0.3");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Content-Length", "" + s.length());
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Cookie", COOKIE);
		connection.setRequestProperty("Host", "www.visitstockholm.com");
		connection.setRequestProperty("Referer", "http://www.visitstockholm.com/en/See--do/");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		connection.setDoOutput(true);
		connection.getOutputStream().write((s + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
		connection.connect();
		
		String msg = "";
		InputStream stream = connection.getInputStream();
		{
			int i = 0;
			while((i = stream.read()) != -1) msg += (char)i;
		}
		
		msg = new String(msg.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		
		return new JSONObject(msg);
	}
	
	public List<String> getRequest(boolean s, boolean e, List<String> t, Integer o, List<String> id, int size) throws Exception {
		Map<String,String> entry = new HashMap<String,String>();
		while(entry.size() < size) {
			String str = (String)POST(s,e,t,Langua,o,id).toString();
			JSONArray test = (JSONArray)get(str, "GetNonDuplicateEntityListResult");
			for(int i = 0; i < test.length(); i++) {
				if(entry.size() >= size) break;
				JSONObject obj = test.getJSONObject(i);
				Object st = obj.get("Latitude");
				if(!(st instanceof String) || st == null || st.equals("")) continue;
				String Lati = st.toString();
				String Long = obj.getString("Longitude");
				String Desc = obj.getString("Description");
				Object Imag = obj.get("RectangleImageUrl");
				Object Imag_Type = "A"; if(Imag == null) { Imag = obj.get("SquareImageUrl"); Imag_Type = "B"; }
				String Name = obj.getString("Name");
				
				Imag = getImage(Imag, Imag_Type);
				
				System.out.println("[" + Lati + "," + Long + "]\t" + Name);
				
				String ent = "{\"Name\":\""+Name.replace("\"","&quot;")+"\",\"Description\":\""+Desc.replace("\"","&quot;")+"\",\"Longitude\":\""+Long+"\",\"Latitude\":\""+Lati+"\",\"Img\":\""+Imag+"\"}";
				entry.put(Name, ent);
			}
		}
		List<String> ret = new ArrayList<>();
		for(String st : entry.keySet()) ret.add(entry.get(st));
		return ret;
	}
	
	class Positi {
		public double lat, lon;
		public boolean valid = true;
		public Positi(Object o) {
			String s = get(o, "Latitude").toString();
			if(!s.equals("null")) lat = Double.valueOf("0" + s); else lat = -1;
			
			s = get(o, "Longitude").toString();
			if(!s.equals("null")) lon = Double.valueOf("0" + s); else lon = -1;
			
			if(lat <= 0.0 || lon <= 0.0) valid = false;
		}
		public String toString() { return valid ? "[" + lat + "," + lon + "]":"[]"; }
	}
	
	public static Object get(Object json, Object... var) {
		Object o = null;
		try { o = new JSONObject(json.toString()); } catch(Exception e) { e.printStackTrace(); return null; }
		try {
			for(int i = 0; i < var.length; i++) {
				switch(o.getClass().getName()) {
					case "org.json.JSONObject":
						o = ((JSONObject)o).get((String)var[i]);
						
						break;
					case "org.json.JSONArray":
						o = ((JSONArray)o).get((int)var[i]);
						
						break;
				}
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