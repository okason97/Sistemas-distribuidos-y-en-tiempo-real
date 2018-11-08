/*
 * EchoServer.java
 * Just receives some data and sends back a "message" to a client
 *
 * Usage:
 * java Server port
 */

import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;

public class SafeBigServer
{

  private static int getLength(byte[] arr){
      int count = 0;
      for(byte el : arr)
          if (el != 0)
              ++count;
      return count;
  }

  public static void main(String[] args) throws IOException
  {
    long startTime, endTime;
  	int size = (int)Math.pow(10,6);
    int bufferLength, newBufferLength;

    /* Check the number of command line parameters */
    if ((args.length != 1) || (Integer.valueOf(args[0]) <= 0) )
    {
      System.out.println("1 arguments needed: port");
      System.exit(1);
    }

    /* The server socket */
    ServerSocket serverSocket = null;    
    try
    {
      serverSocket = new ServerSocket(Integer.valueOf(args[0]));
    } 
    catch (Exception e)
    {
      System.out.println("Error on server socket");
      System.exit(1);
    }

    /* The socket to be created on the connection with the client */
    Socket connected_socket = null;

    try /* To wait for a connection with a client */
    {
      connected_socket = serverSocket.accept();
    }
    catch (IOException e)
    {
      System.err.println("Error on Accept");
      System.exit(1);
    }

    /* Streams from/to client */
    DataInputStream fromclient;
    DataOutputStream toclient;

    /* Get the I/O streams from the connected socket */
    fromclient = new DataInputStream(connected_socket.getInputStream());
    toclient   = new DataOutputStream(connected_socket.getOutputStream());

    /* Buffer to use with communications (and its length) */
    byte[] bufferSize = new byte[4];
    byte[] buffer = new byte[size];
    
    /* Recv data from client */

    startTime = System.nanoTime();
    fromclient.read(bufferSize);
    endTime = System.nanoTime();
    System.out.println("read," + (endTime - startTime));

    int stringSize = ByteBuffer.wrap(bufferSize).getInt();

    startTime = System.nanoTime();
    toclient.write(ByteBuffer.allocate(4).putInt(0).array(), 0, 4);
    endTime = System.nanoTime();
    System.out.println("write," + (endTime - startTime));

    startTime = System.nanoTime();
    fromclient.read(buffer);
    endTime = System.nanoTime();
    System.out.println("read," + (endTime - startTime));

    bufferLength = getLength(buffer);
    if (bufferLength < stringSize){
        byte[] newBuffer;
        while (bufferLength < stringSize){

          startTime = System.nanoTime();
    	    toclient.write(ByteBuffer.allocate(4).putInt(bufferLength).array(), 0, 4);
          endTime = System.nanoTime();
          System.out.println("write," + (endTime - startTime));

    	    newBuffer = new byte[size-bufferLength];
      
          startTime = System.nanoTime();
          fromclient.read(newBuffer);
          endTime = System.nanoTime();
          System.out.println("read," + (endTime - startTime));
      
          newBufferLength = getLength(newBuffer);

    	    // append byte arrays
            for (int i=0;i<newBufferLength;i++){
                buffer[bufferLength+i] = newBuffer[i];
            }

            bufferLength = getLength(buffer);
        }
    }
    startTime = System.nanoTime();
    toclient.write(ByteBuffer.allocate(4).putInt(bufferLength).array(), 0, 4);
    endTime = System.nanoTime();
    System.out.println("write," + (endTime - startTime));

    /* Close everything related to the client connection */
    fromclient.close();
    toclient.close();
    connected_socket.close();
    serverSocket.close();
  }
}
