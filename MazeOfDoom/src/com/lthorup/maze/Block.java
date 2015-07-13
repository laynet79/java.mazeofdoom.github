package com.lthorup.maze;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;

import java.awt.event.*;

public class Block implements Serializable {
	
	static final long serialVersionUID = 1;
	
	public enum BlockType { EMPTY, OUTER_WALL, WALL, HDOOR, VDOOR };
	public enum DoorState { CLOSED, OPENED, OPENING, CLOSING };
	
	public static int   sizeMap;
	public static float sizeScene;

	private static Texture brickTexture;
	private static Texture doorTexture;
	private static Texture doorJamTexture;
	
	private final int DOOR_ANIM_OPEN_FRAMES = 40 * 2;
	private final int DOOR_ANIM_WAIT_FRAMES = 40 * 4;
	
	private int x, y;
	private BlockType type;
	private int doorAnimFrame;
	private DoorState doorState;
	
	public static void loadTextures(MazeGameView gameView) {
		brickTexture = gameView.loadTexturePng("images/brick2.png");
		doorTexture = gameView.loadTexturePng("images/door4.png");
		doorJamTexture = gameView.loadTexturePng("images/doorjam.png");
	}
	
	public Block(int x, int y, BlockType type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.doorAnimFrame = 0;
		this.doorState = DoorState.CLOSED;
	}
	
	public void setType(BlockType type) {
		this.type = type;
	}
	
	public BlockType type() {
		return type;
	}
	
	public boolean isSolid() {
		return (type == BlockType.EMPTY || doorState == DoorState.OPENED);
	}
	
	public static Point sceneToMap(Vec2 scene) {
		return new Point((int)(scene.x * sizeMap / sizeScene), (int)(scene.y * sizeMap / sizeScene));
	}
	
	public boolean blocksPosition(Vec2 p, float radius) {
		if (type == BlockType.EMPTY || doorState == DoorState.OPENED)
			return false;
		float left = x * sizeScene - radius;
		float right = (x+1) * sizeScene + radius;
		float front = y * sizeScene - radius;
		float back = (y+1) * sizeScene + radius;
		return p.x > left && p.x < right && p.y > front && p.y < back;
	}
	
	public void update(MazeGameView game) {
		if (type == BlockType.HDOOR || type == BlockType.VDOOR) {
			Vec2 eye = game.characters.get(game.me).pos();
			int eyeBlockX = (int)(eye.x/Block.sizeScene);
			int eyeBlockY = (int)(eye.y/Block.sizeScene);
	        boolean openDoor = Keyboard.keys.keyJustPressed(KeyEvent.VK_SPACE);
			if (openDoor && Math.abs(eyeBlockX-x) <= 2 && Math.abs(eyeBlockY-y) <= 2) {
				if (openDoor())
					Network.get().write(new OpenDoorMsg(game.me, x, y), -1);
			}
			
			if (doorState == DoorState.OPENING){
				doorAnimFrame++;
				if (doorAnimFrame == DOOR_ANIM_OPEN_FRAMES) {
					doorAnimFrame = 0;
					doorState = DoorState.OPENED;
				}
			}
			else if (doorState == DoorState.OPENED) {
				// don't close door if someone is standing in it
				doorAnimFrame++;
				float left = x * Block.sizeScene;
				float right = left + Block.sizeScene;
				float top = y * Block.sizeScene;
				float bottom = top + Block.sizeScene;
				for (Character c : game.characters) {
					float cl = c.pos().x - c.radius;
					float cr = c.pos().x + c.radius;
					float ct = c.pos().y - c.radius;
					float cb = c.pos().y + c.radius;
					if (cr < left || cl > right || cb < top || ct > bottom)
						continue;
					doorAnimFrame = 0;
					break;
				}
				if (doorAnimFrame == DOOR_ANIM_WAIT_FRAMES) {
					doorAnimFrame = DOOR_ANIM_OPEN_FRAMES - 1;
					doorState = DoorState.CLOSING;
					SoundEffect.DOOR_CLOSE.play();
				}
			}
			else if (doorState == DoorState.CLOSING) {
				doorAnimFrame--;
				if (doorAnimFrame == 0)
					doorState = DoorState.CLOSED;
			}
		}
	}
	
	public boolean openDoor() {
		if (doorState == DoorState.CLOSED) {
			doorState = DoorState.OPENING;
			SoundEffect.DOOR_OPEN.play();
			return true;
		}
		return false;
	}
	
