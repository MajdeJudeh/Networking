import java.net.*;
import java.io.*;

public class DataReceiver {
  public static void main(String[] args){
    try(
    Socket dataReceiver = new Socket(args[0], Integer.parseInt(args[1]));
    ObjectInputStream in = new ObjectInputStream(dataReceiver.getInputStream());
    ){
      int[] intArray;
      String data;
      long time = System.currentTimeMillis(), previous = 0;
      long previousTime = 0;
      long averageTime = 0;
      double rate = 0.000;
      long rateAverage = 0;
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
}
