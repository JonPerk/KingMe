package server;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread{
	private Server server = null;
	private Socket socket = null;
	private int ID = -1;
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;
	private  boolean running = true;
	public volatile int opponentID = -1;
	public volatile String username = null;
   

   public ServerThread(Server _server, Socket _socket){
	   super();
	   server = _server;
	   socket = _socket;
	   ID = socket.getPort();
   }
   
   public void send(String msg){
	   try{
		   streamOut.writeUTF(msg);
		   streamOut.flush();
       }
       catch(IOException ioe){
    	   System.out.println(ID + " ERROR sending: " + ioe.getMessage());
    	   server.remove(ID);
    	   interrupt();
       }
   }
   
   public int getID(){
	   return ID;
   }
   
   public void run(){
	   System.out.println("Server Thread " + ID + " running.");
	   send("\n-----------------------------");
	   send("Welcome to the King Me Server");
	   send("-----------------------------");
	   send("\nPlease enter your name:");
	   while (running){
		   try{
			   server.handle(ID, streamIn.readUTF());
		   }
		   catch(IOException ioe){
			   System.out.println(ID + " ERROR reading: " + ioe.getMessage());
			   server.remove(ID);
			   interrupt();
		   }
		   catch(ArrayIndexOutOfBoundsException e){ continue; }
	   }
   }
   
   public void open() throws IOException{
	   streamIn = new DataInputStream(new 
                        BufferedInputStream(socket.getInputStream()));
	   streamOut = new DataOutputStream(new
                        BufferedOutputStream(socket.getOutputStream()));
   }
   
   public void close() throws IOException{
	   if (socket != null)
		   socket.close();
	   if (streamIn != null)
		   streamIn.close();
	   if (streamOut != null)
		   streamOut.close();
	   running = false;
   }
}