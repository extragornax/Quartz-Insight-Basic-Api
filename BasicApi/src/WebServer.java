import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Arrays;
import org.json.JSONObject;

public class WebServer implements Runnable{ 
	static final int PORT = 8080;
	private Socket connect;
	
	public WebServer(Socket c) {
		connect = c;
	}

	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started on port : " + PORT);
			while (true) {
				WebServer myServer = new WebServer(serverConnect.accept());				
				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	@Override
	public void run() {
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			fileRequested = parse.nextToken().toLowerCase(); // The route requested

			// Only GET is supported
			if (!method.equals("GET")) {
				// Build header with error message content
				byte[] fileData = "{'error': 'Method not supported'}".getBytes();
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + "text/plain");
				out.println("Content-length: " + fileData.length);
				out.println(); // blank line between headers and content
				out.flush();
				dataOut.write(fileData, 0, fileData.length);
				dataOut.flush();
			} else { // Only matched GET method
				byte[] data = {};
				if (fileRequested.startsWith("/api")) { // Check that the URL starts with /api
					String[] splitted = fileRequested.split("/");
					data = parseApiRoute(splitted, fileRequested);
					// send HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + "text/plain");
					out.println("Content-length: " + data.length);
					out.println(); // blank line between headers and content
					out.flush();
					dataOut.write(data, 0, data.length);
					dataOut.flush();
				} else { // URL is not for the API - Rejected
					data = "{'error' : 'Route not supported'}".getBytes();
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + "text/plain");
					out.println("Content-length: " + data.length);
					out.println(); // blank line between headers and content
					out.flush();
					dataOut.write(data, 0, data.length);
					dataOut.flush();
				}
			}
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}

	private byte[] apiStore(String[] route) {
		byte[] data = {};
		switch (route[3]) {
			case "games":
				data = "{\"games\": [{\"name\": \"Zelda\"},{\"name\": \"Spiderman\"},{\"name\": \"Anno 1800\"},{\"name\": \"Frostpunk\"}]}".getBytes();
				break;
			default:
				data = "{'error' : 'Route not found'}".getBytes();
				break;
		}
		return data;
	}

	private byte[] apiLibrary(String[] route) {
		byte[] data = {};
		switch (route[3]) {
			case "games":
				data = "{\"games\": [{\"name\": \"Call of Duty\"},{\"name\": \"Battlefield\"},{\"name\": \"Homeworld\"},{\"name\": \"Max Payne\"}]}".getBytes();
				break;
			default:
				data = "{'error' : 'Route not found'}".getBytes();
				break;
		}
		return data;
	}

	private byte[] apiFriends(String[] route) {
		return "{\"friends\":[{\"name\": \"Kratos\"},{\"name\": \"Atreus\"},{\"name\": \"Thanos\"},{\"name\": \"Minerva\"}]}".getBytes();
	}

	private byte[] parseApiRoute(String[] route, String routestr) {
		System.out.println("User requested route [" + routestr + "]");
		String route_next = route[2];
		byte[] data = {};
		switch (route_next) {
			case "store":
				data = apiStore(route);
				break;
			case "library":
				data = apiLibrary(route);
				break;
			case "friends":
				data = apiFriends(route);
				break;
			default:
				data = "{'error' : 'Route not found'}".getBytes();
		}
		return data;
	}
	
	
}