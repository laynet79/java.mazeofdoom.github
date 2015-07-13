package com.lthorup.maze;

public class ConnectionMsg extends NetworkMsg {

	static final long serialVersionUID = 1;	
	
	public enum Type { JOIN, INITIALIZE, SET_ME, LEAVE };
	
	public Type type;
	public Maze maze;
	public int me;
	
	public ConnectionMsg(Type type, Maze maze, int me) {
		this.type = type;
		this.maze = maze;
		this.me = me;
	}
}