	public void paint2d(Graphics g) {
		if (type == BlockType.OUTER_WALL || type == BlockType.WALL)
		{
			g.setColor(Color.RED);
			g.fillRect(x*sizeMap+1, y*sizeMap+1, sizeMap-2, sizeMap-2);
		}
		else if (type == BlockType.HDOOR) {
			g.setColor(Color.WHITE);
			g.drawLine(x*sizeMap, y*sizeMap+(sizeMap/2), (x+1)*sizeMap, y*sizeMap+(sizeMap/2));
		}
		else if (type == BlockType.VDOOR) {
			g.setColor(Color.WHITE);
			g.drawLine(x*sizeMap+(sizeMap/2), y*sizeMap, x*sizeMap+(sizeMap/2), (y+1)*sizeMap);
		}
	}
	
	public void paint3d(GL2 gl) {
		if (type == BlockType.EMPTY) {
			return;
		}
		else if (type == BlockType.WALL || type == BlockType.OUTER_WALL) {
			float top = sizeScene;
			float bottom = 0;
			float left = x * sizeScene;
			float right = left + sizeScene;
			float front = y * sizeScene;
			float back = front + sizeScene;
			
	        brickTexture.enable(gl);
	        brickTexture.bind(gl);        
			
			// front
	        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
	        gl.glTexCoord2f(0.0f, 0.0f);
	        gl.glVertex3f( right, front, top );
	        gl.glTexCoord2f(0.0f, 1.0f);
	        gl.glVertex3f( right, front, bottom);
	        gl.glTexCoord2f(1.0f, 0.0f);
	        gl.glVertex3f( left, front, top);
	        gl.glTexCoord2f(1.0f, 1.0f);
	        gl.glVertex3f( left, front, bottom);
	        gl.glEnd();
			
	        // back
	        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
	        gl.glTexCoord2f(0.0f, 0.0f);
	        gl.glVertex3f( left, back, top );
	        gl.glTexCoord2f(0.0f, 1.0f);
	        gl.glVertex3f( left, back, bottom );
	        gl.glTexCoord2f(1.0f, 0.0f);
	        gl.glVertex3f( right, back, top);
	        gl.glTexCoord2f(1.0f, 1.0f);
	        gl.glVertex3f( right, back, bottom );
	        gl.glEnd();

	        // left
	        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
	        gl.glTexCoord2f(0.0f, 0.0f);
	        gl.glVertex3f( left, front, top );
	        gl.glTexCoord2f(0.0f, 1.0f);
	        gl.glVertex3f( left, front, bottom );
	        gl.glTexCoord2f(1.0f, 0.0f);
	        gl.glVertex3f( left, back, top );
	        gl.glTexCoord2f(1.0f, 1.0f);
	        gl.glVertex3f( left, back, bottom );
	        gl.glEnd();

	        // right
	        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
	        gl.glTexCoord2f(0.0f, 0.0f);
	        gl.glVertex3f( right, back, top );
	        gl.glTexCoord2f(0.0f, 1.0f);
	        gl.glVertex3f( right, back, bottom);
	        gl.glTexCoord2f(1.0f, 0.0f);
	        gl.glVertex3f( right, front, top);
	        gl.glTexCoord2f(1.0f, 1.0f);
	        gl.glVertex3f( right, front, bottom);
	        gl.glEnd();
		}
		else if (type == BlockType.HDOOR || type == BlockType.VDOOR) {
			
			final float jamHover = 0.04f;
			
			float doorOffset = 0.0f;
			if (doorState == DoorState.OPENING || doorState == DoorState.CLOSING)
				doorOffset = sizeScene * doorAnimFrame / DOOR_ANIM_OPEN_FRAMES;
			else if (doorState == DoorState.OPENED)
				doorOffset = sizeScene;
			
			float top = sizeScene;
			float bottom = 0.0f;
			
			if (type == BlockType.HDOOR) {
				float left = x * sizeScene;
				float right = left + sizeScene;
				float doory = y * sizeScene + (sizeScene/2);
				float jamLeft = left + jamHover;
				float jamRight = right - jamHover;
				float jamFront = y * sizeScene + (sizeScene*0.4f);
				float jamBack = jamFront + (sizeScene*0.2f);
				
				// door
		        doorTexture.enable(gl);
		        doorTexture.bind(gl);        
		        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
		        gl.glTexCoord2f(0.0f, 0.0f);
		        gl.glVertex3f( left + doorOffset, doory, top );
		        gl.glTexCoord2f(0.0f, 1.0f);
		        gl.glVertex3f( left + doorOffset, doory, bottom);
		        gl.glTexCoord2f(1.0f, 0.0f);
		        gl.glVertex3f( right + doorOffset, doory, top);
		        gl.glTexCoord2f(1.0f, 1.0f);
		        gl.glVertex3f( right + doorOffset, doory, bottom);
		        gl.glEnd();
		        doorTexture.disable(gl);
		        
		        // door jam
		        doorJamTexture.enable(gl);
		        doorJamTexture.bind(gl);        
		        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
		        gl.glTexCoord2f(0.0f, 0.0f);
		        gl.glVertex3f( jamLeft, jamBack, top );
		        gl.glTexCoord2f(0.0f, 1.0f);
		        gl.glVertex3f( jamLeft, jamBack, bottom);
		        gl.glTexCoord2f(1.0f, 0.0f);
		        gl.glVertex3f( jamLeft, jamFront, top);
		        gl.glTexCoord2f(1.0f, 1.0f);
		        gl.glVertex3f( jamLeft, jamFront, bottom);
		        gl.glEnd();
		        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
		        gl.glTexCoord2f(0.0f, 0.0f);
		        gl.glVertex3f( jamRight, jamFront, top );
		        gl.glTexCoord2f(0.0f, 1.0f);
		        gl.glVertex3f( jamRight, jamFront, bottom);
		        gl.glTexCoord2f(1.0f, 0.0f);
		        gl.glVertex3f( jamRight, jamBack, top);
		        gl.glTexCoord2f(1.0f, 1.0f);
		        gl.glVertex3f( jamRight, jamBack, bottom);
		        gl.glEnd();
		        doorJamTexture.disable(gl);
			}
			else if (type == BlockType.VDOOR) {
				float front = y * sizeScene;
				float back = front + sizeScene;
				float doorx = x * sizeScene + (sizeScene/2);
				float jamFront = front + jamHover;
				float jamBack = back - jamHover;
				float jamLeft = x * sizeScene + (sizeScene*0.4f);
				float jamRight = jamLeft + (sizeScene*0.2f);
				
				// door
		        doorTexture.enable(gl);
		        doorTexture.bind(gl);        
		        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
		        gl.glTexCoord2f(0.0f, 0.0f);
		        gl.glVertex3f( doorx, front + doorOffset, top );
		        gl.glTexCoord2f(0.0f, 1.0f);
		        gl.glVertex3f( doorx, front + doorOffset, bottom);
		        gl.glTexCoord2f(1.0f, 0.0f);
		        gl.glVertex3f( doorx, back + doorOffset, top);
		        gl.glTexCoord2f(1.0f, 1.0f);
		        gl.glVertex3f( doorx, back + doorOffset, bottom);
		        gl.glEnd();
		        doorTexture.disable(gl);

		        // door jam
		        doorJamTexture.enable(gl);
		        doorJamTexture.bind(gl);        
		        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
		        gl.glTexCoord2f(0.0f, 0.0f);
		        gl.glVertex3f( jamLeft, jamFront, top );
		        gl.glTexCoord2f(0.0f, 1.0f);
		        gl.glVertex3f( jamLeft, jamFront, bottom);
		        gl.glTexCoord2f(1.0f, 0.0f);
		        gl.glVertex3f( jamRight, jamFront, top);
		        gl.glTexCoord2f(1.0f, 1.0f);
		        gl.glVertex3f( jamRight, jamFront, bottom);
		        gl.glEnd();
		        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
		        gl.glTexCoord2f(0.0f, 0.0f);
		        gl.glVertex3f( jamRight, jamBack, top );
		        gl.glTexCoord2f(0.0f, 1.0f);
		        gl.glVertex3f( jamRight, jamBack, bottom);
		        gl.glTexCoord2f(1.0f, 0.0f);
		        gl.glVertex3f( jamLeft, jamBack, top);
		        gl.glTexCoord2f(1.0f, 1.0f);
		        gl.glVertex3f( jamLeft, jamBack, bottom);
		        gl.glEnd();
		        doorJamTexture.disable(gl);
			}
		}
	}
	
	
}
