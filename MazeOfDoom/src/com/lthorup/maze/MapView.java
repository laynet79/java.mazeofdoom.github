package com.lthorup.maze;

import javax.swing.JPanel;
import java.awt.*;

@SuppressWarnings("serial")

public class MapView extends JPanel {

	private MazeGameView gameView;
	
	/**
	 * Create the panel.
	 */
	public MapView() {
		Block.sizeMap = Math.min(getBounds().width/20, getBounds().height/20);
	}
	
	public void setGameView(MazeGameView gameView) {
		this.gameView = gameView;
	}
	
	@Override
	public void paint(Graphics g) {
		gameView.setMapBlockSize();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getBounds().width, getBounds().height);
		gameView.paint2d(g);
	}
}
