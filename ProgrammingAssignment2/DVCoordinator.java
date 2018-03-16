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
    for (int i = 0; i < numberOfNodes; i++){
      if(i == nodeNumber){
        dv[i] = 0;
      }
      else {
        dv[i] = Integer.MAX_VALUE;
      }
    }
    for (int i = 0; i < numberOfNodes; i++){
      if(i == nodeNumber){
        dv[i] = 0;
      }else {
        for (int j = 0; j < eachLinkAndValue.length; j++){
          String[] nodeNumberAndWeight = eachLinkAndValue[j].split(":");
          if(Integer.parseInt(nodeNumberAndWeight[0]) == i){
            dv[i] = Integer.parseInt(nodeNumberAndWeight[1]);
          }
        }
      }

    }//end of outer for
    // System.out.println("Node number: " + nodeNumber);
    // for(int i = 0; i < dv.length; i++){
    //   System.out.println(dv[i]);
    // }
    DV nodeAndDistance = new DV(nodeNumber, dv);
    // System.out.println("Node number: " + nodeAndDistance.getNodeNumber());
    // int dv1[] = nodeAndDistance.getDV();
    // for(int i = 0; i < dv1.length; i++){
    //   System.out.println(dv1[i]);
    // }
    return nodeAndDistance;
  }
  private static byte[] convertToBytes(Object object) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = new ObjectOutputStream(bos);
      out.writeObject(object);
      byte[] serializedMessage = bos.toByteArray();
      out.close();
      bos.close();
      return serializedMessage;

  }

  private static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
      try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
           ObjectInput in = new ObjectInputStream(bis)) {
          return in.readObject();
      }
  }

  public static HashMap<Integer,String> getallNodesIP(Integer numberOfNodes, int portNumber, ArrayList<String> allLinesOfFile) throws IOException{

    HashMap<Integer,String> nodeIPTable = null;
    DatagramSocket socket = new DatagramSocket(portNumber);

    for(int i = 0; i < numberOfNodes; i++){
      byte[] buf = new byte[512];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      String bufferInfo = new String(packet.getData(), 0, packet.getLength());
      System.out.println("Buffer info: " + bufferInfo);
      DV dv = parseFileInputIntoNodeAndDV(i, allLinesOfFile.get(i), numberOfNodes);

      System.out.println("Node number: " + dv.getNodeNumber());

      int node = dv.getNodeNumber();
      int dv1[] = dv.getDV();
      for(int j = 0; j < dv1.length; j++){
        System.out.print(dv1[j] + " ");
      }


      buf = convertToBytes(dv);
      try{
        dv = (DV) convertFromBytes(buf);
      }
      catch(ClassNotFoundException e){
        e.printStackTrace();
        System.exit(1);
      }
      System.out.println("Node number: " + dv.getNodeNumber());

      dv1 = dv.getDV();
      for(int j = 0; j < dv1.length; j++){
        System.out.print(dv1[i] + " ");
      }

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
