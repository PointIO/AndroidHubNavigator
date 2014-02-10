package io.point.hubnavigator.core;

public class Hub {
	public String hubName;
	public String hubId;
	
	public Hub(String id, String name) {
		this.hubId = id;
		this.hubName = name;
	}
	
	public String toString() {
		return this.hubName;
	}
}
