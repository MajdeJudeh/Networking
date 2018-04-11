import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class DVNode{

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


  public static DV requestMyNodeNumberAndDV(String myIPNumber, String coordinatorIP, Integer coordinatorPort) throws IOException{
    DatagramSocket socket = new DatagramSocket();
    byte[] buf = new byte[1024];
    buf = myIPNumber.getBytes();
    InetAddress coordinatorAddress = InetAddress.getByName(coordinatorIP);
    DatagramPacket myPacket = new DatagramPacket(buf, buf.length, coordinatorAddress, coordinatorPort);

    socket.send(myPacket);

    buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    socket.receive(packet); //Packet of dv

    byte[] dataInPacket = packet.getData();
    // for(int k = 0; k < dataInPacket.length; k++){
    //   System.out.print(dataInPacket[k] + " ");
    // }
    DV receivedDV = null;
    receivedDV = DV.bytes2DV(dataInPacket);

    return receivedDV;
  }
  @SuppressWarnings("unchecked")
  public static HashMap<Integer,String> getNeighborIPTable(Integer myPortNumber) throws IOException{
    DatagramSocket socket = new DatagramSocket (myPortNumber);
    byte[] bytesHashMap = new byte[256];
    DatagramPacket incomingHashMap = new DatagramPacket(bytesHashMap, bytesHashMap.length);
    socket.receive(incomingHashMap);
    bytesHashMap = incomingHashMap.getData();

    HashMap<Integer,String> neighborIPTable = null;
    try{
      neighborIPTable = (HashMap<Integer,String>) convertFromBytes(bytesHashMap);
    } catch(ClassNotFoundException e){
      e.printStackTrace();
      System.exit(1);
    }
    return neighborIPTable;
  }

  private static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
      try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
           ObjectInput in = new ObjectInputStream(bis)) {
          return in.readObject();
      }
  }

  public static MulticastSocket getMultiInSocket(Set<Integer> myNeighborIPSet, Integer myNodeNumber) throws IOException{
    int multiSocPortNumber = 11688;
    MulticastSocket multiInSocket = new MulticastSocket(multiSocPortNumber);
    int portNumberPrefix = 116;
    // String myMultiSocketInIP = "230." + portNumberPrefix + ".0." + myNodeNumber;
    // InetAddress myAddress = InetAddress.getByName(myMultiSocketInIP);
    // multiInSocket.joinGroup(myAddress);
    Iterator<Integer> allNodeNumbers = myNeighborIPSet.iterator();

    Integer currentNodeNumber = null;
    String currentNodeMultiSocketIP = null;
    InetAddress currentNodeAddress = null;

    while(allNodeNumbers.hasNext()){
      currentNodeNumber = allNodeNumbers.next();
      currentNodeMultiSocketIP = "230." + portNumberPrefix + ".0." + currentNodeNumber;
      currentNodeAddress = InetAddress.getByName(currentNodeMultiSocketIP);
      multiInSocket.joinGroup(currentNodeAddress);
    }


    return multiInSocket;
  }

  // public static MulticastSocket getMultiOutSocket(Integer myNodeNumber) throws IOException{
  //   int multiSocPortNumber = 11688;
  //   MulticastSocket multiOutSocket = new MulticastSocket(multiSocPortNumber);
  //   int portNumberPrefix = 116;
  //   // String myMultiSocketOutIP = "230." + portNumberPrefix + ".0." + myNodeNumber;
  //   // InetAddress myAddress = InetAddress.getByName(myMultiSocketOutIP);
  //   // multiOutSocket.joinGroup(myAddress);
  //
  //
  //   return multiOutSocket;
  // }


  public static void main(String[] args) throws IOException{
    if (args.length != 2){
      System.err.println("Usage: java DVNode <coordinator-ip> <coordinator-portNumber>");
      System.exit(1);
    }

    String dvCoordinatorIP = args[0]; //IP of the coordinator to send request to.
    int dvCoordinatorPortNumber = Integer.parseInt(args[1]); //coordinator port number

    String myIPNumber = getIP(); //My IP from Amazon.

    int myPortNumber = 11610; //My port number

    DV myNodeNumberAndDV = requestMyNodeNumberAndDV(myIPNumber, dvCoordinatorIP, dvCoordinatorPortNumber);

    Integer myNodeNumber = myNodeNumberAndDV.getNode_num();

    int[] myDV = myNodeNumberAndDV.getDV();
    System.out.println("My dv contents: " + myNodeNumberAndDV);

    HashMap <Integer,String> myNeighborIPTable = getNeighborIPTable(myPortNumber);
    System.out.println("Node " + myNodeNumberAndDV.getNode_num() + "'s neighborIPTable: " + myNeighborIPTable);

    MulticastSocket multiInSoc = getMultiInSocket(myNeighborIPTable.keySet(), myNodeNumber);
    // MulticastSocket multiOutSoc = getMultiOutSocket(myNodeNumber);

    int portNumberPrefix = 116;
    String myMultiSocketInIP = "230." + portNumberPrefix + ".0." + myNodeNumber;

    DVReceiver dvReceiver = new DVReceiver(multiInSoc, myNeighborIPTable.keySet());
    DVSender dvSender = new DVSender(myMultiSocketInIP, myNodeNumberAndDV);
    dvReceiver.start();
		dvSender.start();
  }
}
