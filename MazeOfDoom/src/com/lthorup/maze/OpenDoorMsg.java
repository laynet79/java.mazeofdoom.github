package com.lthorup.maze;

public class OpenDoorMsg extends NetworkMsg {

	static final long serialVersionUID = 1;	

	public int opener;
	public int bx, by;
	
	public OpenDoorMsg(int opener, int bx, int by) {
		this.opener = opener;
		this.bx = bx;
		this.by = by;
	}
}
