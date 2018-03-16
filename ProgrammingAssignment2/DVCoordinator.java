import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
public class DVCoordinator {

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
  public static ArrayList<String> parseFileIntoLines(File fileSupplied){
    ArrayList<String> fileInput = new ArrayList<String>();
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new FileReader(fileSupplied));
      String text = null;
      while ((text = reader.readLine()) != null){
        fileInput.add(text);
      }
    }catch (FileNotFoundException e){
        e.printStackTrace();
        System.out.println("The file: " + fileSupplied + " does not exist.");

    }catch (IOException e){
      e.printStackTrace();
      System.out.println("IOException thrown");
    }finally{
      try {
          if (reader != null) {
              reader.close();
          }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }//end of try/catch/finally

    return fileInput;
  }//end of readFile

  public static DV parseFileInputIntoNodeAndDV(int nodeNumber, String linkValues, int numberOfNodes)
  {
    int[] dv = new int[numberOfNodes];
    String[] eachLinkAndValue = linkValues.split(" ");
    System.out.println(t);
    for (int i = 0; i < numberOfNodes; i++){
      if(i = nodeNumber){
        dv[i] = 0;
      }else if ()
    }
    //left off here

    DV NodeAndDistance = null;
    return NodeAndDistance;
  }
  private static byte[] convertToBytes(DV dv) throws IOException {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutput out = new ObjectOutputStream(bos)) {
          out.writeObject(dv);
          return bos.toByteArray();
      }
  }

  public static HashMap<Integer,String> getallNodesIP(Integer numberOfNodes, int portNumber, ArrayList<String> allLinesOfFile) throws IOException{

    HashMap<Integer,String> nodeIPTable = null;
    DatagramSocket socket = new DatagramSocket(portNumber);

    for(int i = 0; i < numberOfNodes; i++){
      byte[] buf = new byte[256];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      String bufferInfo = new String(packet.getData(), 0, packet.getLength());
      System.out.println("Buffer info: " + bufferInfo);
      DV dv = parseFileInputIntoNodeAndDV(i, allLinesOfFile.get(i), numberOfNodes);
      buf = convertToBytes(dv);

      InetAddress address = packet.getAddress();
      System.out.println("Address of node " + i + ": " + address);
      int port = packet.getPort();
      packet = new DatagramPacket(buf, buf.length, address, port);
      socket.send(packet);
    }
    socket.close();
    return nodeIPTable;
  }





  public static void main(String[] args){
    if (args.length != 2){
      System.err.println("Usage: java DVCoordinator <port number> <file name>");
      System.exit(1);
    }
    String dvCoordinatorIP = getIP(); //IP address of the DVCoordinator
    System.out.println("dvCoordinatorIP: " + dvCoordinatorIP);
    int portNumber = Integer.parseInt(args[0]); //PortNumber
    File mapOfNeighbors = new File(args[1]);  //File of the adjacency list

    ArrayList<String> allLinesOfFile = parseFileIntoLines(mapOfNeighbors);
    int numberOfNodes = allLinesOfFile.size();
    try{
      HashMap<Integer,String> allNodesIP = getallNodesIP(numberOfNodes, portNumber, allLinesOfFile);
    } catch (IOException e){
      System.err.println("There is an error with your input");
      System.exit(1);
    }


                  //   for(int i = 0; i < ello.size(); i++){
                  //     System.out.println(ello.get(i));
                  //   }
  }//end of main

}//end of DVCoordinator class
