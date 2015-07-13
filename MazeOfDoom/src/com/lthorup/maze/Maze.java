package com.lthorup.maze;

import java.awt.*;
import java.io.Serializable;
import com.jogamp.opengl.*;

import com.jogamp.opengl.util.texture.Texture;
import com.lthorup.maze.Block.BlockType;

public class Maze implements Serializable {
	
	static final long serialVersionUID = 1;
	
	private int blksX, blksY;
	private Block[][] blocks;
	private static Texture floorTexture;

	//-------------------------------------------
	// constructor
	public Maze(int blksX, int blksY) {
		this.blksX = blksX;
		this.blksY = blksY;
		blocks = new Block[blksX][blksY];
		for (int y = 0; y < blksY; y++) {
			for (int x = 0; x < blksX; x++) {
				boolean isWall = x == 0 || x == (blksX-1) || y == 0 || y == (blksY-1);
				blocks[x][y] = new Block(x, y, isWall ? BlockType.OUTER_WALL : BlockType.EMPTY);
			}
		}
	}
	
	public Maze(int blksX, int blksY, String[] desc) {
		this.blksX = blksX;
		this.blksY = blksY;
		blocks = new Block[blksX][blksY];
		for (int y = 0; y < blksY; y++) {
			for (int x = 0; x < blksX; x++) {
				BlockType type;
	            if (desc[y].charAt(x) == '#') {
	                boolean isExterior = x == 0 || x == (blksX-1) || y == 0 || y == (blksY-1);
	                if (isExterior)
	                    type = BlockType.OUTER_WALL;
	                else
	                    type = BlockType.WALL;
	            }
	            else if (desc[y].charAt(x) == 'H')
	                type = BlockType.HDOOR;
	            else if (desc[y].charAt(x) == 'V')
	                type = BlockType.VDOOR;
	            else
	                type = BlockType.EMPTY;
				blocks[x][y] = new Block(x, y, type);
			}
		}		
	}
	
	//-------------------------------------------
	// get maze dimensions
	public int width()  { return blksX; }
	public int height() { return blksY; }
	
	//-------------------------------------------
	// load maze textures
	public static void loadTextures(MazeGameView gameView) {
		floorTexture = gameView.loadTexturePng("images/Floor.png");
		Block.loadTextures(gameView);
	}
	
	//-------------------------------------------
	// find a valid location within the maze
	public Vec2 randomPosition(float radius) {
		float minX = Block.sizeScene;
		float maxX = (blksX-1) * Block.sizeScene;
		float minY = Block.sizeScene;
		float maxY = (blksY-1) * Block.sizeScene;
		while (true) {
			Vec2 pos = new Vec2(randomFloat(minX, maxX), randomFloat(minY, maxY));
			if (validPosition(pos, radius+0.5f))
				return pos;
		}
	}
	
	//-------------------------------------------
	// determine if one position in maze is visible by another, an intersection point is
	// created if a wall was in the way
	public boolean visable(Vec2 start, Vec2 end, Vec2 intersection) {
		float x = (float)start.x;
		float y = (float)start.y;
		int bx = (int)(start.x / Block.sizeScene);
		int by = (int)(start.y / Block.sizeScene);
		int ebx = (int)(end.x / Block.sizeScene);
		int eby = (int)(end.y / Block.sizeScene);
		float dx = (float)end.x - start.x;
		float dy = (float)end.y - start.y;
		
		// setup constants depending on which quadrant we're walking in
		int blockStepX, blockStepY;
		int blockEdgeX, blockEdgeY;
		float largeChangeY;
		if (dx >= 0.0f) {
			if (dy >= 0.0f) {
				blockStepX = 1; blockStepY = 1;
				blockEdgeX = 1; blockEdgeY = 1;
				largeChangeY = 1e6f;
			}
			else {
				blockStepX = 1; blockStepY = -1;
				blockEdgeX = 1; blockEdgeY =  0;
				largeChangeY = -1e6f;
			}
		}
		else {
			if (dy >= 0.0f) {
				blockStepX = -1; blockStepY = 1;
				blockEdgeX = 0; blockEdgeY = 1;
				largeChangeY = 1e6f;
			}
			else {
				blockStepX = -1; blockStepY = -1;
				blockEdgeX = 0; blockEdgeY = 0;
				largeChangeY = -1e6f;
			}
		}
		
		// walk until we hit a wall or arrive at the target square
		while (bx != ebx || by != eby) {
			float nx = (bx+blockEdgeX) * Block.sizeScene;
			float ny = (by+blockEdgeY) * Block.sizeScene;
			float cy = (dx == 0.0f) ? largeChangeY : (nx-x) * dy / dx;
			if ((dy >= 0.0f) ? ((y + cy) <= ny) : ((y + cy) >= ny)) {
				x = nx;
				y = y+cy;
				bx += blockStepX;
			}
			else {
				x = x + (ny-y) * dx / dy;
				y = ny;
				by += blockStepY;
			}
			if (bx > 9 || by > 9 || blocks[bx][by].isSolid())
				break;
		}
		
		// if at the target square, set the hit point to the target
		// otherwise set the hit point to the collision point on the wall
		if (bx == ebx && by == eby) {
			intersection.x = end.x;
			intersection.y = end.y;

			return true;
		}
		else {
			intersection.x = x;
			intersection.y = y;
			return true;
		}
	}
	
