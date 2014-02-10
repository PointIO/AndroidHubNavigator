package io.point.hubnavigator;

import io.point.hubnavigator.core.TaskCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener, TaskCallback {
	
	private JSONObject authRc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		Button b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(this);
				
	}


	@Override
	public void done() {
		Log.d("POINTIO", "task is complete, processing vars now...");
		try {


			if (this.authRc.getInt("ERROR") == 0) {
				// store session, start new activity
				((HubNavigator) this.getApplication()).setSession(this.authRc.getJSONObject("RESULT").getString("SESSIONKEY"));
				Log.d("POINTIO", "Saved session key " + ((HubNavigator) this.getApplication()).getSession());
				
				// start new activity view
				startActivity(new Intent(Login.this, HubList.class));

				/*new AlertDialog.Builder(this)
			    .setTitle("Success!")
			    .setMessage("Login successful, session= " + this.authRc.getJSONObject("RESULT").getString("SESSIONKEY"))
			    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        }
			     })
			     .show();*/

			}
			else
			{
				new AlertDialog.Builder(this)
			    .setTitle("Login Failed")
			    .setMessage("Reason: " + this.authRc.getString("MESSAGE"))
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

	
	@Override
	public void onClick(View v) {
		
		EditText email = (EditText) findViewById(R.id.editText1);
		EditText password = (EditText) findViewById(R.id.editText2);

		new PointIOAuth(this){

			@Override public void onPreExecute() {
				Log.d("POINTIO", "starting async api AUTH request", null);
				Button b = (Button) findViewById(R.id.button1);
				b.setEnabled(false);
	        	Toast.makeText(Login.this,"Attempting to login...", Toast.LENGTH_LONG).show();
				
	        }			
			@Override public void onPostExecute(String result)
		    {
				Button b = (Button) findViewById(R.id.button1);
				b.setEnabled(true);

				Log.d("POINTIO", "finished async api AUTH request", null);
				try {
					JSONObject rc = new JSONObject(result);
					authRc = rc; // pass back the result info 
					super.onPostExecute(); // which fires callback method
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("POINTIO", "JSONException: " + e.getMessage(), null);					
				}

		    }
		}.execute(email.getText().toString(), password.getText().toString());
		
	}


	/*
	 * asynctask class takes 3 args: input type, progress type, return type
	 * custom interface 
	 * reference: 
	 * http://stackoverflow.com/questions/8623823/finish-the-calling-activity-when-asynctask-completes
	 */
	private class PointIOAuth extends AsyncTask <String, Void, String> {

		private TaskCallback mCallback;

		public PointIOAuth(TaskCallback callback)
		{
			mCallback = callback;
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
				HttpContext localContext = new BasicHttpContext();
				//HttpGet httpGet = new HttpGet("https://api.point.io/v2/auth.json");
				HttpPost httpPost = new HttpPost("https://api.point.io/v2/auth.json");

				List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
				nvPairs.add(new BasicNameValuePair("email", params[0]));
				nvPairs.add(new BasicNameValuePair("password", params[1]));
				nvPairs.add(new BasicNameValuePair("apikey", "your api key here"));
				httpPost.setEntity(new UrlEncodedFormEntity(nvPairs));

				HttpResponse response = httpClient.execute(httpPost, localContext);	
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
