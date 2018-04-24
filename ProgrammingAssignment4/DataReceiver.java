import java.net.*;
import java.io.*;

public class DataReceiver {
  int[] intArray;
  String data;
  long time;
  long previous;
  long previousTime;
  long averageTime;
  double rate;
  long rateAverage;
  Socket dataReceiver;
  ObjectInputStream in;
  public DataReceiver(String ip, int portNumber){
    try{
      dataReceiver = new Socket(ip, portNumber);
      in = new ObjectInputStream(dataReceiver.getInputStream());
    } catch(IOException e){
      e.printStackTrace();
      System.exit(1);
    }
  }
  public void initialize(){
    previous = 0;
    previousTime = 0;
    averageTime = 0;
    rate = 0.0;
    rateAverage = 0;
    time = 0;
  }
  public void receiveData(){
    try{
      while((intArray = (int[])in.readObject()) != null){

        previous = System.currentTimeMillis();
        data = "Data: ";
        for(int i =0; i < intArray.length; i++){
          data += intArray[i] + " ";
        }

        System.out.println(data);

        time = previous - time;
        previousTime = time;
        averageTime = (previousTime + time)/2;
        rate = 1000.0/(double)averageTime;
        rateAverage = Math.round(rate);
        System.out.println("Number of packages being sent per second: " + rateAverage);

        time = System.currentTimeMillis();
      }

    } catch (IOException e){
      System.out.println("IOException");
    } catch (ClassNotFoundException e){
      System.out.println("ClassNotFoundException");
    }

  }
  public static void main(String[] args){
    if (args.length != 2){
      System.out.println("Usage: java DataReceiver <IP address> <PortNumber>");
      System.exit(0);
    }
    DataReceiver receiver = new DataReceiver(args[0], Integer.parseInt(args[1]));
    receiver.initialize();
    receiver.receiveData();
  }
}
