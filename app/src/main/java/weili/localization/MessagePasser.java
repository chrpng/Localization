package communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MessagePasser {

	public final String receive_block = new String();

	String serverIP;
	int serverPort;

	List<Message> receive_queue;

	public MessagePasser(String IP, int port, boolean iAmServer) {
		this.receive_queue = new ArrayList<Message>();

		this.serverIP = IP;
		this.serverPort = port;
		
		if (this.serverIP == null || this.serverPort == 0) {
			System.out.println("Unknown IP or Port!");
			return;
		}

		// run server
		Thread server = new Thread() {
			public void run() {
				listen_server();
			}
		};
		if (iAmServer){
			server.start();
		}
	}

	public Connection open_connection() {

		String ip = this.serverIP;
		int port = this.serverPort;
		
		if (ip == null || port == 0) {
			return null;
		}
		
		Socket s = null;

		try {
			s = new Socket(ip, port);
			s.setKeepAlive(true);

			Connection c = new Connection(s, this);

			return c;
		} catch (UnknownHostException e) {
			System.out.println("Client Socket error:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("Client EOF error:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("Client readline error:" + e.getMessage());
		}

		if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				System.out.println("close error:" + e.getMessage());
			}
		return null;
	}

	public synchronized void send(Message message) {
		Connection connection_to_use = open_connection();
		
		if (connection_to_use == null) {
			System.out.println("Failed to find or open connection");
			return;
		}

		// send message via TCP:
		send_tcp(message, connection_to_use);

	}

	private void send_tcp(Message message, Connection connection_to_use) {
		connection_to_use.write_object(message);
	}

	public Message receive() {
		int size = 0;
		synchronized (receive_queue) {
			size = receive_queue.size();
		}
		if (size < 1)
			return null;

		Message retreive = null;
		synchronized (receive_queue) {
			retreive = receive_queue.remove(0);
		}

		return retreive;
	}

	public synchronized void receive_message(Message message, Connection c) {

		synchronized (receive_queue) {
			receive_queue.add(message);
		}
		
		synchronized (receive_block){
			receive_block.notify();
		}
	}

	private void listen_server() {
		System.out.println("Starting MessagePasser receive server with address = " + this.serverIP);
		int counter = 0;
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(this.serverPort);

			while (true) {
				Socket clientSocket = listenSocket.accept();

				new Connection(clientSocket, this);
				System.out.println("Server received a new connection: # " + counter);
				counter++;
			}
		} catch (IOException e) {
			System.out.println("Listen socket:" + e.getMessage());
		} finally {
			try {
				if (listenSocket != null)
					listenSocket.close();
			} catch (IOException e) {
				System.out.println("Failed to close server.");
			}
		}

	}


}

class Connection extends Thread {

	DataInputStream in;
	DataOutputStream out;
	ObjectOutputStream outObj;
	ObjectInputStream inObj;

	volatile int seqNum = -1;
	Socket clientSocket;
	MessagePasser msg_passer;
	List<Message> message_queue = new ArrayList<Message>();

	public Connection(Socket aClientSocket, MessagePasser msg_passer) {
		this.msg_passer = msg_passer;
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			outObj = new ObjectOutputStream(clientSocket.getOutputStream());
			inObj = new ObjectInputStream(clientSocket.getInputStream());

			this.start();
		} catch (IOException e) {
			System.out.println("Connection:" + e.getMessage());
		}
	}

	public void run() {
		try {
			while (true) {
				Message message = (Message) inObj.readObject();
				msg_passer.receive_message(message, this);
			}

		} catch (EOFException e) {
			System.out.println("Server EOF:" + e.getMessage());
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to convert received object!");
		} finally {
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close connection!");
			}
		}

	}

	public void write_object(Object object) {
		try {
			synchronized (outObj) {
				this.outObj.writeObject(object);
			}
		} catch (IOException e) {
			System.out.println("error sending message - client side:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void enqueue(Message message) {
		synchronized (message_queue) {
			this.message_queue.add(message);
		}
	}

	public void close() {
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("Failed to close connection: ");
			e.printStackTrace();
		}
	}


}
