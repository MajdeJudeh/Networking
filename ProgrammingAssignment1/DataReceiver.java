import java.net.*;
import java.io.*;

public class DataReceiver {
  public static void main(String[] args){
    try(
    Socket dataReceiver = new Socket(args[0], Integer.parseInt(args[1]));
    ObjectInputStream in = new ObjectInputStream(dataReceiver.getInputStream());
    ){
      int[] intArray;
      while((intArray = (int[])in.readObject()) != null){
        System.out.println(intArray);
      }


    } catch (IOException e){

    } catch (ClassNotFoundException e){

    }
  }
}
