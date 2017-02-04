/**
 * WebProxy Class
 *
 * @author      Muhammad Wahaj Taseer
 * @version     2.done, 3 Feb 2017
 *
 */
import java.util.Scanner;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WebProxy {

  String line;
  private int port;
  Scanner inputClient = null;
  DataOutputStream outputClient = null;
  ServerSocket serverSocket = null;
  HashMap<String, String> request = 									// Creating a hashmap to store the request
	new HashMap<String, String>();
  Socket socket1, socket2;
  String filePath;
  byte[] file = null;
  File checkFile = null;


  /**
  *  Constructor that initializes the server listening port
  *
  * @param port      Proxy server listening port
  */
	public WebProxy(int port) {
		this.port = port;
	}

  /**
  * The webproxy logic goes here
  */
	public void start(){
		try{
			serverSocket = new ServerSocket(port);
			socket1 = serverSocket.accept();

			// Connection from client made, set up streams
			inputClient = new Scanner(new InputStreamReader(socket1.getInputStream()));
			outputClient = new DataOutputStream(socket1.getOutputStream());

			String next = inputClient.nextLine(); 						// Getting first line of request
			String[] nextArray = next.split(" ", 2);					// Parsing and storing it in the hashmap
			request.put(nextArray[0], nextArray[1]);

			next = inputClient.nextLine(); 								// Getting 2nd line of request
			while (!(next.isEmpty())) { 								// while the next line (starting at 2) is not empty
				nextArray = next.split(":", 2);							// Parse and store in hashmap
				request.put(nextArray[0], nextArray[1]);

				next = inputClient.nextLine(); 							// 3rd line +
			}

			if ((request.get("GET")) == null){
				outputClient.write
				("HTTP:/1.1 400 BAD REQUEST".getBytes());				// To client
				outputClient.flush();
				
				System.out.println("HTTP:/1.1 400 BAD REQUEST");		// To console
				System.out.println("Connection: close");				// Close the streams
				
				inputClient.close();
				outputClient.close();
				return;
			}

			// If file is in the cache
			if (checkCache() == true){
				System.out.println("Serving from cache!");
				
				// Getting the size of the file
				long size = checkFile.length();
				file = new byte[(int) size];
				FileInputStream fileInputStream = null;
				
				// Reading the file into byte array
				try{
				fileInputStream = new FileInputStream(filePath);
				fileInputStream.read(file);
				}
				finally{
					fileInputStream.close();
				}
				
				// Creating the response
				String responseHeader = "HTTP/1.1 200 OK\r\nConnection: close\r\n\r\n";
				
				// Sending response in bytes to client
				outputClient.write(responseHeader.getBytes());
				outputClient.write(file);
				outputClient.flush();
				
			}
			// Else we retrieve from server
			else{
				System.out.println("Serving from Origin Server!");
				getFile();
			}
		}

		catch(Exception e){
			// Displaying the exception
			System.out.println("Error " + e);
		}
	}

	/**
	  * Checks if the requested file is present in cache
	  * 
	  * @return true if file in cache else false
	  */
	public boolean checkCache(){
		String currentDir = System.getProperty("user.dir");
		String[] path = (request.get("GET").split("http:/")); 			// Parsing head request to merge with current dir
		path = path[1].split(" ");
		filePath = currentDir + path[0];

		//Checking if the file is present there
		checkFile = new File(filePath);
		if(checkFile.exists() && !checkFile.isDirectory()){return true;}
		else{return false;}
	}

  /**
    * Gets file from origin server
    * Receives and responds with data in bytes
    */
  public void getFile(){
    try{
      // Creating socket
      Socket socket2 = new Socket(request.get("Host").trim(),80);

      // Setting up output stream to request the origin server
      PrintWriter outputOrigin = new PrintWriter(new DataOutputStream(socket2.getOutputStream()));

      // Creating the request
      String serverRequest = "GET " + request.get("GET") + "\r\n" +
      "Host:" + request.get("Host") + "\r\n\r\n";

      // Sending request to Origin Server
      outputOrigin.write(serverRequest);
      outputOrigin.flush();

      // assembling response from server in this string, byte by byte until the end is reached
      String response = "";
      while(!response.contains("\r\n\r\n")){
    	  response += (char) socket2.getInputStream().read();  
    	  
      }
      
      // Error checking if response is okay
      if(!(response.contains("200 OK"))){
    	  outputClient.write										// To client
    	  ("HTTP:/1.1 400 BAD REQUEST".getBytes());
    	  outputClient.flush();
    	  System.out.println("HTTP:/1.1 400 BAD REQUEST");			// To console
			
    	  System.out.println("Connection: close");					// Close the streams
    	  inputClient.close();
    	  outputClient.close();
    	  outputOrigin.close();
    	  return;
      }
      
      // Figure out size of content
      String[] size = response.split("Content-Length: ");
      size = size[1].split("\r\n");
      int fileSize = Integer.parseInt(size[0]);
      
      // Byte array for downloading 
      file = new byte[fileSize];
      
      // All the bytes for the file being stored in array
      socket2.getInputStream().read(file);
      
      // Setting up output stream to respond to client
      outputClient = new DataOutputStream(socket1.getOutputStream());	
      outputClient.write(response.getBytes()); 						// response header
      outputClient.write(file);										// response data
      
      
      // Making all the directories in the file path
      File filePathDirs = new File(filePath);
      filePathDirs.getParentFile().mkdirs();
      
      //Writing to actual file      
      FileOutputStream fileWrite = new FileOutputStream(filePath);
      try{
    	  fileWrite.write(file);
      }   
      finally{
    	  fileWrite.close();
      }

    }

    catch(Exception e){
      System.out.println("Error" + e);
    }
  }


	/**
	 * A simple test driver
	*/
	public static void main(String[] args) {
	    int server_port = 0;
		try {
			// check for command line arguments
	        if (args.length == 1) {
	        	server_port = Integer.parseInt(args[0]);
	        }
	        else {
	        	System.out.println("wrong number of arguments, try again.");
	            System.out.println("usage: java WebProxy port");
	            System.exit(0);
	        }
	
	        WebProxy proxy = new WebProxy(server_port);
	
	        System.out.printf("Proxy server started...\n");
	        proxy.start();
		} 
		catch (Exception e){
			System.out.println("Exception in main: " + e.getMessage());
			e.printStackTrace();
			
		}
	}
}
