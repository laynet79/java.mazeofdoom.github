package com.lthorup.maze;

import java.io.Serializable;

public class NetworkMsg implements Serializable {
	
	static final long serialVersionUID = 1;
	
	public int connectionId;
	
	public NetworkMsg() {
		connectionId = 0;
	}
}
