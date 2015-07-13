package com.lthorup.maze;

import java.awt.event.*;

public class Player extends Character {

	private boolean onWall;
	private boolean turningLeft, turningRight;
	private final float minRate = 0.1f;
	private final float maxRate = 1.0f;
	private final float deltaRate = 0.01f;
	private float rate;
	private int shootWait;
	
	private final float SPEED = 0.2f;
	
	public Player(int id, String spriteName, State state, Vec2 pos, float heading) {
		super(id, spriteName, state, pos, heading);
		this.isPlayer = true;
		this.health = 10;
		this.onWall = false;
		turningLeft = turningRight = false;
		rate = minRate;
	}
	
	@Override
	protected void runBehavior(MazeGameView game) {
		if (id == game.me) {
			if (! game.maze.validPosition(pos, radius))
				pos = game.maze.randomPosition(radius);			
			
			float speed = SPEED;
			
			if (Keyboard.keys.keyDown(KeyEvent.VK_Z))
				speed *= 3;

			if (Keyboard.keys.keyDown(KeyEvent.VK_LEFT)) {
				if (turningRight)
					rate = minRate;
				else if (rate < maxRate)
					rate += deltaRate;
				turningLeft = true;
			}
			else if (Keyboard.keys.keyDown(KeyEvent.VK_RIGHT)) {
				if (turningLeft)
					rate = minRate;
				else if (rate < maxRate)
					rate += deltaRate;
				turningRight = true;
			}
			else {
				turningLeft = turningRight = false;
				rate = minRate;
			}				
			
			float cos = (float)Math.cos(Math.toRadians(heading));
			float sin = (float)Math.sin(Math.toRadians(heading));
			float cos90 = (float)Math.cos(Math.toRadians(heading+90));
			float sin90 = (float)Math.sin(Math.toRadians(heading+90));
			float dx = 0.0f;
			float dy = 0.0f;
			
	        if (Keyboard.keys.keyDown(KeyEvent.VK_UP)) {
	            dx = speed * cos;
	            dy = speed * sin;
	        }
	        else if (Keyboard.keys.keyDown(KeyEvent.VK_DOWN)) {
	            dx = -(speed * cos);
	            dy = -(speed * sin);
	        }
	        if (Keyboard.keys.keyDown(KeyEvent.VK_LEFT)) {
	        	if (Keyboard.keys.keyDown(KeyEvent.VK_SHIFT)) {
	        		dx -= (speed * cos90);
	        		dy -= (speed * sin90);
	        	}
	        	else
	        		heading -= rate;
	        }
	        if (Keyboard.keys.keyDown(KeyEvent.VK_RIGHT)) {
	        	if (Keyboard.keys.keyDown(KeyEvent.VK_SHIFT)){
	        		dx += (speed * cos90);
	        		dy += (speed * sin90);      		
	        	}
	        	else
	        		heading += rate;
	        }
	        if (heading >= 360.0f)
	        	heading -= 360.0f;
	        if (heading < 0.0f)
	        	heading += 360.0f;
	
	        boolean hitWall = false;
	        pos.x += dx;
	        if (! game.maze.validPosition(pos, radius)) {
	        	pos.x -= dx;
	        	hitWall = true;
	        }
	        pos.y += dy;
	        if (! game.maze.validPosition(pos, radius)) {
	        	pos.y -= dy;
	        	hitWall = true;
	        }
	        
	        if (hitWall && ! onWall) {
	        	SoundEffect.WALK_INTO_WALL.playIfNotRunning();
	        	onWall = true;
	        }
	        if ((dx != 0 || dy != 0) && ! hitWall)
	        	onWall = false;
	        
	        // handle gun fire
	        if (shootWait > 0)
	        	shootWait--;
			if (Keyboard.keys.keyJustPressed(KeyEvent.VK_CONTROL) && shootWait == 0) {
				shootWait = 50;
				SoundEffect.SHOTGUN.play();
				Vec2 normal = new Vec2(heading + 90.0f);
				Vec2 intersection = new Vec2(0,0);
				Character hit = null;
				float hitDist = 1e6f;;
				for (Character c : game.characters) {
					if (c != this) {
						Vec2 eyeToChar = Vec2.sub(c.pos, pos);
						float dist = eyeToChar.length();
						if (dist < 50.0f && Math.abs(Vec2.dot(normal, eyeToChar)) < radius  && game.maze.visable(pos, c.pos, intersection)) {
							if (hit == null || dist < hitDist) {
								hit = c;
								hitDist = dist;
							}
						}
					}
				}
				if (hit != null) {
					hit.shoot();
					Network.get().write(new ShootMsg(game.me, hit.id()), -1);					
				}
			}
		}
		
		if (pos.x != lastPos.x || pos.y != lastPos.y)
			state = State.WALKING;
		else
			state = State.STANDING;
	}	
}
