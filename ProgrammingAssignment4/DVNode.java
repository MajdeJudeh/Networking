import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class DVNode{
  String dvCoordinatorIP;
  int dvCoordinatorPortNumber;
  String myIPNumber;
  int myPortNumber;
  DV myNodeNumberAndDV;
  int myNodeNumber;
  final int MY_PORTNUMBER = 11610;
  HashMap<Integer,String> myNeighborIPTable;
  MulticastSocket multiInSoc;
  DVReceiver dvReceiver;
  DVSender dvSender;
  ChangeListener cl;


  public DVNode(String coordinatorIP, Integer coordinatorPort, ChangeListener cl){
    dvCoordinatorIP = coordinatorIP;
    dvCoordinatorPortNumber = coordinatorPort;
    myIPNumber = getIP();
    myPortNumber = MY_PORTNUMBER;
    this.cl = cl;
  }

  public void initialize() throws IOException{
    myNodeNumberAndDV = requestMyNodeNumberAndDV();
    myNodeNumber = myNodeNumberAndDV.getNode_num();
    System.out.println(myNodeNumberAndDV);
    myNeighborIPTable = getNeighborIPTable();

  }

  public void dvSwapSetUp() throws IOException{
    multiInSoc = getMultiInSocket(myNeighborIPTable.keySet(), myNodeNumber);

    int portNumberPrefix = 116;
    String myMultiSocketInIP = "230." + portNumberPrefix + ".0." + myNodeNumber;

    dvReceiver = new DVReceiver(multiInSoc, myNeighborIPTable.keySet(), cl);
    dvSender = new DVSender(myMultiSocketInIP, myNodeNumberAndDV);

  }
  public HashMap<Integer,String> getMyNeighborIPTable(){
    return myNeighborIPTable;
  }
  public int getMyNodeNumber(){
    return myNodeNumber;
  }
  public void setDV(DV dv){
    myNodeNumberAndDV = dv;
  }
  public DV getDV(){
    return myNodeNumberAndDV;
  }
  public void dvSwap(){
    dvReceiver.start();
    dvSender.start();
  }
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


  public DV requestMyNodeNumberAndDV() throws IOException{
    DatagramSocket socket = new DatagramSocket();
    byte[] buf = new byte[1024];
    buf = myIPNumber.getBytes();
    InetAddress coordinatorAddress = InetAddress.getByName(dvCoordinatorIP);
    DatagramPacket myPacket = new DatagramPacket(buf, buf.length, coordinatorAddress, dvCoordinatorPortNumber);

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
  public HashMap<Integer,String> getNeighborIPTable() throws IOException{
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

  public MulticastSocket getMultiInSocket(Set<Integer> myNeighborIPSet, Integer myNodeNumber) throws IOException{
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


  public static void main(String[] args) throws IOException{
    if (args.length != 2){
      System.err.println("Usage: java DVNode <coordinator-ip> <coordinator-portNumber>");
      System.exit(1);
    }

    // DVNode dvNode = new DVNode(args[0], Integer.parseInt(args[1]));
    // dvNode.initialize();
    // //dvNode.getDVFromNeighbors();
    // dvNode.dvSwapSetUp();
    // dvNode.dvSwap();

  }
}