	//--------------------------------------------
	// open a door at the given block
	public void openDoor(int bx, int by) {
		blocks[bx][by].openDoor();
	}
	
	//--------------------------------------------
	// perform maze updates
	public void update(MazeGameView game) {
		for (int y = 0; y < blksY; y++)
			for (int x = 0; x < blksX; x++)
				blocks[x][y].update(game);
	}
	
	//--------------------------------------------
	// Check that the current maze location is valid for an object
	// at the given position with the given radius
	public boolean validPosition(Vec2 p, float radius) {
		int bx = (int)(p.x/Block.sizeScene);
		int by = (int)(p.y/Block.sizeScene);
		for (int y = by-1; y <= (by+1); y++)
			for (int x = bx-1; x <= (bx+1); x++)
				if (blocks[x][y].blocksPosition(p, radius))
					return false;
		return true;
	}

	//-------------------------------------------
	// paint the 2d map
	public void paint2d(Graphics g) {
		for (int y = 0; y < blksY; y++) {
			for (int x = 0; x < blksX; x++) {
				if (blocks[x][y].type() != BlockType.EMPTY)
					blocks[x][y].paint2d(g);
			}
		}
	}

	//-------------------------------------------
	// paint the 3d scene
	public void paint3d(GL2 gl) {
        floorTexture.enable(gl);
        floorTexture.bind(gl);
        floorTexture.setTexParameterf(gl,GL2.GL_TEXTURE_WRAP_S,GL2.GL_REPEAT);
        floorTexture.setTexParameterf(gl,GL2.GL_TEXTURE_WRAP_T,GL2.GL_REPEAT);

        gl.glBegin( GL2.GL_TRIANGLE_STRIP );
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f( 0.0f, 0.0f, 0.0f );
        gl.glTexCoord2f(0.0f, blksY*2);
        gl.glVertex3f( 0.0f, blksY*Block.sizeScene, 0.0f);
        gl.glTexCoord2f(blksX*2, 0.0f);
        gl.glVertex3f( blksX*Block.sizeScene, 0.0f, 0.0f);
        gl.glTexCoord2f(blksX*2, blksY*2);
        gl.glVertex3f( blksX*Block.sizeScene, blksY*Block.sizeScene, 0.0f);
        gl.glEnd();
        
        floorTexture.disable(gl);

        for (int y = 0; y < blksY; y++) {
        	for (int x = 0; x < blksX; x++) {
        		blocks[x][y].paint3d(gl);
        	}
        }		
	}

	//-------------------------------------------
	// get the block at the given X/Y pixel position
	public Block selectBlock(int x, int y) {
		int bx = x / Block.sizeMap;
		int by = y / Block.sizeMap;
		return blocks[bx][by];
	}
	
	//-------------------------------------------
	// random number generators
    private float randomFloat(float min, float max) {
        return (max - min) * (float)Math.random() + min;
    }
}
