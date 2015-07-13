package com.lthorup.maze;


import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class Network {
	
	private final int LEARN_PORT = 10006;
	private final int DATA_PORT = 10007;
	private boolean exiting;
	private ArrayList<Connection> connections = null;
	private MsgQueue readQueue;
	private Thread connectThread;
	
	private static Network network; // singleton 
	
	public static void init() {
		network = new Network();
	}
	
	public static Network get() {
		return network;
	}

	private Network() {
	}
	
	public boolean start(boolean isServer) {
		exiting = false;
		connections = new ArrayList<Connection>();
		readQueue = new MsgQueue(65536);
		if (isServer)
			return startServer();
		else
			return startClient();		
	}
	
	public void stop() {
		exiting = true;
		try {
			connectThread.join();
		}
		catch (Exception e) {}
		for (Connection c : connections) {
			c.stop();
		}
		connections = null;
	}
	
	public int connectionCnt() { return connections.size(); }
	
	public boolean connected(int connectionId) {
		return (connectionId >= connections.size()) ? false : connections.get(connectionId).connected();
	}
	
	public Object read() {
		if (connections == null)
			return null;
		return readQueue.dequeue();
	}
	
	public void write(Object o, int client) {
		if (connections == null)
			return;
		synchronized (connections) {
			if (client == -1) { // -1 means broadcast
				for (Connection c : connections) {
					c.write(o);
				}
			}
			else
				connections.get(client).write(o);
		}
	}
	
	private boolean startServer() {
		connectThread = new Thread(new Runnable() {
			public void run() {
				ServerSocket serverSocket;
				DatagramSocket broadcastSocket;
				DatagramPacket broadcastPacket;
				try {
					broadcastSocket = new DatagramSocket(); 
					serverSocket = new ServerSocket(DATA_PORT, 10);
					serverSocket.setSoTimeout(1000);
					InetAddress broadcastIp = InetAddress.getByName("255.255.255.255");
					byte[] sendData = new byte[1024]; 
			        broadcastPacket = new DatagramPacket(sendData, 10, broadcastIp, LEARN_PORT); 
				}
				catch (Exception e) {
					System.err.printf("server initialization failed");
					e.printStackTrace();
					return;
				}
				
		        // continue servicing client connections until told to exit
				System.out.printf("waiting for client connection requests...\n");
				while (! exiting) {
					// listen for a client connection
					try {
						Socket dataSocket = serverSocket.accept();
						//dataSocket.setTcpNoDelay(true);
						synchronized (connections) {
				        	System.out.printf("connection created\n");
							connections.add(new Connection(dataSocket, connections.size()));
						}
					}
					catch (Exception e) { /* normal timeout waiting for connection */ }
					
					// send out empty broadcast packet to let potential clients that we are here
					try {
						broadcastSocket.send(broadcastPacket);
					}
					catch (Exception e) {}
				}
				try {
					serverSocket.close();
					broadcastSocket.close();
				}
				catch (Exception e) {}
			}
		});
		connectThread.start();
		return true;
	}
	
	private boolean startClient() {
		DatagramSocket learnSocket = null;
		try {
			// see if there is a server out there broadcasting its address
			learnSocket = new DatagramSocket(LEARN_PORT); 
			learnSocket.setSoTimeout(10000);
			byte[] receiveData = new byte[1024]; 
	        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
	        
	        learnSocket.receive(receivePacket); 
	        InetAddress serverIp = receivePacket.getAddress(); 
	        
	        //InetAddress serverIp = InetAddress.getByName("192.168.1.112");
	        
	        learnSocket.close();
	        System.out.printf("server found at address: \n", serverIp.toString());
	        
	        // connect to server and create object IO stream objects
	        Socket dataSocket = new Socket(serverIp, DATA_PORT);
			//dataSocket.setTcpNoDelay(true);
	        synchronized (connections) {
	        	System.out.printf("connection created\n");
	        	connections.add(new Connection(dataSocket, 0));
	        }
		}
		catch (Exception e) {
			System.out.printf("client initialization failed, no server found\n");
			if (learnSocket != null)
				learnSocket.close();
			return false;
		}
		return true;
	}	
	
	private class Connection {
		
		private Socket dataSocket;
		private int id;
		private ObjectInputStream input;
		private ObjectOutputStream output;
		private MsgQueue writeQueue;
		private Thread readThread, writeThread;
		private boolean connected;
		private boolean exiting;
		
		public Connection(Socket dataSocket, int id) {
			this.dataSocket = dataSocket;
			this.id = id;
			try {
				writeQueue = new MsgQueue(4096);
				output = new ObjectOutputStream(dataSocket.getOutputStream());
				output.flush();
				input = new ObjectInputStream(dataSocket.getInputStream());
				//dataSocket.setSoTimeout(1); // one ms timeout
				connected = true;
				exiting = false;
				startReadThread();
				startWriteThread();
			}
			catch (SocketTimeoutException e) {}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		public boolean connected() { return connected; }
		
		public void stop() {
			exiting = true;
			try {
				dataSocket.close();
				readThread.join();
				writeThread.join();
			}
			catch (Exception e) {}
		}
		
		public void write(Object o) {
			if (! connected)
				return;
			writeQueue.enqueue(o);
		}
		
		private void startReadThread() {
			readThread = new Thread(new Runnable() {
				public void run() {
					while (! exiting) {
						try {
							Object o = input.readObject();
							if (o instanceof ConnectionMsg) {
								((NetworkMsg) o).connectionId = id;
							}
							synchronized (readQueue) {
								readQueue.enqueue(o);
							}
						}
						catch (SocketTimeoutException e) {}
						catch (Exception e) { System.out.printf("connection close detected\n"); e.printStackTrace(); break; }
					}
					try { connected = false; input.close(); dataSocket.close(); }
					catch (Exception e) {}
				}
			});
			readThread.start();
		}
		
		private void startWriteThread() {
			writeThread = new Thread(new Runnable() {
				public void run() {
					while (! exiting) {
						try {
								while (! writeQueue.empty()) {
									Object o = writeQueue.dequeue();
									output.writeObject(o);
									output.flush();
								}
							Thread.sleep(5);
						}
						catch (SocketTimeoutException e) {}
						catch (Exception e) { System.out.printf("connection close detected\n");  e.printStackTrace(); break; }
					}
					try { connected = false; output.close(); dataSocket.close(); }
					catch (Exception e) {}
				}
			});
			writeThread.start();
		}

	}
}