package com.lthorup.maze;

public class CharacterMsg extends NetworkMsg {
	
	static final long serialVersionUID = 1;

	public enum Type { CREATE, DESTROY, UPDATE };
	
	Type type;
	public int id;
	public boolean isPlayer;
	public String spriteName;
	public Character.State state;
	public float x, y, heading;
	
	public CharacterMsg(Type type, int id, boolean isPlayer, String spriteName, Character.State state, Vec2 pos, float heading) {
		this.type = type;
		this.id = id;
		this.isPlayer = isPlayer;
		this.spriteName = spriteName;
		this.state = state;
		this.x = pos.x;
		this.y = pos.y;
		this.heading = heading;
	}
}
