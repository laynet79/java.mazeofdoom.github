package com.lthorup.maze;

import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.lthorup.maze.Block.BlockType;

@SuppressWarnings("serial")

public class MazeGameView extends OglGameView {
	
	private final int BLKSX = 20;
	private final int BLKSY = 20;
	private final float FEET_PER_BLK = 10.0f;
	private final int NUM_CHARACTERS = 20;
	
	private MapView mapView;
	public Maze maze;
	private BlockType tool;
	private int fieldCnt;
	private int growlCntDown;
	public  int me; // index of my character
	public  ArrayList<Character> characters;
	public boolean running;
	public boolean master;
	
	public MazeGameView() {
		
		Block.sizeScene = FEET_PER_BLK;
		this.fieldCnt = 0;
		maze = new Maze(BLKSX, BLKSY);
		tool = BlockType.WALL;
		newMaze(true);
		
        // start background music
		SoundEffect.init();
		new Music("music/level3.wav", 100.0f, true);
		//SoundEffect.LEVEL3.playContinuous();        
		growlCntDown = 60 * 15;
		
		// initialize the network
		Network.init();
		running = false;
	}
	
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}
	
	public void setTool(BlockType tool) {
		this.tool = tool;
	}
	
	public void setMapBlockSize() {
		Block.sizeMap = Math.min(mapView.getBounds().width/maze.width(), mapView.getBounds().height/maze.height());
	}
	
	public boolean startGame(boolean asMaster) {
		master = asMaster;
		if (! running) {
			if (Network.get().start(master)) {
				if (! master)
					Network.get().write(new ConnectionMsg(ConnectionMsg.Type.JOIN, null, 0), 0);
				running = true;
			}
		}
		return running;
	}
	
	public void stopGame() {
		if (running) {
			running = false;
			Network.get().stop();
		}
	}
	
	@Override
	public void init(GL2 gl) {		
		
		// initialize constant OGL settings
        gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL2.GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL2.GL_LEQUAL);  // the type of depth test to do
