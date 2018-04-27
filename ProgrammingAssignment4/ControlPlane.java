import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;

public class ControlPlane extends Thread implements ChangeListener{
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
  boolean changedNeighbor;
  DV neighborDV;
  boolean changedSelf;
  HashMap<Integer,Integer> forwardTable;
  HashMap<Integer,Integer> neighborLinksOnly; //First integer is the neigbor and second integer is the distance to the neigbor
  public ControlPlane(String coordinatorIP, Integer coordinatorPort){
    dvCoordinatorIP = coordinatorIP;
    dvCoordinatorPortNumber = coordinatorPort;
    myIPNumber = getIP();
    myPortNumber = MY_PORTNUMBER;
    changedNeighbor = false;
    changedSelf = false;
    forwardTable = new HashMap<Integer,Integer>();
    neighborLinksOnly = new HashMap<Integer,Integer>();
  }

  @Override
  public void changed(DV dv){
    if(neighborDV != dv){
      this.neighborDV = dv;
      changedNeighbor = true;
    }
  }

  public void initialize() throws IOException{
    myNodeNumberAndDV = requestMyNodeNumberAndDV();
    myNodeNumber = myNodeNumberAndDV.getNode_num();
    System.out.println(myNodeNumberAndDV);
    myNeighborIPTable = getNeighborIPTable();
    initializeForwardTableAndNeigborsLinks();
  }

  public void initializeForwardTableAndNeigborsLinks(){
    int[] myDV = myNodeNumberAndDV.getDV();
    for(int i =0; i < myDV.length; i++){
      if(myDV[i] < Integer.MAX_VALUE && myDV[i] != 0){
        forwardTable.put(i,i);
        neighborLinksOnly.put(i,myDV[i]);
      }
    }
  }


  public void dvSwap() throws IOException{
    multiInSoc = getMultiInSocket(myNeighborIPTable.keySet(), myNodeNumber);

    int portNumberPrefix = 116;
    String myMultiSocketInIP = "230." + portNumberPrefix + ".0." + myNodeNumber;

    dvReceiver = new DVReceiver(multiInSoc, myNeighborIPTable.keySet(), this);
    dvSender = new DVSender(myMultiSocketInIP, myNodeNumberAndDV);

    dvReceiver.start();
    dvSender.start();
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

  public void dvAlgorithim(){
    System.out.println("Inside of dvAlgorithm");
    while(true){
      if(!changedNeighbor && !changedSelf){
        try{sleep(100);} catch (Exception e) {e.printStackTrace();}
      }else{
        System.out.println("Not sleeping");
        if(changedNeighbor){
          changedNeighbor = false;
          System.out.println("Changed changed neigbor to true");
          int[] myDVContents = Arrays.copyOf(myNodeNumberAndDV.getDV(), myNodeNumberAndDV.getDV().length);
          int[] neighborDVContents = neighborDV.getDV();
          int neighborNodeNumber = neighborDV.getNode_num();
          for(int i = 0; i < myDVContents.length; i++){
            if(neighborDVContents[i] != Integer.MAX_VALUE){
              // System.out.println("Neighbor dv contents is not infinity.");
              if(myDVContents[i] > (neighborDVContents[i] + myDVContents[neighborNodeNumber])){
                myDVContents[i] = neighborDVContents[i] + myDVContents[neighborNodeNumber];
                System.out.println("My dv contents changed");
                if(forwardTable.get(i) != null){
                  forwardTable.replace(i, neighborNodeNumber);
                }else{
                  forwardTable.put(i, neighborNodeNumber);
                }

              }
            }
          }//Check against my own Links to see if they are faster???
          System.out.println("Finished changing my dv");
          System.out.println(Arrays.toString(myDVContents));
          System.out.println(Arrays.toString(myNodeNumberAndDV.getDV()));
          if(!(Arrays.equals(myDVContents, myNodeNumberAndDV.getDV()))){
            System.out.println("DV updated");
            System.out.println("Original: " + myNodeNumberAndDV);
            myNodeNumberAndDV.setDV(myDVContents);
            System.out.println("Updated: " + myNodeNumberAndDV);
            dvSender.setDV(myNodeNumberAndDV);
            //try{sleep(10000);} catch (Exception e) {e.printStackTrace();}
            dvSender.run();
          }
          System.out.println("Forward Table: " + forwardTable);
          System.out.println("Afterwards");
        }//end of changed ne
      }//end of else statement
    }//end of while
  }

  public void run(){
    dvAlgorithim();
  }

  public static void main(String[] args) throws IOException{
    if (args.length != 2){
      System.err.println("Usage: java ControlPlane <coordinator-ip> <coordinator-portNumber>");
      System.exit(1);
    }

    ControlPlane controlPlane = new ControlPlane(args[0], Integer.parseInt(args[1]));
    controlPlane.initialize();
    controlPlane.dvSwap();
    controlPlane.start();
    // DVNode dvNode = new DVNode(args[0], Integer.parseInt(args[1]));
    // dvNode.initialize();
    // //dvNode.getDVFromNeighbors();
    // dvNode.dvSwapSetUp();
    // dvNode.dvSwap();Fg

  }
}

//
// import java.*;
// public class ControlPlane extends Thread implements ChangeListener{
//   DV neighborDV;
//   boolean changed = false;
//
//   public ControlPlane(){
//
//   }
//   @Override
//   public void changed(DV dv){
//     this.neighborDV = dv;
//     changed = true;
//   }
//

// }
