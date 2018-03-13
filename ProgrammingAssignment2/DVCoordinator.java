import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
public class DVCoordinator {

  public static void main(String[] args){
    if (args.length != 2){
      System.err.println("Usage: java DVCoordinator <port number> <file name>");
      System.exit(1);
    }
    String dvIP; //IP address of the DVCoordinator
    int portNum = Integer.parseInt(args[0]); //PortNumber
    File mapOfNeighbors = new File(args[1]);  //File of the adjacency list

    dvIP = getIP();
    System.out.println(dvIP);

  }//end of main


  public static String getIP(){
    URL aws;
    String ip = null;
    try {
      aws = new URL("http://checkip.amazonaws.com/");
      BufferedReader in = new BufferedReader(
      new InputStreamReader(aws.openStream()));
      ip = in.readLine();
      in.close();
    } catch (IOException ioe) {
        System.err.println("Failed in checkip of AWS.");
    }//end of catch
    return ip;
}//end of getIP
  public static String readFile(File fileSupplied){
    String fileInput = null;
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new FileReader(fileSupplied));
      String text = null;
      while ((text = reader.readLine()) != null){
        fileInput += text;
      }
    }catch (FileNotFoundException e){
        e.printStackTrace();
        System.out.println("The file: " + fileSupplied + " does not exist.");

    }catch (IOException e){
      e.printStackTrace();
      System.out.println("IOException thrown");
    }

    return fileInput;
  }//end of readFile

}//end of DVCoordinator class