//        gl.glEnable(GL2.GL_ALPHA_TEST);
//        gl.glAlphaFunc(GL2.GL_GREATER, 0.5f);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction
        gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // load up the maze texture maps
        Maze.loadTextures(this);
        SpriteAnimation.load(this);
	}
	
	@Override
	public void update() {		
		fieldCnt++;
		Keyboard.keys.update();
		
		handleNetwork();

        growlCntDown--;
        if (growlCntDown <= 0) {
        	SoundEffect.GROWL.play();
        	growlCntDown = randomInt(2*60, 30*60);
        }
        
        // update the maze
        maze.update(this);
        
        // update the characters
        for (Character c : characters)
        	c.update(this);

        // update the 2d view every six fields
        if ((fieldCnt % 6) == 0)
        	mapView.repaint();
	}
		
	//-------------------------------------------
	// paint the 3d Scene
    @Override
	public void paint(GL2 gl) {
    	
    	// clear the screen
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        // draw the 3D scene
        paintScene(gl); 
        
        // draw overlay symbology
        paintSymbology(gl);
	}
    
    private void paintScene(GL2 gl)
    {
        // setup 3D perspective view model view transformation (swap y and z coordinates)
    	setPerspective(gl, 60.0f);
        setModelViewZup(gl);
        Vec2 eye = characters.get(me).pos();
        float heading = characters.get(me).heading();
        
        if (characters.get(me).dead()) {
        	gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
        	gl.glRotatef(-(heading+90.0f), 0.0f, 0.0f, 1.0f);
        	gl.glTranslatef(-eye.x, -eye.y, -Block.sizeScene*0.1f);
        }
        else {
        	gl.glRotatef(-(heading+90.0f), 0.0f, 0.0f, 1.0f);
        	gl.glTranslatef(-eye.x, -eye.y, -Block.sizeScene*0.6f);
        }
    	
        // setup render state
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.5f);
        
        // paint the maze
        maze.paint3d(gl);

        // paint the characters
        for (Character c : characters)
        	c.paint3d(gl, eye);
    }
	
    private void paintSymbology(GL2 gl)
    {
        setOrtho(gl, -10, 10, -10, 10);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glDisable(GL2.GL_ALPHA_TEST);
        
        if (characters.get(me).dead()) {
            gl.glColor4f(1,0,0,0.4f);
        	gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        	gl.glVertex2f(-10, 10);
        	gl.glVertex2f(-10, -10);
        	gl.glVertex2f(10, 10);
        	gl.glVertex2f(10, -10);
        	gl.glEnd();
        }
        else {
            gl.glColor4f(1,0,0,1);
	        gl.glBegin( GL2.GL_LINES );
	        gl.glVertex2f( -1.0f, 0.0f );
	        gl.glVertex2f( 1.0f, 0.0f );
	        gl.glVertex2f( 0.0f, -1.0f );
	        gl.glVertex2f( 0.0f, 1.0f );
	        gl.glEnd();
        }
    }
    
	//-------------------------------------------
	// handle mouse pressed event
	public void mouseDown(int x, int y) {
		if (running)
			return;
		Block b = maze.selectBlock(x,  y);
		if (b.type() == BlockType.OUTER_WALL)
			return;
		if (tool == BlockType.WALL) {
			if (b.type() == BlockType.WALL)
				b.setType(BlockType.EMPTY);
			else
				b.setType(BlockType.WALL);
		}
		else if (tool == BlockType.HDOOR) {
			if (b.type() == BlockType.HDOOR)
				b.setType(BlockType.VDOOR);
			else if (b.type() == BlockType.VDOOR)
				b.setType(BlockType.EMPTY);
			else if (b.type() == BlockType.EMPTY)
				b.setType(BlockType.HDOOR);
		}
	}

	//-------------------------------------------
	// paint the 2d map
	public void paint2d(Graphics g) {
		maze.paint2d(g);
		
		for (Character c : characters)
			c.paint2d(g, me);
	}

	//-------------------------------------------
	// Create a new maze
	public void newMaze(boolean first) {
		
	    String mazeDesc[] =
	        {   "####################",
	            "#      #    #   #  #",
	            "#      #    V   #  #",
	            "#      #    #   #  #",
	            "#      #  ###   #  #",
	            "#      V  #     #H##",
	            "#      #  #        #",
	            "###H#######        #",
	            "#     #   #        #",
	            "#     #   #        #",
	            "#     #   #        #",
	            "#     #   V        #",
	            "#  ####   #        #",
	            "#  #  #   #        #",
	            "#  V  #####        #",
	            "#  #  #   #    #####",
	            "#  ####   #    #   #",
	            "#         V    V   #",
	            "#         #    #   #",
	            "####################" };

		if (first)
			maze = new Maze(BLKSX, BLKSY, mazeDesc);
		else
			maze = new Maze(BLKSX, BLKSY);
			
		// create a list of random roaming characters
		ArrayList<Character> list = new ArrayList<Character>();
		for (int i = 0; i < NUM_CHARACTERS; i++) {
			Character c;
			if (i == 0)
				c = randomCharacter(true, 0);
			else
				c = randomCharacter(false, list.size());
			list.add(c);
		}
		characters = list;
		me = 0;
	}
	
	//-------------------------------------------
	// Create a random character
	private Character randomCharacter(boolean player, int id) {
		Vec2 pos = maze.randomPosition(Character.RADIUS);
		float heading = randomFloat(0.0f, 360.0f);
		Character c;
		if (player)
			c = new Player(id, "Soldier", Character.State.STANDING, pos, heading);
		else {
			String[] names = {"Sergeant", "Imp", "Marine", "FormerHuman" };
			String name = names[randomInt(0,names.length-1)];
			c = new Roamer(id, name, Character.State.STANDING, pos, heading);
		}
		return c;
	}
	
	//-------------------------------------------
	// save maze to file
	public void saveMaze(File file) {
		FileOutputStream f;
		ObjectOutputStream out;
		try {
			f = new FileOutputStream(file);
			out = new ObjectOutputStream(f);
			out.writeObject(maze);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------
	// load maze from file
	public void loadMaze(File file) {
		FileInputStream f;
		ObjectInputStream in;
		try {
			f = new FileInputStream(file);
			in = new ObjectInputStream(f);
			maze = (Maze) in.readObject();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------
	// service network requests
	private void handleNetwork() {
		if (! running)
			return;
		
		// if we are the master, kill off players of disconnected clients
		if (master) {
			int clientCnt = Network.get().connectionCnt();
			for (int client = 0; client < clientCnt; client++) {
				int character = client + NUM_CHARACTERS;
				if (! Network.get().connected(client) && character < characters.size())
					characters.get(character).kill();
			}
		}
		
		// process all messages on the network queue
		Object obj = Network.get().read();
		while (obj != null) {
			
			// handle connection messages
			if (obj instanceof ConnectionMsg) {
				ConnectionMsg msg = (ConnectionMsg)obj;
				if (msg.type == ConnectionMsg.Type.JOIN){
					Network.get().write(new ConnectionMsg(ConnectionMsg.Type.INITIALIZE, maze, 0), msg.connectionId);
					int playerId = characters.size();
					characters.add(randomCharacter(true, playerId));
					for (Character c : characters) {
						int connectionTarget = (c.id() == playerId) ? -1 : msg.connectionId;
						Network.get().write(new CharacterMsg(CharacterMsg.Type.CREATE, c.id, c.isPlayer, c.spriteName, c.state, c.pos, c.heading), connectionTarget);
					}
					Network.get().write(new ConnectionMsg(ConnectionMsg.Type.SET_ME, maze, playerId), msg.connectionId);
				}
				else if (msg.type == ConnectionMsg.Type.INITIALIZE) {
					maze = msg.maze;
					characters = new ArrayList<Character>();
				}
				else if (msg.type == ConnectionMsg.Type.SET_ME)
					me = msg.me;
				else if (msg.type == ConnectionMsg.Type.LEAVE) {
					// don't know what to do here yet
				}
			}
			
			// handle character messages
			else if (obj instanceof CharacterMsg) {
				CharacterMsg msg = (CharacterMsg)obj;
				if (msg.type == CharacterMsg.Type.CREATE && msg.id >= characters.size()) {
					Character c;
					if (msg.isPlayer)
						c = new Player(msg.id, msg.spriteName, msg.state, new Vec2(msg.x, msg.y), msg.heading);
					else
						c = new Roamer(msg.id, msg.spriteName, msg.state, new Vec2(msg.x, msg.y), msg.heading);
					characters.add(c);
				}
				else if (msg.type == CharacterMsg.Type.UPDATE) {
					if (msg.id < characters.size() && msg.id != me) {
						Character c = characters.get(msg.id);
						c.state = msg.state;
						c.pos.x = msg.x;
						c.pos.y = msg.y;
						c.heading = msg.heading;
					}
				}
			}
			
			// handle shoot message
			else if (obj instanceof ShootMsg) {
				ShootMsg msg = (ShootMsg)obj;
				if (msg.shooter != me) {
					characters.get(msg.target).shoot();
					if (master)
						Network.get().write(msg, -1);
				}
			}
			
			// handle open door message
			else if (obj instanceof OpenDoorMsg) {
				OpenDoorMsg msg = (OpenDoorMsg)obj;
				if (msg.opener != me) {
					maze.openDoor(msg.bx, msg.by);
					if (master)
						Network.get().write(msg, -1);					
				}
			}
			
			// get next message
			obj = Network.get().read();
		}
	}
	
	//-------------------------------------------
	// random number generators
    private float randomFloat(float min, float max) {
        return (max - min) * (float)Math.random() + min;
    }
    private int randomInt(int min, int max) {
        return (int)((max-min+1) * Math.random() + min);
    }

}
