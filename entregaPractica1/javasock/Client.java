/*
 * Client.java
 * Just sends stdin read data to and receives back some data from the server
 *
 * usage:
 * java Client serverhostname port
 */

import java.io.*;
import java.net.*;

public class Client
{
  public static void main(String[] args) throws IOException
  {
    long startTime;
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
    
    String inputline = "hola";

    /* Get the bytes... */
    buffer = inputline.getBytes();

    /* Send read data to server */
    startTime = System.nanoTime();
    toserver.write(buffer, 0, buffer.length);
    fromserver.read(buffer);
    System.out.println("time,"+((System.nanoTime()-startTime)/2));
        
    fromserver.close();
    toserver.close();
    socketwithserver.close();
  }
}
