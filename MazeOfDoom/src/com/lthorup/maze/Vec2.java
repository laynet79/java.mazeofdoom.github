package com.lthorup.maze;

public class Vec2 {

	public float x, y;
	
	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	public Vec2(float heading) {
		this.x = (float)Math.cos(Math.toRadians(heading));
		this.y = (float)Math.sin(Math.toRadians(heading));
	}
	
	public float length() {
		return (float)Math.sqrt(x*x + y*y);
	}
	
	public float angle() {
		double angle = Math.toDegrees(Math.atan2(y,x));
		if (angle < 0.0)
			angle += 360.0;
		return (float)angle;
	}
	
	public static Vec2 add(Vec2 a, Vec2 b) {
		return new Vec2(a.x+b.x, a.y+b.y);
	}
	public static Vec2 sub(Vec2 a, Vec2 b) {
		return new Vec2(a.x-b.x, a.y-b.y);
	}
	public static float dot(Vec2 a, Vec2 b) {
		return a.x*b.x + a.y*b.y;
	}
}
