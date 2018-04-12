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

    DV nodeAndDistance = new DV(nodeNumber, dv);

    return nodeAndDistance;
  }



  public static HashMap<Integer,String> getallNodesIP(Integer numberOfNodes, int portNumber, ArrayList<String> allLinesOfFile) throws IOException{

    HashMap<Integer,String> nodeIPTable = new HashMap<Integer,String>();
    DatagramSocket socket = new DatagramSocket(portNumber);
    byte[] buf = new byte[512];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    for(int i = 0; i < numberOfNodes; i++){

      socket.receive(packet); //Receives packet from dvNode.
      String nodeIP = new String(packet.getData(), 0, packet.getLength()); //The DVNode's ip number.
      nodeIPTable.put(i, nodeIP); //puts the node and the IP into one table.
      System.out.println("Node's ip: " + nodeIP);
      DV dv = parseFileInputIntoNodeAndDV(i, allLinesOfFile.get(i), numberOfNodes);

      System.out.println(dv);

      byte[] dvToBytes = dv.getBytes(); //gives an byte Array representation of DV.

      InetAddress address = InetAddress.getByName(nodeIP);
      int port = packet.getPort();
      DatagramPacket newPacket = new DatagramPacket(dvToBytes, dvToBytes.length, address, port);

      socket.send(newPacket);

    }

    socket.close();
    return nodeIPTable;
  }
  private static byte[] convertToBytes(Object object) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = new ObjectOutputStream(bos);
      out.writeObject(object);
      out.flush();
      byte[] serializedMessage = bos.toByteArray();
      out.close();
      bos.close();
      return serializedMessage;

  }
  public static void sendNeighborIPTable(HashMap<Integer,String> nodeIPTable, ArrayList<String> allLinesOfFile, Integer numberOfNodes, Integer nodePortNumber) throws IOException{
    DatagramSocket socketToSendIPTable = new DatagramSocket();
    String nodeIP;
    HashMap<Integer,String> neighborIPTable = null;
    byte[] neighborIPTableToBytes = null;
    DatagramPacket neighborIPTablePacket = null;
    for(int nodeNumber = 0; nodeNumber < numberOfNodes; nodeNumber++){
      nodeIP = nodeIPTable.get(nodeNumber);

      neighborIPTable = generateNeighborIPTable(nodeIPTable, allLinesOfFile.get(nodeNumber), numberOfNodes, nodeNumber);
      neighborIPTableToBytes = convertToBytes(neighborIPTable);
      System.out.println("Node " + nodeNumber + "'s neighborIPTable: " + neighborIPTable);
      InetAddress address = InetAddress.getByName(nodeIP);
      neighborIPTablePacket = new DatagramPacket(neighborIPTableToBytes, neighborIPTableToBytes.length, address, nodePortNumber);

      socketToSendIPTable.send(neighborIPTablePacket);
    }
    socketToSendIPTable.close();
  }

  public static HashMap<Integer,String> generateNeighborIPTable(HashMap<Integer,String> nodeIPTable, String nodesNeighbors, Integer numberOfNodes, Integer nodeNumber) throws IOException{
    String[] eachLinkAndValue = nodesNeighbors.split(" ");
    HashMap<Integer,String> neighborIPTable = new HashMap<Integer,String>();
    for (int i = 0; i < numberOfNodes; i++){
        for (int j = 0; j < eachLinkAndValue.length; j++){
          String[] nodeNumberAndWeight = eachLinkAndValue[j].split(":");
          if(Integer.parseInt(nodeNumberAndWeight[0]) == i){
            neighborIPTable.put(i, nodeIPTable.get(i));
          }
        }
    }//end of outer for


    return neighborIPTable;
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
    HashMap<Integer,String> allNodesIP = null;
    try{
      allNodesIP = getallNodesIP(numberOfNodes, portNumber, allLinesOfFile); //Gives every node their IP number and maps each node number to  their ip number.
    } catch (IOException e){
      System.err.println("There is an error with your input");
      System.exit(1);
    }
    System.out.println("All nodes and their IP's table\n");
    System.out.println(allNodesIP + "\n\n");
    System.out.println("Waiting 2 seconds so that DVNodes are ready to accept data");
    try{Thread.sleep(2000);}catch(InterruptedException e){e.printStackTrace();}
    int nodePortNumber = 11610;
    try{
      sendNeighborIPTable(allNodesIP, allLinesOfFile, numberOfNodes, nodePortNumber);
    } catch(IOException e){
      System.err.println("There is an IOException in sendNeighborIPTable");
      System.exit(1);
    }

  }//end of main

}//end of DVCoordinator class
