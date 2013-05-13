package dk.itu.activityrecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;




import android.os.AsyncTask;
import android.util.Log;

public class DataUploader {

	private HttpPost httpPost;
	// = new HttpPost("http://pervasivelecture10simmi.appspot.com/ma3tester");
	private HttpClient httpClient; 

	private String dataUploaderTag = "upload";

	// private String nameOfRecord = "";
	// private List<String> listToUpload;
	// private int numberOfValues = 4;

	public DataUploader(String uri) {

		try {
			httpPost = new HttpPost(uri);
		} catch (IllegalArgumentException iae) {
			Log.v(dataUploaderTag, "IAE: " + iae);
		}

	}

	public void uploadJson(String nameOfFile, List<String> data) {
		
		try {
			JSONObject obj = new JSONObject();
			
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>();
			int index = 0;
			for (String s : data) {
				list.add(new LinkedHashMap());
				String[] spli = s.split(",");
				
				list.get(index).put("timestamp", spli[0]);
				list.get(index).put("x",spli[1]);
				list.get(index).put("y", spli[2]);
				list.get(index).put("z", spli[3]);
				list.get(index).put("activity", spli[4]);
				/*
				obj.put("timestamp", spli[0]);
				obj.put("x",spli[1]);
				obj.put("y", spli[2]);
				obj.put("z", spli[3]);
				*/
				index++;
			}
			obj.put("data", list);
			
			//receiveJson(obj.toString());
			Log.v(dataUploaderTag, "Json string: "+obj.toJSONString());
			
			
			
			new Post().execute(nameOfFile,obj.toString());
		} catch (Exception e) {
			Log.v(dataUploaderTag, "Exception: " + e);
		}
		Log.v(dataUploaderTag, "uploadJson method finished.");
	}

	
	public void receiveJson(String json) throws ParseException{
		JSONParser parser=new JSONParser();  
		
		JSONObject obj = (JSONObject) parser.parse(json);
		
		JSONArray msg = (JSONArray) obj.get("measures");
		
		Iterator<String> i = msg.iterator();
		
		while(i.hasNext()){
			Log.v(dataUploaderTag, i.next());
		}
		
	}
	
	public void uploadList(String nameOfRecord, List<String> listToUpload,
			int numberOfValues) {
		// int index = 0;
		// this.nameOfRecord = nameOfRecord;
		// this.listToUpload = listToUpload;
		// this.numberOfValues = numberOfValues;

		String data = "";

		Log.v(dataUploaderTag, "Starting execute..");
		for (String line : listToUpload) {
			data += line + "/n";
			// index++;
		}

		new Post().execute(nameOfRecord, data);

		Log.v(dataUploaderTag, "Execute started..");
	}

