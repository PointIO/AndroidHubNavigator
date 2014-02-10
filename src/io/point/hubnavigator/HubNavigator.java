package io.point.hubnavigator;

import android.app.Application;

public class HubNavigator extends Application {
	
	private String sessionKey;
	
	public void setSession(String session)
	{
		this.sessionKey = session;
	}

	public String getSession()
	{
		return this.sessionKey;
	}

}
