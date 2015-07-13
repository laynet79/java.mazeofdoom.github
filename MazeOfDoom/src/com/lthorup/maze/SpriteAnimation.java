package com.lthorup.maze;

import java.util.ArrayList;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;

class SpriteAnimation implements Cloneable {
	
	private static ArrayList<SpriteAnimation> sprites = new ArrayList<SpriteAnimation>();
	
	public enum AnimType { STANDING, WALKING, DYING };
	
	public String name;
	private Texture texture;
	private int framesX, frameOffset, deathStartFrame;
	private float frameSizeX, frameSizeY;
	private float frameSizeU, frameSizeV;
	private int frame, framesPerSequence;
	private float distPerFrame, distance;
	private AnimType animType;
	private int frameFields;
	
	private final int DEATH_FIELDS_PER_FRAME = 10;
	
	public static void load(OglGameView gameView) {
		float width = Block.sizeScene * 0.75f;
		float height = Block.sizeScene * 0.9f;
		sprites.add(new SpriteAnimation(gameView, "images/Soldier.png",     59, 64,  9, 8, 36, width, height, 1.0f));
		sprites.add(new SpriteAnimation(gameView, "images/Sergeant.png",    58, 63, 12, 5, 35, width, height, 0.9f));
		sprites.add(new SpriteAnimation(gameView, "images/Imp.png",         59, 62, 12, 5, 40, width, height, 1.0f));
		sprites.add(new SpriteAnimation(gameView, "images/Marine.png",      58, 60, 11, 5, 35, width, height, 1.0f));
		sprites.add(new SpriteAnimation(gameView, "images/FormerHuman.png", 58, 63, 12, 5, 35, width, height, 1.0f));
	}
	
	public static SpriteAnimation create(String name, Character.State state) {
		for (SpriteAnimation s : sprites) {
			if (name.equals(s.name)){
				try {
					SpriteAnimation c = (SpriteAnimation)s.clone();
					if (state == Character.State.DEAD) {
						c.animType = AnimType.DYING;
						c.frame = c.framesPerSequence;
					}
					else
						c.select(state);
					return c;					
				}
				catch (CloneNotSupportedException e) {
					return null;
				}
			}
		}
		return null;
	}
	
	public SpriteAnimation(
			OglGameView gameView, String fileName,
			int frameResX, int frameResY, int framesX, int frameOffset, int deathStartFrame,
			float frameSizeX, float frameSizeY, float distPerFrame) {
		int beginIndex = fileName.lastIndexOf('/') + 1;
		int endIndex = fileName.lastIndexOf('.');
		this.name = fileName.substring(beginIndex, endIndex);
		this.texture = gameView.loadTexturePng(fileName);
		this.framesX = framesX;
		this.frameOffset = frameOffset;
		this.deathStartFrame = deathStartFrame;
		this.frameSizeU = (float)frameResX / texture.getWidth();
		this.frameSizeV = (float)frameResY / texture.getHeight();
		this.frameSizeX = frameSizeX;
		this.frameSizeY = frameSizeY;
		this.frame = 0;
		this.distPerFrame = distPerFrame;
		this.distance = distPerFrame;
		this.framesPerSequence = 4;
		this.animType = AnimType.STANDING;
	}
	
	public void select(Character.State state) {
		AnimType newType = AnimType.STANDING;
		if (state == Character.State.DEAD)
			newType = AnimType.DYING;
		else if (state == Character.State.STANDING)
			newType = AnimType.STANDING;
		else if (state == Character.State.WALKING)
			newType = AnimType.WALKING;
		
		if (this.animType != newType) {
			this.animType = newType;
			distance = distPerFrame;
			frame = 0;
			frameFields = 0;
		}
	}

	public void update(float dist) {
		if (animType == AnimType.WALKING) {
			distance += dist;
			if (distance >= distPerFrame) {
				frame++;
				if (frame >= framesPerSequence)
					frame = 0;
				distance = 0.0f;
			}
		}
		else if (animType == AnimType.DYING) {
			int fieldsPerFrame = DEATH_FIELDS_PER_FRAME;
			frameFields++;
			if (frameFields >= fieldsPerFrame && frame < 4) {
				frame++;
				frameFields = 0;
			}
		}
	}
	
	public void paint3d(GL2 gl, Vec2 pos, float heading, Vec2 eye) {
        
        // determine which of the 8 sectors of the sprite that is to be displayed
		float headingToEye = Vec2.sub(eye,pos).angle();
		float dir = heading - headingToEye;
		if (dir < 0.0f)
			dir += 360.0f;
		int sector = (int)((dir + 22.5f) / 45.0f);
		if (sector == 8)
			sector = 7;
		boolean reverse = false;
		if (sector > 4) {
			sector = 8 - sector;
			reverse = true;
		}
		
		// compute the texture coordinates for the current frame
		int index;
		if (animType == AnimType.DYING)
			index = frame + deathStartFrame;
		else
			index = sector + (frame * frameOffset);
		int y = index / framesX;
		int x = index % framesX;
		float leftU = x * frameSizeU;
		float rightU = leftU + frameSizeU;
		float topV = y * frameSizeV;
		float bottomV = topV + frameSizeV;
		if (reverse) {
			float temp = leftU;
			leftU = rightU;
			rightU = temp;
		}
		
		// rotate the stamp polygon so that it faces the eye
		float px = pos.x - eye.x;
		float py = pos.y - eye.y;
		float len = (float)Math.sqrt(px*px+py*py);
		float dy = -px * frameSizeX * 0.5f / len;
		float dx =  py * frameSizeX * 0.5f / len;
				
		// setup texture state for transparency
        texture.enable(gl);
        texture.bind(gl);        				
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE); 
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // draw the stamp polygon
        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
        gl.glTexCoord2f(leftU, topV);
        gl.glVertex3f( pos.x+dx, pos.y+dy, frameSizeY);
        gl.glTexCoord2f(leftU, bottomV);
        gl.glVertex3f( pos.x+dx, pos.y+dy, 0.0f);
        gl.glTexCoord2f(rightU, topV);
        gl.glVertex3f( pos.x-dx, pos.y-dy, frameSizeY);
        gl.glTexCoord2f(rightU, bottomV);
        gl.glVertex3f( pos.x-dx, pos.y-dy, 0.0f);
        gl.glEnd();
        
        texture.disable(gl);
	}
}
