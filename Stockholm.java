package hackforsweden.team4.main;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import scripters.api.network.DeviceEvent;
import scripters.api.network.DeviceListener;
import scripters.api.network.ServerInstance;
import scripters.api.network.ServerLog;

public class Stockholm {
	public static String COOKIE = "";
	public static Random random = null;
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
					//System.out.println(new String(event.getPacket().getRaw()));
					//System.out.println("-----------------------------------------------------------------");
					//System.out.println(event.getPacket().resource);
					double d_lat, d_lon, y_lat, y_lon;
					{	String[] arr = r.substring(2).split("&");
						
						d_lat = 0; try { d_lat = Double.valueOf(oNum(arr[0].substring(0))); } catch(Exception e) {}
						d_lon = 0; try { d_lon = Double.valueOf(oNum(arr[1].substring(0))); } catch(Exception e) {}
						y_lat = 0; try { y_lat = Double.valueOf(oNum(arr[2].substring(0))); } catch(Exception e) {}
						y_lon = 0; try { y_lon = Double.valueOf(oNum(arr[3].substring(0))); } catch(Exception e) {}
						System.out.println("Before: [" + d_lon + ", " + d_lat + "], [" + y_lon + ", " + y_lat + "]");
						
						{	d_lat += 90;
						/**/d_lat = (d_lat < 0 ? (180 - (d_lat % 180)):d_lat) % 180;
						/**/d_lat -= 90; }
						d_lon = (d_lon < 0 ? (360 - (d_lon % 360)):d_lon) % 360;
						
						{	y_lat += 90;
						/**/y_lat = (y_lat < 0 ? (180 - (y_lat % 180)):y_lat) % 180;
						/**/y_lat -= 90; }
						y_lon = (y_lon < 0 ? (360 - (y_lon % 360)):y_lon) % 360;
					}
					double radi = 0;
					// lat 0, 360
					// lon 0, 360
					double m = 111319.44;
					
					
					double num = 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((y_lat - Math.abs(d_lat)) * Math.PI / 180 / 2), 2) + Math.cos(y_lat * Math.PI / 180) * Math.cos(Math.abs(d_lat) * Math.PI / 180) * Math.pow(Math.sin((y_lon - d_lon) * Math.PI / 180 / 2), 2)));
					
					System.out.println("After : [" + d_lon + ", " + d_lat + "], [" + y_lon + ", " + y_lat + "], " + num);
					// Long, Lat
					
					String msg = "";
					try {
						String send = (String)POST(false, false, a("0", "1", "2", "3", "4", "5", "6", "7"), "en", 0, a()).toString();
						JSONArray array = (JSONArray)get(send, "GetNonDuplicateEntityListResult");
						for(int i = 0; i < array.length(); i++) {
							if(i >= 10) break;
							JSONObject arr = array.getJSONObject(i);
							Object Long = arr.get("Longitude");
							Object Lati = arr.get("Latitude");
							if(Long == null || Long.equals("") || Lati == null || Lati.equals("")) continue;
							Object Desc = arr.get("Description");
							Object Imag = arr.get("RectangleImageUrl");
							if(Imag == null) Imag = arr.get("SquareImageUrl");
							Object Name = arr.get("Name");
							
							msg += ",{\"Name\":\""+Name+"\",\"Description\":\""+Desc+"\",\"Longitude\":\""+Long+"\",\"Latitude\":\""+Lati+"\",\"Img\":\""+Imag+"\"}";
						}
						JSONObject object = new JSONObject("{\"List\":["+msg.substring(1)+"]}");
						System.out.println(object);
						msg = object.toString();
					} catch(Exception e) {
						e.printStackTrace();
					}
//					try {
//						int rad = random.nextInt();
//						rad = (rad < 0 ? -rad:rad) % 10;
//						msg = (String)POST(false, false, a("" + rad), "en", 0, a()).toString();
//					} catch(Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					event.getDevice().WriteToClient(("HTTP/2.0 200 OK\r\nAccess-Control-Allow-Origin: *\r\nContent-Type: application/json\r\n\r\n" + msg + "\r\n").getBytes());//"\r\n".getBytes());
					event.consume();
				}
				public void clientDisconnect(DeviceEvent event) {
				}
				public void clientTimeout(DeviceEvent event) {
					//System.out.println(new String(event.getPacket().getRaw()));
					event.consume();
				}
			});
			instance.startServer();
			
			initCookie();
			
			//String msg = (String)POST(false, false, a("2"), "en", 0, a()).toString();
			//System.out.println(msg);
			
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
			
			//String msg = (String)POST(false, false, a("0", "9"), "en", 0, a()).toString();
			//System.out.println(msg);
			
			/*Object[] oa = new Object[9];
			int times = 2;
			for(int t = 0; t < times; t++) for(int i = 0; i < oa.length; i++) {
				if(oa[i] == null) oa[i] = new HashMap<String, Integer>();
				@SuppressWarnings("unchecked")
				Map<String, Integer> map = (Map<String, Integer>)oa[i];
				
				String msg = (String)POST(false, false, a("" + i), "en", 0, a()).toString();
				JSONArray o = (JSONArray)get(msg, "GetNonDuplicateEntityListResult");
				
				for(int k = 0; k < o.length(); k++) {
					JSONArray j = (JSONArray)get(o.get(k), "TagNames");
					for(int v = 0; v < j.length(); v++) {
						Object st = j.get(v);
						if(st == null) continue;
						try {
							map.put(st.toString(), map.get(st.toString()) + 1);
						} catch(Exception e) { map.put(st.toString(), 1); }
					}
				}
				double d = (100. / (times * oa.length));
				d = d * (i + t * oa.length);
				System.out.println("Literate! {" + ((int)(d * 100) / 100.) + "}");
			}
			
			
			
			for(int i = 0; i < oa.length; i++) {
				int c = 0;
				@SuppressWarnings("unchecked")
				Map<String, Integer> map = (Map<String, Integer>)oa[i];
				for(String s : map.keySet()) c += map.get(s);
				System.out.println(c);
				
				double d = 100. / c;
				
				for(String s : map.keySet()) {
					System.out.println(s + ": " + ((int)(d * map.get(s) * 100) / 100.));
				}
				System.out.println("\n");
			}*/
			
			
			//System.out.println(o);
			
			/*msg = (String)POST(false, false, a("0"), "en", 0, a()).toString();
			
			o = (JSONArray)get(msg, "GetNonDuplicateEntityListResult");
			for(int i = 0; i < o.length(); i++) {
				id.add(get(o.get(i), "Id"));
				pi.add(get(o.get(i), "RectangleImageUrl"));
				po.add(new Positi(o.get(i)));
			}
			
			System.out.println("");*/
		} catch(Exception e) {
			e.printStackTrace();
		}
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
		connection.getOutputStream().write((s + "\r\n").getBytes());
		connection.connect();
		
		String msg = "";
		{ int i = 0; while((i = connection.getInputStream().read()) != -1) msg += (char)i; }
		return new JSONObject(msg);
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