package com.lthorup.maze;

public class ShootMsg extends NetworkMsg {

	static final long serialVersionUID = 1;	
	
	public int shooter, target;

	public ShootMsg(int shooter, int target) {
		this.shooter = shooter;
		this.target = target;
	}
}
