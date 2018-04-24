import java.net.*;
import java.io.*;
import java.util.Random;

public class DataSender {
  int interval;
  int portNumber;
  int[] rates;

  public DataSender(String[] args){
    interval = Integer.parseInt(args[1]);
    portNumber = Integer.parseInt(args[0]);
    rates = getRates(args);
  }
  //
  public void sendData(){
    try (
      ServerSocket serverSocket = new ServerSocket(portNumber); //ServerSocket used to start connection.
      Socket clientSocket = serverSocket.accept(); //Once DataReceiver sends a requestion for a connection, ServeSocket spawns a new socket to connect to DataReceiver.
      ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream()); //The new socket's output stream is used to create an ObjectOutputStream object.

    )
    {
      while (true){
        for (int i = 0; i < rates.length; i++){//For each rate in rates, rates[] is an array that contains all the rates.
            for(int j = 0; j < interval * rates[i]; j++){//If each interval simulates
              Thread.sleep(1000/rates[i]);//Sleep for 1 second divided by the current rate. So if the current rate is 3 per second, this thread will sleep for 1 third of a second.
              out.writeObject(getArray());//Write an object to the ObjectOutputStream which sends the output to DataReceiver.
            }
        }
      }
    } catch (IOException e){
      System.err.println("Couldn't get I/O for the connection to server."); System.exit(1);
    } catch (InterruptedException e){
      e.printStackTrace();
    }

  }
  //Returns an array of 16 random integers.
  public int[] getArray(){
    int[] intArray = new int[16];
    Random randomNumber = new Random();
    for (int i = 0; i < 16; i++){
      intArray[i] = randomNumber.nextInt((100*i)+100);
    }

    return intArray;
  }
  //Takes in all arguments passed in from command line and returns only the rates.
  public int[] getRates(String[] argsv){
    int[] rates = new int[argsv.length - 2];
    for(int i = 0; i < rates.length; i++){
      rates[i] = Integer.parseInt(argsv[i+2]);
    }

    return rates;
  }

  public static void main(String[] args){
    if (args.length < 3){
      System.err.println("Usage: java DataSender <port number> <interval> <d1> ... <dk>");
      System.exit(1);
    }
    DataSender sendData = new DataSender(args);
    sendData.sendData();

  }//end of main
}//end of DataSender
