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
					//double radi = 0;
					// double m = 111319.44;
					
					System.out.println("After : [" + d_lon + ", " + d_lat + "]");//, [" + y_lon + ", " + y_lat + "]");
					
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
					for(int i = 0x160;  i <= 0x256;  i++) msg = msg.replace("" + (char)i, "&#" + i + ";");
					
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
					
					//String data = new String(gzip_msg, StandardCharsets.ISO_8859_1);
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
		
		for(int i = 0x2080; i <= 0x209F; i++) msg = msg.replace("" + (char)i, "" + (char)(i % 256));
		for(int i = 0x160;  i <= 0x256;  i++) msg = msg.replace("" + (char)i, "&#" + i + ";");
		/*msg = msg.replace("Ã¶", "&ouml;")
		.replace("Ã¤", "&auml;")
		.replace("Ã¥", "&aring;")
		.replace("Ã…", "&Aring;")
		.replace("Ã„", "&Auml;")
		.replace("Ã\u0096", "&Ouml;");
		/*.replace("â\u0080\u009c", "")
		.replace("'", "&apos;")
		.replace("â\u0080\u0099", "'")
		.replace("â\u0080\u009d", "'")
		.replace("â\u0080\u0093", "-");*/
		
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