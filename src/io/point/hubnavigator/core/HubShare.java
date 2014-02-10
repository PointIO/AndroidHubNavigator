package io.point.hubnavigator.core;

public class HubShare {
	public String shareName;
	public String roomShareId;
	public String hubId;
	
	public HubShare(String hub, String id, String name) {
		this.hubId = hub;
		this.roomShareId = id;
		this.shareName = name;
	}
	
	public String toString() {
		return this.shareName;
	}
}

