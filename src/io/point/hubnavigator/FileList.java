package io.point.hubnavigator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.point.hubnavigator.core.HubDirEntry;
import io.point.hubnavigator.core.HubShare;
import io.point.hubnavigator.core.TaskCallback;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FileList extends Activity implements TaskCallback, OnItemClickListener{

	private JSONObject FileListRc;
	private String hubId;
	private String roomShareId;
	private String containerId;
	private String path;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filelist);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    this.hubId = extras.getString("HUB_ID");
		    this.roomShareId = extras.getString("ROOMSHAREID");
		    if (extras.containsKey("CONTAINERID"))
		    	this.containerId = extras.getString("CONTAINERID");
		    else
		    	this.containerId = "";
		    if (extras.containsKey("PATH"))
		    	this.path = extras.getString("PATH");
		    else
		    	this.path = "";
		    Log.d("POINTIO","Got the roomShareId passed from HubList: " + this.roomShareId);
		}

		ListView lv = (ListView) findViewById(R.id.listView1);

		lv.setOnItemClickListener(this);
		
		
		new PointIOHubFileList(this){

			@Override public void onPreExecute() {
				super.onPreExecute();
				Log.d("POINTIO", "starting async api HUB FILE LIST request", null);
	        }			
			@Override public void onPostExecute(String result)
		    {
				Log.d("POINTIO", "finished async api HUB FILE LIST request", null);
				try {
					JSONObject rc = new JSONObject(result);
					FileListRc = rc; // pass back the result info 
					super.onPostExecute(); // which fires callback method
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("POINTIO", "JSONException: " + e.getMessage(), null);					
				}

		    }
		}.execute(this.hubId, this.roomShareId, this.containerId, this.path);
	
	}

	@Override
	public void done() {

		try {

			if (this.FileListRc.getInt("ERROR") == 0) {
				// populate list
				Log.d("POINTIO", "Result: " + this.FileListRc.toString());
				
				final ListView listview = (ListView) findViewById(R.id.listView1);
				
				JSONArray jLabels = this.FileListRc.getJSONObject("RESULT").getJSONArray("COLUMNS");
				JSONArray jData = this.FileListRc.getJSONObject("RESULT").getJSONArray("DATA");

				HubDirEntry[] items = new HubDirEntry[jData.length()];
				for (int i=0; i<jData.length(); i++)
				{
					JSONArray o = jData.getJSONArray(i);
					HubDirEntry newEntry = new HubDirEntry();
					newEntry.hubId = this.hubId;
					newEntry.roomShareId = this.roomShareId;
					newEntry.fileId = o.getString(indexOfLabel("FILEID", jLabels));
					newEntry.filename = o.getString(indexOfLabel("NAME", jLabels));
					newEntry.path = o.getString(indexOfLabel("PATH", jLabels));
					newEntry.type = o.getString(indexOfLabel("TYPE", jLabels));
					Log.d("POINTIO", "adding dir entry " + newEntry.toString());
					items[i] = newEntry;

				}
				
				ArrayAdapter<HubDirEntry> adapter = new ArrayAdapter<HubDirEntry>(this, android.R.layout.simple_list_item_1, items);
				listview.setAdapter(adapter);
			}
			else
			{
				Toast.makeText(FileList.this,"API Call Failed: " + this.FileListRc.getString("MESSAGE"), Toast.LENGTH_LONG).show();

			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
		Toast.makeText(FileList.this,"Clicked on " + ((HubDirEntry)a.getItemAtPosition(position)).filename, Toast.LENGTH_LONG).show();

		Intent intent;
		
		// pass the data we need, start new activity
		if (((HubDirEntry)a.getItemAtPosition(position)).type.equalsIgnoreCase("dir"))
			intent = new Intent(FileList.this, FileList.class);
		else
			intent = new Intent(FileList.this, DocumentViewer.class);
		intent.putExtra("ROOMSHAREID", ((HubDirEntry)a.getItemAtPosition(position)).roomShareId);
		intent.putExtra("HUB_ID", ((HubDirEntry)a.getItemAtPosition(position)).hubId);
		intent.putExtra("FILEID", ((HubDirEntry)a.getItemAtPosition(position)).fileId);
		intent.putExtra("FILENAME", ((HubDirEntry)a.getItemAtPosition(position)).filename);
		intent.putExtra("PATH", ((HubDirEntry)a.getItemAtPosition(position)).path);
		intent.putExtra("CONTAINERID", ((HubDirEntry)a.getItemAtPosition(position)).containerId);
		startActivity(intent);
		
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
	private class PointIOHubFileList extends AsyncTask <String, Void, String> {

		private TaskCallback mCallback;
		private String sessionKey;

		public PointIOHubFileList(TaskCallback callback)
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
				nvPairs.add(new BasicNameValuePair("containerId", params[2]));
				nvPairs.add(new BasicNameValuePair("path", params[3]));

		        HttpGet httpGet = new HttpGet("https://api.point.io/v2/hubs/"+params[0]+"/folders/list.json?"+URLEncodedUtils.format(nvPairs, "utf-8"));

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