	private class Post extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... arg0) {
			String nameOfRecord = arg0[0];
			
			String data = arg0[1];
			
			
			//String server = "10.25.253.124:8888";
			String server = "ma3gae.appspot.com";
			URL url = null;
			 DataInputStream input = null;
			 String str = "";
			try {
				 url = new URL("http://" + server + "/mandatoryassignment3_gae");
				 HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				 // Let the run-time system (RTS) know that we want input.
				 urlConn.setDoInput (true);
				 // Let the RTS know that we want to do output.
				 urlConn.setDoOutput (true);
				 // No caching, we want the real thing.
				 urlConn.setUseCaches (false);
				 urlConn.setRequestMethod("POST");
				 System.out.println("post request method");
				 urlConn.connect();
				 DataOutputStream output = new DataOutputStream(urlConn.getOutputStream());
				 String content = "nameOfRecord=10_simmis-walking_000000&data=" + data;
				 output.writeBytes(content);
				 output.flush();
				 output.close(); 
				 
				 String o = "";
				 input = new DataInputStream (urlConn.getInputStream());
				 
				 while(( o = input.readLine() ) != null)
				 {
					str += o;
					 
				 }
				 
				 System.out.println("response: "+str);
				 input.close ();
			}
			catch(Exception ea)
			{
				Log.v(dataUploaderTag,"Exception: "+ea);
				
			}
			finally{
				try {
					input.close ();
					Log.v(dataUploaderTag,"REsponse: "+str);
				} catch (IOException e) {
					
					Log.v(dataUploaderTag,"Exception2: "+e);
				}
			}
			
			return 0;
			/*
			httpClient = new DefaultHttpClient();
			
			String nameOfRecord = arg0[0];
			
			String data = arg0[1];
			
			List<NameValuePair> nameValuePairs;
			
			nameValuePairs = new ArrayList<NameValuePair>(2);

			nameValuePairs.add(new BasicNameValuePair("nameOfRecord", nameOfRecord));
			nameValuePairs.add(new BasicNameValuePair("data", data));

			// Execute HTTP Post Request
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpClient.execute(httpPost);
				// httpPost.getEntity().consumeContent();
				Log.v(dataUploaderTag," Status line: " + response.getStatusLine());
			} catch (ClientProtocolException e) {
				e.printStackTrace();

			} catch (IOException e) {
				e.printStackTrace();

			}
			return 0;
			/*
			 * int currentIndex = 0; String[] splitLine = arg0[0].split(",");
			 * currentIndex = Integer.valueOf(splitLine[0]);
			 * 
			 * Log.v(dataUploaderTag,"Do background started..nr: "+currentIndex);
			 * 
			 * List<NameValuePair> nameValuePairs;
			 * 
			 * //for(String line : listToUpload){
			 * 
			 * // The following process can be made dynamic by using the length
			 * of splitLine as // the numberOfValues and fetching the header of
			 * the file (line 0 in listToUpload). // Then use the header to
			 * indicate the name of a value in another loop. Later..
			 * nameValuePairs = new ArrayList<NameValuePair>(6);
			 * 
			 * nameValuePairs.add(new
			 * BasicNameValuePair("nameOfRecord",nameOfRecord));
			 * nameValuePairs.add(new
			 * BasicNameValuePair("index",String.valueOf(currentIndex)));
			 * nameValuePairs.add(new
			 * BasicNameValuePair("timeStamp",splitLine[1]));
			 * nameValuePairs.add(new BasicNameValuePair("x",splitLine[2]));
			 * nameValuePairs.add(new BasicNameValuePair("y",splitLine[3]));
			 * nameValuePairs.add(new BasicNameValuePair("z",splitLine[4]));
			 * 
			 * // Execute HTTP Post Request try { httpPost.setEntity(new
			 * UrlEncodedFormEntity(nameValuePairs)); HttpResponse response =
			 * httpClient.execute(httpPost);
			 * //httpPost.getEntity().consumeContent();
			 * Log.v(dataUploaderTag,currentIndex
			 * +" Status line: "+response.getStatusLine()); } catch
			 * (ClientProtocolException e) { e.printStackTrace();
			 * 
			 * } catch (IOException e) { e.printStackTrace();
			 * 
			 * }
			 * 
			 * //}
			 * Log.v(dataUploaderTag,"Do background stopped..nr: "+currentIndex
			 * ); return currentIndex;
			 */

		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(String result) {

		}

	}

	/*
	 * public String uploadList(String nameOfRecord, List<String>
	 * listToUpload,int numberOfValues){
	 * 
	 * int index = 0; List<NameValuePair> nameValuePairs;
	 * 
	 * for(String line : listToUpload){ String[] splitLine = line.split(","); //
	 * The following process can be made dynamic by using the length of
	 * splitLine as // the numberOfValues and fetching the header of the file
	 * (line 0 in listToUpload). // Then use the header to indicate the name of
	 * a value in another loop. Later.. nameValuePairs = new
	 * ArrayList<NameValuePair>(numberOfValues);
	 * 
	 * 
	 * nameValuePairs.add(new BasicNameValuePair("nameOfRecord",nameOfRecord));
	 * nameValuePairs.add(new BasicNameValuePair("timeStamp",splitLine[0]));
	 * nameValuePairs.add(new BasicNameValuePair("x",splitLine[1]));
	 * nameValuePairs.add(new BasicNameValuePair("y",splitLine[2]));
	 * nameValuePairs.add(new BasicNameValuePair("z",splitLine[3]));
	 * 
	 * // Execute HTTP Post Request try { httpPost.setEntity(new
	 * UrlEncodedFormEntity(nameValuePairs)); HttpResponse response =
	 * httpClient.execute(httpPost);
	 * Log.v(dataUploaderTag,"Status line: "+response.getStatusLine()); } catch
	 * (ClientProtocolException e) { e.printStackTrace(); return
	 * "Exception: ClientProtocolException"; } catch (IOException e) {
	 * e.printStackTrace(); return "Exception: IOException"; } index++;
	 * 
	 * }
	 * 
	 * return "Upload complete: "+index+" lines uploaded."; }
	 * 
	 * private class Post extends AsyncTask<String,Void, Void> {
	 * 
	 * @Override protected Void doInBackground(String... arg0) { // TODO
	 * Auto-generated method stub try {
	 * 
	 * // Add your data List<NameValuePair> nameValuePairs = new
	 * ArrayList<NameValuePair>(6); nameValuePairs.add(new
	 * BasicNameValuePair("kodi",arg0[0])); nameValuePairs.add(new
	 * BasicNameValuePair("longitude",arg0[1])); nameValuePairs.add(new
	 * BasicNameValuePair("latitude", arg0[2])); nameValuePairs.add(new
	 * BasicNameValuePair("x",arg0[3])); nameValuePairs.add(new
	 * BasicNameValuePair("y",arg0[4])); nameValuePairs.add(new
	 * BasicNameValuePair("z",arg0[5])); httpPost.setEntity(new
	 * UrlEncodedFormEntity(nameValuePairs));
	 * 
	 * 
	 * // Execute HTTP Post Request HttpResponse response =
	 * httpClient.execute(httpPost);
	 * 
	 * Log.v(dataUploaderTag,"Status line: "+response.getStatusLine());
	 * Log.v(dataUploaderTag,
	 * "Log successful. Check http://pervasivelecture10simmi.appspot.com/activitytracker"
	 * ); //Log.v(locationLogTag,response.toString());
	 * 
	 * } catch (ClientProtocolException e) { // TODO Auto-generated catch block
	 * Log.v(dataUploaderTag, e.toString()); } catch (IOException e) { // TODO
	 * Auto-generated catch block Log.v(dataUploaderTag, e.toString()); }
	 * catch(Exception e){
	 * 
	 * Log.v(dataUploaderTag, e.toString()); } return null; } }
	 */
}
