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

public class SaferBigServer
{

  private static int getLength(byte[] arr, int off){
      int count = 0;
      for (int i = off; i < arr.length; i++) {
        if (arr[i] != 0)
          ++count;  
      }
      return count;
  }

  public static void main(String[] args) throws IOException
  {
    int dataSize = 65536;
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
    byte[] buffer = new byte[size+4];    

    /* Recv data from client */

    fromclient.read(bufferSize);

    int stringSize = ByteBuffer.wrap(bufferSize).getInt();

    toclient.write(ByteBuffer.allocate(4).putInt(0).array(), 0, 4);

    int n = fromclient.read(buffer) - 4;
    bufferLength = 0;
    System.out.println("n: " + n);
    System.out.println("bufferLength: " + bufferLength);
    System.out.println("safety: " + ByteBuffer.wrap(buffer,0,4).getInt());
    if(bufferLength == ByteBuffer.wrap(buffer,0,4).getInt()){
      bufferLength = n;
      byte[] newBuffer;
      while (bufferLength < stringSize){

        toclient.write(ByteBuffer.allocate(4).putInt(bufferLength).array(), 0, 4);

        newBuffer = new byte[size-bufferLength+4];
        
        n = fromclient.read(newBuffer) - 4;
        System.out.println("n: " + n);
        System.out.println("bufferLength: " + bufferLength);
        System.out.println("safety: " + ByteBuffer.wrap(buffer,0,4).getInt());
        if(bufferLength == ByteBuffer.wrap(newBuffer,0,4).getInt()){
          newBufferLength = n;

          // append byte arrays
          for (int i=0;i<newBufferLength;i++){
              buffer[bufferLength+i] = newBuffer[i+4];
          }                
          bufferLength = bufferLength + newBufferLength;
        }else{
          System.err.println("Error on Receive");
          System.exit(1);    
        }
      }
    }
    toclient.write(ByteBuffer.allocate(4).putInt(bufferLength).array(), 0, 4);

    /* Close everything related to the client connection */
    fromclient.close();
    toclient.close();
    connected_socket.close();
    serverSocket.close();
  }
}
