package io.point.hubnavigator.core;

public class HubDirEntry {
	public String hubId;
	public String roomShareId;
	public String path;
	public String containerId;
	public String fileId;
	public String filename;
	public String type;
	
	public String toString() {
		if (this.type.equals("DIR"))
			return "[dir]" + this.filename;
		else
			return this.filename;
	}
}
