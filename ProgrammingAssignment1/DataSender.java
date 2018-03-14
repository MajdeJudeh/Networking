import java.net.*;
import java.io.*;

public class DataSender {
  public static void main(String[] args){
    if (args.length < 3){
      System.err.println("Usage: java DataSender <port number> <interval> <d1> ... <dk>");
      System.exit(1);
    }
    int interval = Integer.parseInt(args[1]);
    int[] intArray = getArray();
    int portNumber = Integer.parseInt(args[0]);
    int[] rates = getRates(args);
    // try{
    //
    //   for (int i = 0; i < rates.length; i++){
    //       for(int j = 0; j < interval * rates[i]; j++){
    //         Thread.sleep(1000/rates[i]);
    //         System.out.println(j);
    //       }
    //   }
    // }
    // catch (InterruptedException e){
    //   e.printStackTrace();
    // }
    try (
      ServerSocket serverSocket = new ServerSocket(portNumber);
      Socket clientSocket = serverSocket.accept();
      ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

    )
    {
      while (true){
        for (int i = 0; i < rates.length; i++){//For each rate in rates, rates[] is an array that contains all the rates.
            for(int j = 0; j < interval * rates[i]; j++){//If each interval simulates
              Thread.sleep(1000/rates[i]);
              out.writeObject(intArray);
            }
        }
      }

      // out.close();
      // clientSocket.close();
      // serverSocket.close();
    } catch (IOException e){
      System.err.println("Couldn't get I/O for the connection to server."); System.exit(1);
    } catch (InterruptedException e){
      e.printStackTrace();
    }

  }//end of main

  public static int[] getArray(){
    int[] intArray = new int[16];
    for (int i = 0; i < 16; i++){
      intArray[i] = i * 100;
    }

    return intArray;
  }

  public static int[] getRates(String[] argsv){
    int[] rates = new int[argsv.length - 2];
    for(int i = 0; i < rates.length; i++){
      rates[i] = Integer.parseInt(argsv[i+2]);
    }

    return rates;
  }
}//end of DataSender
