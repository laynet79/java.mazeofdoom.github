package com.lthorup.maze;
import java.beans.Beans;
import java.io.IOException;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

@SuppressWarnings("serial")

public class OglGameView extends GLCanvas implements GLEventListener {

	private FPSAnimator animator = null;
	protected GLU glu;
	private float angle = 0.0f;
	private int resX, resY;
	private float fovX, fovY;
		
	public OglGameView() {
        Beans.setDesignTime(false); // makes it so the GUI editor doesn't crash
             
		// setup the openGL context
        GLProfile glp = GLProfile.getDefault(); 
        GLCapabilities caps = new GLCapabilities(glp); 
        caps.setDoubleBuffered(true); 
        caps.setDepthBits(32);        
        this.addGLEventListener(this); 
        
        fovX = 60.0f;
		
        // create a 60 Hz animator and start it cycling
		animator = new FPSAnimator(this,60);
		//animator.add(this);
		animator.start();
	}
	
	// override this function to perform one time GL initialization
	public void init(GL2 gl) {
		
        // initialize constant OGL settings
        gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL2.GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL2.GL_LEQUAL);  // the type of depth test to do
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.5f);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction
        gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
	
	// override this function to do GL cleanup if needed
	public void dispose(GL2 gl) { /* default does nothing */ }
	
	// override this function to handle window resize events
    public void reshape(GL2 gl, int width, int height) {
    	resX = width;
    	resY = height;
    	fovY = fovX * height / width;
        gl.glViewport( 0, 0, resX, resY );

        setPerspective(gl, fovX);
        
        // Enable the model-view transform
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    // override this function to perform scene updates for a field
    public void update() {
    	angle += 2.0f;
    }
    
    // override this function to paint the 3D scene
    public void paint(GL2 gl) {
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    	setModelViewZup(gl);
        gl.glTranslatef(0.0f, -20.0f, 0.0f);
        gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
        
        gl.glBegin( GL2.GL_TRIANGLES );
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f( -4.0f, 0.0f, -4.0f );
        gl.glColor3f( 0.0f, 1.0f, 0.0f );
        gl.glVertex3f( 4.0f, 0.0f, -4.0f );
        gl.glColor3f( 0.0f, 0.0f, 1.0f );
        gl.glVertex3f( 0.0f, 0.0f, 4.0f );
        gl.glEnd();
    }
    
    //----------------------------------------------------------------
    // setup a perspective frustum of the given horizontal field-of-view
    public void setPerspective(GL2 gl, float fovX)
    {
    	this.fovX = fovX;
        float aspect = (float)resX / resY;
        fovY = fovX / aspect;
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(fovY, aspect, 1.0, 100000.0); // fovy, aspect, zNear, zFar
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    //----------------------------------------------------------------
    // setup a perspective frustum of the given horizontal field-of-view
    public void setOrtho(GL2 gl, float left, float right, float bottom, float top)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(left, right, bottom, top);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    //----------------------------------------------------------------
    // set the model view matrix so that y is backwards and z is up
    public void setModelViewZup(GL2 gl) {
        float[] swap = new float[] { 1.0f, 0.0f, 0.0f, 0.0f,
        		                     0.0f, 0.0f, 1.0f, 0.0f,
        		                     0.0f, 1.0f, 0.0f, 0.0f,
        		                     0.0f, 0.0f, 0.0f, 1.0f };
        gl.glLoadMatrixf(swap, 0);    	
    }
    
    //----------------------------------------------------------------
    // load a texture map
    public Texture loadTexturePng(String fileName) {
    	Texture texture = null;
        try {
        	texture = TextureIO.newTexture(getClass().getClassLoader().getResource(fileName), true, ".png");
        }
        catch (GLException e) {
        	e.printStackTrace();
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
    	return texture;
    }
    
    //---------------------------------------------------------------
    // OGL handler hook functions
    @Override 
    public void init(GLAutoDrawable drawable) { 
        GL2 gl = drawable.getGL().getGL2(); 
        glu = new GLU();
        init(gl);
    }
    
    @Override 
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        dispose(gl);
    } 

    @Override 
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL2 gl = drawable.getGL().getGL2();
        reshape(gl, w, h);
    } 	
    
    @Override 
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        update();
        paint(gl);
    }
}
