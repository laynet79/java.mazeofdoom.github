package com.lthorup.maze;

public class Roamer extends Character {

	protected int fieldCnt;
	
	private final float SPEED = 0.1f;

	public Roamer(int id, String spriteName, State state, Vec2 pos, float heading) {
		super(id, spriteName, state, pos, heading);
		fieldCnt = 0;
	}
	
	@Override
	protected void runBehavior(MazeGameView game) {
		if (game.running && ! game.master)
			return;
		
		if (! game.maze.validPosition(pos, radius))
			pos = game.maze.randomPosition(radius);

		fieldCnt--;
		if (fieldCnt <= 0) {
			if (state == Character.State.STANDING) {
				state = Character.State.WALKING;
				heading = randomFloat(0.0f, 360.0f);
				fieldCnt = randomInt(60, 600); // 1 to 5 seconds
			}
			else {
				state = Character.State.STANDING;
				fieldCnt = randomInt(0, 180); // 0 to 3 seconds
			}
		}
		
		if (state == Character.State.WALKING){
			float speed = SPEED;
			float cos = (float)Math.cos(Math.toRadians(heading));
			float sin = (float)Math.sin(Math.toRadians(heading));
	        float dx = speed * cos;
	        float dy = speed * sin;
	        
	        pos.x += dx;
	        pos.y += dy;
	        if (! game.maze.validPosition(pos, radius)) {
	        	pos.x -= dx;
	        	pos.y -= dy;
	        	fieldCnt = 0;
	        }
		}
	}

}
