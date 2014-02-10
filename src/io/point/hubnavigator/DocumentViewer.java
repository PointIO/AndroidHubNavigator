package io.point.hubnavigator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.point.hubnavigator.core.Hub;
import io.point.hubnavigator.core.HubDirEntry;
import io.point.hubnavigator.core.HubShare;
import io.point.hubnavigator.core.TaskCallback;
import io.point.hubnavigator.loaders.ImageLoader;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DocumentViewer extends Activity  implements TaskCallback {

	private JSONObject DocPreviewRc;
	private String hubId;
	private String roomShareId;
	private String fileId;
	private String filename;
	private String containerId;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_docviewer);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    this.hubId = extras.getString("HUB_ID");
		    this.roomShareId = extras.getString("ROOMSHAREID");
		    this.fileId = extras.getString("FILEID");
		    this.filename = extras.getString("FILENAME");
		    this.containerId = extras.getString("CONTAINERID");
		    this.path = extras.getString("PATH");
		    Log.d("POINTIO","Got the roomShareId passed from HubList: " + this.roomShareId);
		}

		new PointIOPreviewFile(this){

			@Override public void onPreExecute() {
				super.onPreExecute();
				Log.d("POINTIO", "starting async api PREVIEW FILE request", null);
	        }			
			@Override public void onPostExecute(String result)
		    {
				Log.d("POINTIO", "finished async api PREVIEW FILE request", null);
				try {
					JSONObject rc = new JSONObject(result);
					DocPreviewRc = rc; // pass back the result info 
					super.onPostExecute(); // which fires callback method
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("POINTIO", "JSONException: " + e.getMessage(), null);					
				}

		    }
		}.execute(this.hubId, this.roomShareId, this.fileId, this.filename, this.containerId, this.path);
	
	}
	
	@Override
	public void done() {
		try {

			if (this.DocPreviewRc.getInt("ERROR") == 0) {
				// populate list
				
				Toast.makeText(DocumentViewer.this,"Got preview:  " + this.DocPreviewRc.getString("RESULT"), Toast.LENGTH_LONG).show();

				int placeholder = R.drawable.ic_launcher;
		         
		        // Imageview to show
		        ImageView image = (ImageView) findViewById(R.id.imageView1);
		         
		        // Image url
		        String image_url = "http://api.androidhive.info/images/sample.jpg";
		         
		        // ImageLoader class instance
		        ImageLoader imgLoader = new ImageLoader(getApplicationContext());
		         
		        // whenever you want to load an image from url
		        // call DisplayImage function
		        // url - image url to load
		        // loader - loader image, will be displayed before getting image
		        // image - ImageView 
		        imgLoader.DisplayImage(this.DocPreviewRc.getString("RESULT"), placeholder, image);

			}
			else
			{
				Toast.makeText(DocumentViewer.this,"An error occurred: " + this.DocPreviewRc.getString("MESSAGE"), Toast.LENGTH_LONG).show();
			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private int indexOfLabel(String label, JSONArray array) throws JSONException {
		for (int i=0; i< array.length(); i++) {
			if (array.getString(i).equalsIgnoreCase(label))
				return i;
		}
		return -1;
	}

	/*
	 * asynctask class takes 3 args: input type, progress type, return type
	 * custom interface 
	 * reference: 
	 * http://stackoverflow.com/questions/8623823/finish-the-calling-activity-when-asynctask-completes
	 */
	private class PointIOPreviewFile extends AsyncTask <String, Void, String> {

		private TaskCallback mCallback;
		private String sessionKey;

		public PointIOPreviewFile(TaskCallback callback)
		{
			mCallback = callback;
		}
		
		protected void onPreExecute() {
		    this.sessionKey = ((HubNavigator) getApplication()).getSession();
		}

		protected void onPostExecute() {
		    mCallback.done();
		}
		
		protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException 
		{
			InputStream in = entity.getContent();
			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n>0) {
				byte[] b = new byte[4096];
				n =  in.read(b);
				if (n>0) 
					out.append(new String(b, 0, n));
			}
			return out.toString();
		}

		@Override
		protected String doInBackground(String... params) {

			String text = "";
	        try {

		        HttpClient httpClient = new DefaultHttpClient();

				List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
				nvPairs.add(new BasicNameValuePair("hubId", params[0]));
				nvPairs.add(new BasicNameValuePair("roomShareId", params[1]));
				nvPairs.add(new BasicNameValuePair("fileId", params[2]));
				nvPairs.add(new BasicNameValuePair("filename", params[3]));
				nvPairs.add(new BasicNameValuePair("containerId", params[4]));
				nvPairs.add(new BasicNameValuePair("path", params[5]));

		        HttpGet httpGet = new HttpGet("https://api.point.io/v2/hubs/"+params[0]+"/folders/files/preview.json?"+URLEncodedUtils.format(nvPairs, "utf-8"));

				httpGet.addHeader("Authorization", this.sessionKey);
				
				HttpResponse response = httpClient.execute(httpGet);
				Log.d("POINTIO","status code " + response.getStatusLine().toString());
				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} 
			finally{}
			return text;

		}
		
	}
}
