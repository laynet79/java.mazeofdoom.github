package com.lthorup.maze;

import java.awt.event.*;

public class Keyboard implements KeyListener
{
    private static int KEYCNT = 255;
    private enum KeyState { RELEASED, JUSTPRESSED, PRESSED }
    private boolean[] keyDown = new boolean[KEYCNT];
    private KeyState[] keyState = new KeyState[255];

    public static Keyboard keys;

    public static void init() {
    	keys = new Keyboard();
    }
    
    protected Keyboard()
    {
        for (int i = 0; i < KEYCNT; i++)
        {
            keyDown[i] = false;
            keyState[i] = KeyState.RELEASED;
        }
    }

    public synchronized void update() {
        for (int i = 0; i < KEYCNT; i++) {
            if (keyDown[i]) {
                if (keyState[i] == KeyState.RELEASED)
                    keyState[i] = KeyState.JUSTPRESSED;
                else
                    keyState[i] = KeyState.PRESSED;
            }
            else
                keyState[i] = KeyState.RELEASED;
        }
    }

    public boolean keyDown(int key) {
        return keyState[key] == KeyState.JUSTPRESSED || keyState[key] == KeyState.PRESSED;
    }

    public boolean keyJustPressed(int key) {
        return keyState[key] == KeyState.JUSTPRESSED;
    }

    public synchronized void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();  
        if (key >= 0 && key < 255)
            keyDown[key] = true;
    }
    public synchronized void keyReleased(KeyEvent e){
        int key = e.getKeyCode();
        if (key >= 0 && key < 255)
            keyDown[key] = false;
    }
    public void keyTyped(KeyEvent e) { /* unused */ }
}
