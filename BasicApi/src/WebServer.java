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

// The tutorial can be found just here on the SSaurel's Blog : 
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
public class WebServer implements Runnable{ 
	// port to listen connection
	static final int PORT = 8080;
	
	// Client Connection via Socket Class
	private Socket connect;
	
	public WebServer(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			
			// we listen until user halts server execution
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
		// we manage our particular client connection
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();
			
			// we support only GET and HEAD methods, we check
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				byte[] fileData = "{'error': 'Method not supported'}".getBytes();

				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + "text/plain");
				out.println("Content-length: " + fileData.length);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileData.length);
				dataOut.flush();
				
			} else {
				// GET or HEAD method
				System.out.println("request ->'" + fileRequested + "'");
				// if (fileRequested.endsWith("/")) {
				// 	fileRequested += DEFAULT_FILE;
				// } else 
				byte[] data = {};
				if (fileRequested.startsWith("/api")) {
					String[] splitted = fileRequested.split("/");
					// String[] toSend =  Arrays.copyOfRange(splitted, 2, splitted.length);
					System.out.println("ARR LEN " + splitted.length);
					data = parseApiRoute(splitted);

				}
				
				// File file = new File(WEB_ROOT, fileRequested);
				// int fileLength = (int) file.length();
				// String content = getContentType(fileRequested);
				
				if (method.equals("GET")) { // GET method so we return content
					byte[] fileData = data; //readFileData(file, fileLength);
					
					// send HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + "text/plain");
					out.println("Content-length: " + data.length);
					out.println(); // blank line between headers and content, very important !
					out.flush(); // flush character output stream buffer
					
					dataOut.write(fileData, 0, data.length);
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
				String tmp = "{\"games\": [{\"name\": \"Zelda\"},{\"name\": \"Spiderman\"},{\"name\": \"Anno 1800\"},{\"name\": \"Frostpunk\"}]}";
				data = tmp.getBytes();
				break;
			default:
				data = "{'Error' : 'Route not found'}".getBytes();
				break;
		}
		return data;
	}

	private byte[] apiLibrary(String[] route) {
		byte[] data = {};
		switch (route[3]) {
			case "games":
				String tmp = "{\"games\": [{\"name\": \"Call of Duty\"},{\"name\": \"Battlefield\"},{\"name\": \"Homeworld\"},{\"name\": \"Max Payne\"}]}";
				data = tmp.getBytes();
				break;
			default:
				data = "{'Error' : 'Route not found'}".getBytes();
				break;
		}
		return data;
	}

	private byte[] apiFriends(String[] route) {
		String data = "{\"friends\":[{\"name\": \"Kratos\"},{\"name\": \"Atreus\"},{\"name\": \"Thanos\"},{\"name\": \"Minerva\"}]}";
		
		return data.getBytes();
	}

	private byte[] parseApiRoute(String[] route) {
		System.out.println("User requested route [" + route + "]");
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
				data = "{'Error' : 'Route not found'}".getBytes();
		}
		return data;
	}
	
	
}