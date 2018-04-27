import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.ArrayList;

public class DataSender {
  int interval;
  int portNumber;
  ArrayList<Integer> rates;

  String ipAddress;
  Socket dataSender;
  DataOutputStream out;
  public DataSender(int interval, int portNumber, String ipAddress, ArrayList<Integer> rates){
    this.interval = interval;
    this.portNumber = portNumber;
    this.rates = rates;
    this.ipAddress = ipAddress;
    try{
      dataSender = new Socket(ipAddress, portNumber);
      out = new DataOutputStream(dataSender.getOutputStream());
    } catch(IOException e){
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void sendData(MessageType message) throws Exception{

      while (true){
        for (int i = 0; i < rates.size(); i++){//For each rate in rates, rates[] is an array that contains all the rates.
            for(int j = 0; j < interval * rates.get(i); j++){//If each interval simulates
              Thread.sleep(1000/rates.get(i));//Sleep for 1 second divided by the current rate. So if the current rate is 3 per second, this thread will sleep for 1 third of a second.
              out.write(message.toBytes());//Write an object to the DataOutputStream which sends
            }
        }
      }

  }

  //Returns an array of 16 random integers.
  // public int[] getArray(){
  //   int[] intArray = new int[16];
  //   Random randomNumber = new Random();
  //   for (int i = 0; i < 16; i++){
  //     intArray[i] = randomNumber.nextInt((100*i)+100);
  //   }
  //
  //   return intArray;
  // }
  //Takes in all arguments passed in from command line and returns only the rates.
  // public int[] getRates(String[] argsv){
  //   int[] rates = new int[argsv.length - 2];
  //   for(int i = 0; i < rates.length; i++){
  //     rates[i] = Integer.parseInt(argsv[i+2]);
  //   }
  //
  //   return rates;
  // }

  // public static void main(String[] args){
  //   if (args.length < 3){
  //     System.err.println("Usage: java DataSender <port number> <interval> <d1> ... <dk>");
  //     System.exit(1);
  //   }
  //   DataSender sendData = new DataSender(args);
  //   sendData.sendData();
  //
  //
  // }//end of main
}//end of DataSender
