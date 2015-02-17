package server;

import java.net.*;
import java.util.HashMap;
import java.io.*;

public class Server implements Runnable
{  private ServerThread clients[] = new ServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private int openClients = 0;
   private HashMap names = new HashMap();

   public Server(int port){
	   try{
		   System.out.println("Binding to port " + port + ", please wait  ...");
		   server = new ServerSocket(port);  
		   System.out.println("Server started: " + server);
		   start(); 
	   }
	   catch(IOException ioe){
		   System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); 
	   }
   }
   
   public void run(){
	   while (thread != null){
		   try{  
			   System.out.println("Waiting for a client ..."); 
			   addThread(server.accept());
		   }
		   catch(IOException ioe){
			   System.out.println("Server accept error: " + ioe); 
			   stop(); 
		   }
	   }
   }
   
   public void start(){
	   if (thread == null){  
		   thread = new Thread(this); 
		   thread.start();
	   }
   }
   
   public void stop(){
	   if (thread != null){
		   thread.interrupt(); 
		   thread = null;
	   } 
   }
   private int findClient(int ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }
   public synchronized void handle(int ID, String input){
	   ServerThread client = clients[findClient(ID)];
	   int opponent = client.opponentID;
	   if (input.equals(".bye")){
		   client.send(".bye");
		   if(opponent != -1){
			   clients[findClient(opponent)].send("Game Ended");
			   clients[findClient(opponent)].opponentID = -1;
		   }
		   remove(ID); 
       }
	   else if(client.username == null){
		   setName(input, client);
	   }
      else{
    	  if((opponent) != -1){
			  respondOpponent(input, client);
    	  }
    	  else{
    		  findOpponent(input, client);
    	  }
      }
   }
   
   private void setName(String input, ServerThread client){
	   String name = input.trim();
	   if(!name.equals("") && !names.containsKey(name)){
		   client.username = name;
		   names.put(name, client);
		   client.send("Name set to " + name);
		   openClients++;
		   showOpponents(client);
	   }
	   else if(names.containsKey(name)){
		   client.send("Name is already taken");
	   }
	   else{
		   name = "Guest" + client.getID();
		   client.username = name;
		   names.put(name, client);
		   client.send("Name set to " + name);
		   openClients++;
		   showOpponents(client);
	   }
   }
   
   private void showOpponents(ServerThread client){
	   if(openClients > 1){
		   int thisClient = findClient(client.getID());
		   client.send("Available opponents:");
		   for (int i = 0; i < clientCount; i++){
			   ServerThread temp = clients[i];
			   if(i != thisClient && temp.opponentID == -1 && temp.username != null)
				   client.send(temp.username);
		   }
		   client.send("Enter an opponent's name to challenge or hit enter to refresh");
	   }
	   else{
		   client.send("Waiting for opponents. Hit enter to refresh");
	   }
   }
   
   private void findOpponent(String input, ServerThread client){
	   if(openClients > 1){
		   int ID = client.getID();
		   if(!input.equals(client.username) && names.containsKey(input)){
			   ServerThread op = (ServerThread) names.get(input);
			   if(op.opponentID == -1){
				   op.send(client.username + " is challenging you to a game. Do you want to play? Y/N");
				   //client.opponentID = temp;
				   op.opponentID = ID;
				   client.opponentID = -2;
				   openClients = openClients - 2;
			   }
			   else
				   client.send(op.username + " is currently playing. Choose another opponent");
		   }
		   else{
			   showOpponents(client);
		   }
	  }
	  else{
		  client.send("Waiting for opponents. Hit enter to refresh");
	  }
   }
   
   private void respondOpponent(String input, ServerThread client){
	   ServerThread op = clients[findClient(client.opponentID)];
		  if(input.toUpperCase().equals("Y") && op.opponentID == -2){
			  op.opponentID = client.getID();
			  op.send("Game Accepted");
			  client.send("Game Accepted");
		  }
		  else if(input.toUpperCase().equals("N") && op.opponentID == -2){
			  client.opponentID = -1;
			  op.send("Game Rejected");
			  client.send("Game Rejected");
			  openClients = openClients + 2;
		  }
		  else if (op.opponentID == -2){
			  client.send("Please respond 'Y' or 'N'");
		  }
		  else{
			  op.send(client.username + ": " + input);
		  }
   }
   
   public synchronized void remove(int ID){
	   int pos = findClient(ID);
	   if (pos >= 0)
	   {
		   ServerThread toTerminate = clients[pos];
		   System.out.println("Removing client thread " + ID + " at " + pos);
		   names.remove(toTerminate.username);
		   if (pos < clientCount-1)
			   for (int i = pos+1; i < clientCount; i++)
				   clients[i-1] = clients[i];
		   clientCount--;
		   try{
			   toTerminate.close();
		   }
		   catch(IOException ioe){
			   System.out.println("Error closing thread: " + ioe);
		   }
		   toTerminate.interrupt();
	   }
   }
   
   private void addThread(Socket socket){
	   if (clientCount < clients.length)
	   {
		   System.out.println("Client accepted: " + socket);
		   clients[clientCount] = new ServerThread(this, socket);
		   try
		   {
			   clients[clientCount].open(); 
			   clients[clientCount].start();  
			   clientCount++;
		   }
		   catch(IOException ioe){
			   System.out.println("Error opening thread: " + ioe); 
		   }
		}
	   	else
	   		System.out.println("Client refused: maximum " + clients.length + " reached.");
   }
   
   public static void main(String args[]) { Server server = null;
   if (args.length != 1)
       System.out.println("Usage: java Server port");
    else
       server = new Server(Integer.parseInt(args[0])); }
}
