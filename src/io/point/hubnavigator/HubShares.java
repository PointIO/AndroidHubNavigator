package io.point.hubnavigator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import io.point.hubnavigator.core.*;

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
import io.point.hubnavigator.core.TaskCallback;
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

public class HubShares extends Activity implements TaskCallback, OnItemClickListener{

	private JSONObject hubShareListRc;
	private String hubId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sharelist);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    this.hubId = extras.getString("HUB_ID");
		    Log.d("POINTIO","Got the hubId passed from HubList: " + this.hubId);
		}

		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		
		
		new PointIOHubSharesList(this){

			@Override public void onPreExecute() {
				super.onPreExecute();
				Log.d("POINTIO", "starting async api HUB SHARE LIST request", null);
	        }			
			@Override public void onPostExecute(String result)
		    {
				Log.d("POINTIO", "finished async api HUB SHARE LIST request", null);
				try {
					JSONObject rc = new JSONObject(result);
					hubShareListRc = rc; // pass back the result info 
					super.onPostExecute(); // which fires callback method
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("POINTIO", "JSONException: " + e.getMessage(), null);					
				}

		    }
		}.execute(this.hubId);
	
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {

		// pass the hub ID, start new activity
		Intent intent = new Intent(HubShares.this, FileList.class);
		intent.putExtra("ROOMSHAREID", ((HubShare)a.getItemAtPosition(position)).roomShareId);
		intent.putExtra("HUB_ID", ((HubShare)a.getItemAtPosition(position)).hubId);
		startActivity(intent);

		Toast.makeText(HubShares.this,"Clicked on " + ((HubShare)a.getItemAtPosition(position)).shareName, Toast.LENGTH_LONG).show();

	}

	
	@Override
	public void done() {

		try {

			if (this.hubShareListRc.getInt("ERROR") == 0) {
				// populate list
				
				final ListView listview = (ListView) findViewById(R.id.listView1);
				
				JSONArray jLabels = this.hubShareListRc.getJSONObject("RESULT").getJSONArray("COLUMNS");
				JSONArray jData = this.hubShareListRc.getJSONObject("RESULT").getJSONArray("DATA");

				HubShare[] items = new HubShare[jData.length()];
				for (int i=0; i<jData.length(); i++)
				{
					JSONArray o = jData.getJSONArray(i);
					items[i] = new HubShare(this.hubId, o.getString(indexOfLabel("ROOMSHAREID", jLabels)), o.getString(indexOfLabel("SHARENAME", jLabels)));
					Log.d("POINTIO", "Share Name=" + jData.getJSONArray(i).getString(indexOfLabel("SHARENAME", jLabels)));
				}
				
				ArrayAdapter<HubShare> adapter = new ArrayAdapter<HubShare>(this, android.R.layout.simple_list_item_1, items);
				listview.setAdapter(adapter);
			}
			else
			{
				new AlertDialog.Builder(this)
			    .setTitle("API Call Failed")
			    .setMessage("Reason: " + this.hubShareListRc.getString("MESSAGE"))
			    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        }
			     })
			     .show();
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
	private class PointIOHubSharesList extends AsyncTask <String, Void, String> {

		private TaskCallback mCallback;
		private String sessionKey;

		public PointIOHubSharesList(TaskCallback callback)
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

		        HttpGet httpGet = new HttpGet("https://api.point.io/v2/hubs/"+params[0]+"/shares/list.json?"+URLEncodedUtils.format(nvPairs, "utf-8"));

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
