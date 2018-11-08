/*
 * Client.java
 * Just sends stdin read data to and receives back some data from the server
 *
 * usage:
 * java Client serverhostname port
 */

import java.io.*;
import java.net.*;

public class BigClient
{
  public static void main(String[] args) throws IOException
  {
	int size = (int)Math.pow(10,6);

    /* Check the number of command line parameters */
    if ((args.length != 2) || (Integer.valueOf(args[1]) <= 0) )
    {
      System.out.println("2 arguments needed: serverhostname port");
      System.exit(1);
    }

    /* The socket to connect to the echo server */
    Socket socketwithserver = null;

    try /* Connection with the server */
    { 
      socketwithserver = new Socket(args[0], Integer.valueOf(args[1]));
    }
    catch (Exception e)
    {
      System.out.println("ERROR connecting");
      System.exit(1);
    } 

    /* Streams from/to server */
    DataInputStream  fromserver;
    DataOutputStream toserver;

    /* Streams for I/O through the connected socket */
    fromserver = new DataInputStream(socketwithserver.getInputStream());
    toserver   = new DataOutputStream(socketwithserver.getOutputStream());

    /* Buffer to use with communications (and its length) */
    byte[] buffer;
    
    /* Get some input from user */
	char[] array = new char[size];

	for(int i=0;i<size;i++){
		array[i] = 'a';
	}
    String inputline = new String(array);

    /* Get the bytes... */
    buffer = inputline.getBytes();
    System.out.println(buffer.length);

    /* Send read data to server */
    toserver.write(buffer, 0, buffer.length);
    
    /* Recv data back from server (get space) */
    buffer = new byte[size];
    fromserver.read(buffer);

    /* Show data received from server */
    String resp = new String(buffer);
    System.out.println(resp);
    
    fromserver.close();
    toserver.close();
    socketwithserver.close();
  }
}
