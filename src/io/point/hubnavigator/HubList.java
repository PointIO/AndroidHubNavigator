package io.point.hubnavigator;

import io.point.hubnavigator.core.Hub;
import io.point.hubnavigator.core.TaskCallback;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;  
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Toast;

public class HubList extends Activity implements OnItemClickListener, TaskCallback {
	
	private JSONObject hublistRc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hublist);

		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		
		new PointIOHubList(this){

			@Override public void onPreExecute() {
				super.onPreExecute();
				Log.d("POINTIO", "starting async api HUB LIST request", null);
	        }			
			@Override public void onPostExecute(String result)
		    {
				Log.d("POINTIO", "finished async api HUB LIST request", null);
				//Log.d("POINTIO", "raw result: " + result);
				try {
					JSONObject rc = new JSONObject(result);
					hublistRc = rc; // pass back the result info 
					super.onPostExecute(); // which fires callback method
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("POINTIO", "JSONException: " + e.getMessage(), null);					
				}

		    }
		}.execute();
	
	}


	@Override
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {		
		Toast.makeText(HubList.this,"Clicked on " + ((Hub)a.getItemAtPosition(position)).hubId, Toast.LENGTH_LONG).show();

		// pass the hub ID, start new activity
		Intent intent = new Intent(HubList.this, HubShares.class);
		intent.putExtra("HUB_ID", ((Hub)a.getItemAtPosition(position)).hubId);
		startActivity(intent);

	}

	@Override
	public void done() {

		try {

			if (this.hublistRc.getInt("ERROR") == 0) {
				// populate list
				
				final ListView listview = (ListView) findViewById(R.id.listView1);
				
				JSONArray jLabels = this.hublistRc.getJSONObject("RESULT").getJSONArray("COLUMNS");
				JSONArray jData = this.hublistRc.getJSONObject("RESULT").getJSONArray("DATA");

				Hub[] items = new Hub[jData.length()];
				for (int i=0; i<jData.length(); i++)
				{
					JSONArray o = jData.getJSONArray(i);
					items[i] = new Hub(o.getString(indexOfLabel("ROOMID", jLabels)), o.getString(indexOfLabel("ROOMNAME", jLabels)));
					Log.d("POINTIO", "Name=" + jData.getJSONArray(i).getString(indexOfLabel("ROOMNAME", jLabels)));
				}
				
				ArrayAdapter<Hub> adapter = new ArrayAdapter<Hub>(this, android.R.layout.simple_list_item_1, items);
				listview.setAdapter(adapter);
			}
			else
			{
				new AlertDialog.Builder(this)
			    .setTitle("API Call Failed")
			    .setMessage("Reason: " + this.hublistRc.getString("MESSAGE"))
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
	private class PointIOHubList extends AsyncTask <String, Void, String> {

		private TaskCallback mCallback;
		private String sessionKey;

		public PointIOHubList(TaskCallback callback)
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
				HttpGet httpGet = new HttpGet("https://api.point.io/v2/hubs/list.json");

				//Log.d("POINTIO","sending Authorization Header " + ((HubNavigator) getParent().getApplication()).getSession());
				//Log.d("POINTIO","sending Authorization Header " + this.sessionKey);
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
