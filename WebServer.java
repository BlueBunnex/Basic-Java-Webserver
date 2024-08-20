import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class WebServer implements Runnable {
	
	public static void main(String[] args) {
		
		try {
			new WebServer(0);
		} catch (IOException e) {}
	}
	
	
	
	private final ServerSocket server;
	
	public WebServer(int port) throws IOException {
		
		// create server
		server = new ServerSocket(port);
		
		System.out.println(
				"http://" + InetAddress.getLocalHost().getHostAddress() +
				":" + server.getLocalPort() + "\n");
		
		// respond to client requests while alive
		new Thread(this).start();
	}
	
	public void run() {
		
		try {
			while (server.isBound())
				new Thread(new ClientRequestHandler(server.accept())).start();
			
			server.close();
		} catch (IOException e) {}
	}
	
	
	
	private class ClientRequestHandler implements Runnable {
		
		Socket socket;
		Scanner in;
		PrintWriter out;
		
		public ClientRequestHandler(Socket socket) throws IOException {
			
			this.socket = socket;
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream());
		}
		
		public void run() {
			
			if (!in.hasNext())
				return;
			
			String resource;
			
			// read header
			System.out.println("CLIENT:       " + socket.getInetAddress().getHostAddress());
			System.out.println("REQUEST TYPE: " +             in.next());
			System.out.println("RESOURCE:     " + (resource = in.next()));
			System.out.println("PROTOCOL:     " +             in.next());
			
			boolean successful = false;
			
			// respond for a request for the index.html
			if (resource.equals("/")) {
				
				out.print("HTTP/1.1 200 OK\n\n");
			
				out.print("<!DOCTYPE html>"
						+ "<html>"
						+ "<head>"
						+ "	<meta charset=\"UTF-8\">"
						+ "	<title>My website</title>"
						+ "</head>"
						+ "<body>"
						+ "	<h1>My website</h1>"
						+ "	<p>You just got served <span style=\"color: red;\">my website</span>!</p>"
						+ "</body>"
						+ "</html>");
				
				out.flush(); // very important!
				
				successful = true;
			}
			
			// print out result
			if (successful) {
				System.out.println("RESULT:       SUCCESSFUL");
			} else {
				System.err.println("RESULT:       DID NOT RESPOND");
			}
			System.out.println();
			
			// close
			in.close();
			out.close();
			try {
				socket.close();
			} catch (IOException e) {}
		}
	}
}
