/*
 * Client.java
 * Just sends stdin read data to and receives back some data from the server
 *
 * usage:
 * java Client serverhostname port
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class SafeBigClient
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
    int sliceSize;

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
    byte[] buffer = new byte[size];
    byte[] confirmedBuffer = new byte[4];

    for(int i=0;i<size;i++){
    buffer[i] = (byte)'a';
    }

    /* Send read data to server */
    startTime = System.nanoTime();
    toserver.write(ByteBuffer.allocate(4).putInt(size).array(), 0, 4);
    endTime = System.nanoTime();
    System.out.println("write," + (endTime - startTime));

    startTime = System.nanoTime();
    fromserver.read(confirmedBuffer);
    endTime = System.nanoTime();
    System.out.println("read," + (endTime - startTime));

    int confirmedBufferInt = ByteBuffer.wrap(confirmedBuffer).getInt();
    while (confirmedBufferInt < size){
        byte[] slice = Arrays.copyOfRange(buffer, confirmedBufferInt, size);
        sliceSize = getLength(slice);

        startTime = System.nanoTime();
        toserver.write(slice, 0, sliceSize);
        endTime = System.nanoTime();
        System.out.println("write," + (endTime - startTime));

        startTime = System.nanoTime();
        fromserver.read(confirmedBuffer);
        endTime = System.nanoTime();
        System.out.println("read," + (endTime - startTime));

        confirmedBufferInt = ByteBuffer.wrap(confirmedBuffer).getInt();
    }
    /* Recv data back from server (get space) */
    buffer = new byte[size];

    fromserver.close();
    toserver.close();
    socketwithserver.close();
  }
}
