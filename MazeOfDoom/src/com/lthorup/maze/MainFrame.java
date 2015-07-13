package com.lthorup.maze;

import java.awt.EventQueue;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.Color;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.lthorup.maze.Block.BlockType;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")

public class MainFrame extends JFrame {

	private JPanel contentPane;
	private MapView mapView;
	private MazeGameView gameView;
	private Keyboard keys;
	private JRadioButton rdbtnWall;
	private JRadioButton rdbtnDoor;
	private JCheckBox chckbxMaster;
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnNew;
	private JButton btnSave;
	private JButton btnLoad;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		Keyboard.init();
		addKeyListener(Keyboard.keys);
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1143, 570);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 153, 204));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.addKeyListener(keys);
		
		mapView = new MapView();
		mapView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				gameView.mouseDown(e.getX(), e.getY());
				mapView.repaint();
			}
		});
		mapView.setBounds(10, 131, 400, 400);
		contentPane.add(mapView);
		mapView.addKeyListener(Keyboard.keys);
		
		gameView = new MazeGameView();
		gameView.setBounds(431, 11, 696, 520);
		contentPane.add(gameView);
		gameView.addKeyListener(Keyboard.keys);
		gameView.setMapView(mapView);
		mapView.setGameView(gameView);
		
		btnSave = new JButton("Save");
		btnSave.setFocusable(false);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("Maze Files", new String[] {"mz"}));
				if (fileChooser.showSaveDialog(mapView) == JFileChooser.APPROVE_OPTION) {
				  File file = fileChooser.getSelectedFile();
				  gameView.saveMaze(file);
				}
			}
		});
		btnSave.setBounds(10, 45, 89, 23);
		contentPane.add(btnSave);
		
		btnLoad = new JButton("Load");
		btnLoad.setFocusable(false);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("Maze Files", new String[] {"mz"}));
				if (fileChooser.showOpenDialog(mapView) == JFileChooser.APPROVE_OPTION) {
				  File file = fileChooser.getSelectedFile();
				  gameView.loadMaze(file);
				  setTitle(file.getName());
				  mapView.repaint();
				}		
			}
		});
		btnLoad.setBounds(10, 79, 89, 23);
		contentPane.add(btnLoad);
		
		btnNew = new JButton("New");
		btnNew.setFocusable(false);
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				gameView.newMaze(false);
				mapView.repaint();
			}
		});
		btnNew.setBounds(10, 11, 89, 23);
		contentPane.add(btnNew);
		
		rdbtnWall = new JRadioButton("Wall");
		rdbtnWall.setBackground(new Color(0, 153, 204));
		rdbtnWall.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (rdbtnWall.isSelected())
					gameView.setTool(BlockType.WALL);
			}
		});
		rdbtnWall.setFocusable(false);
		rdbtnWall.setSelected(true);
		rdbtnWall.setBounds(117, 45, 65, 23);
		contentPane.add(rdbtnWall);
		
		rdbtnDoor = new JRadioButton("Door");
		rdbtnDoor.setBackground(new Color(0, 153, 204));
		rdbtnDoor.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (rdbtnDoor.isSelected())
					gameView.setTool(BlockType.HDOOR);
			}
		});
		rdbtnDoor.setFocusable(false);
		rdbtnDoor.setBounds(117, 79, 65, 23);
		contentPane.add(rdbtnDoor);
		
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(rdbtnWall);
		bGroup.add(rdbtnDoor);
		
		chckbxMaster = new JCheckBox("master");
		chckbxMaster.setBackground(new Color(0, 153, 204));
		chckbxMaster.setFocusable(false);
		chckbxMaster.setBounds(240, 79, 89, 23);
		contentPane.add(chckbxMaster);
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (gameView.startGame(chckbxMaster.isSelected())) {
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);
					chckbxMaster.setEnabled(false);
					btnNew.setEnabled(false);
					btnSave.setEnabled(false);
					btnLoad.setEnabled(false);
				}
			}
		});
		btnStart.setFocusable(false);
		btnStart.setBounds(240, 11, 89, 23);
		contentPane.add(btnStart);
		
		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gameView.stopGame();
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				chckbxMaster.setEnabled(true);
				btnNew.setEnabled(true);
				btnSave.setEnabled(true);
				btnLoad.setEnabled(true);
			}
		});
		btnStop.setFocusable(false);
		btnStop.setBounds(240, 45, 89, 23);
		btnStop.setEnabled(false);
		contentPane.add(btnStop);

	}
}
