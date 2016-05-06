package weili.localization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//import communication.MessagePasser;

public class Client {

	private static String config_file_address;

	static String serverIP;
	static int serverPort;
	static String name;

	public static void main(String[] args) {
		createMap();
	}

	public static void createMap() {
		// --------------------------------
		// initialize - get necessary parameters from input

		name = "sammy";
		serverIP = "52.36.135.251";
		//		serverIP = "127.0.0.1";
		serverPort = 4001;

		// --------------------------------
		// Execute the receiver/sender scripts

		//      center: {lat: 40.442243, lng: -79.94701758}


		for (int t = 0; t<100; t++){
			double start_lat = 	40.442243000;
			double start_long =-79.947010580;
			double del_lat = 	  .000007;
			double[] del_lats = {.000007, .000017, -.000007, -.000027,.000007,-.000007,.000007,-.000007,.000007,-.000007,.000007,-.000007};
			double del_long = 	  .00002;


			//String url = "http://52.36.135.251/insert.php?name=sammy&latt=40.442243000&long=-79.947010580&vecx=.000007&vecy=.000017

			String url = constructURL(serverIP,name,start_lat,start_long,del_lat,del_long);

			for (int k=0; k<del_lats.length;k++){
				start_lat+=(del_lats[k]);
				start_long+=(del_long);
				System.out.println(url);
				url = constructURL(serverIP,name,start_lat,start_long,del_lats[k],del_long);
				try {
					System.out.println(getHTML(url));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		System.out.println("done");
		System.exit(0);

	}

	public static String constructURL(String IP,String name,double latt, double longi, double vecx, double vecy){
		String url = "http://";
		url=url+IP;
		url=url+"/insert.php?";
		url=url+"name="+name;
		url=url+"&latt="+latt;
		url=url+"&long="+longi;
		url=url+"&vecx="+vecx;
		url=url+"&vecy="+vecy;
		return url;
	}

	public static String getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();
		InputStreamReader ir = new InputStreamReader(is);
		BufferedReader rd = new BufferedReader(ir);
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result.toString();
	}

}
