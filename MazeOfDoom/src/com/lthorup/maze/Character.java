package com.lthorup.maze;

import java.awt.*;
import com.jogamp.opengl.*;

public class Character {

	public enum State { STANDING, WALKING, DEAD };
	
	public static final float RADIUS = 3.0f;

	protected int id;
	protected boolean isPlayer;
	protected String spriteName;
	protected SpriteAnimation sprite;
	protected Vec2 pos, lastPos;
	protected float heading;
	protected float radius;
	protected State state;
	protected int health;
	
	public Character(int id, String spriteName, State state, Vec2 pos, float heading) {
		this.id = id;
		this.isPlayer = false;
		this.spriteName = spriteName;
		this.pos = pos;
		this.lastPos = new Vec2(pos.x, pos.y);
		this.heading = heading;
		this.radius = RADIUS;
		this.state = state;
		health = 3;
	}
	
	public int id() { return id; }
	
	public Vec2 pos() { return pos; }
	public float heading() { return heading; }
	
	public void shoot() {
		if (health > 0) {
			health--;
			if (health > 0) {
				SoundEffect.PAIN.play();
			}
			else {
				SoundEffect.DEATH.play();
				state = State.DEAD;
			}
		}
	}
	
	public void kill() {
		health = 0;
		state = State.DEAD;
	}
	
	public boolean dead() { return state == State.DEAD; }

	public void update(MazeGameView game) {
		if (sprite == null)
			sprite = SpriteAnimation.create(spriteName, state);
		
		if (sprite != null) {
			
			float dist = 0.0f;

			if (state != State.DEAD) {
				runBehavior(game);
				
				float dx = pos.x - lastPos.x;
				float dy = pos.y - lastPos.y;
				dist = (float)Math.sqrt(dx*dx + dy*dy);
				lastPos.x = pos.x;
				lastPos.y = pos.y;
			}
			sprite.select(state);			
			sprite.update(dist);
			
			if (game.running && (game.master || (id == game.me))) {
				Network.get().write(new CharacterMsg(CharacterMsg.Type.UPDATE, id, isPlayer, spriteName, state, pos, heading), -1);
			}
		}
	}	
	
	protected void runBehavior(MazeGameView game) {
		// override this in sub-class to give behavior to character by modifying state and position
	}

	public void paint2d(Graphics g, int me) {
		if (state == State.DEAD)
			g.setColor(Color.RED);
		else if (id == me)
			g.setColor(Color.GREEN);
		else
			g.setColor(Color.WHITE);
		Point p = Block.sceneToMap(pos);
		g.fillRect(p.x-2, p.y, 4, 4);
	}
	
	public void paint3d(GL2 gl, Vec2 eye) {
		if (sprite != null)
			sprite.paint3d(gl,  pos,  heading,  eye);
	}
	
	//-------------------------------------------
	// random number generators
	protected float randomFloat(float min, float max) {
        return (max - min) * (float)Math.random() + min;
    }
	protected int randomInt(int min, int max) {
        return (int)((max-min+1) * Math.random() + min);
    }
	
}
