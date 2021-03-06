import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class ControlPlane extends Thread implements ChangeListener{
  String dvCoordinatorIP; //
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
  boolean changed;
  DV neighborDV;
  HashMap<Integer,Integer> forwardTable;
  HashMap<Integer,Integer> neighborLinksOnly; //First integer is the neigbor and second integer is the distance to the neigbor
  FWNode fwNode;
  HashMap<Integer,DV> neighbors;
  Scanner input;

  public ControlPlane(String coordinatorIP, Integer coordinatorPort){
    dvCoordinatorIP = coordinatorIP;
    dvCoordinatorPortNumber = coordinatorPort;
    myIPNumber = getIP();
    myPortNumber = MY_PORTNUMBER;
    changed = false;
    forwardTable = new HashMap<Integer,Integer>();
    neighborLinksOnly = new HashMap<Integer,Integer>();
    neighbors = new HashMap<Integer,DV>();
    input = new Scanner(System.in);
  }

  @Override
  public void changed(DV dv){//Changed is called from inside of dvReceiver.
    if(neighborDV != dv){//Changed first checks if the DV passed in is the most up to date neighbor dv.
      this.neighborDV = dv;

      if(neighbors.containsKey(neighborDV.getNode_num())){ //Checks if the dv for this neighbor is already in the map.
        neighbors.replace(neighborDV.getNode_num(), neighborDV); //Replaces it if it is.
      }else{
        neighbors.put(neighborDV.getNode_num(), neighborDV); //Adds it if it isnt.
      }
      changed = true; //Update Changed into true so the DV dvAlgorithim starts
    }
  }

  public void initialize() throws IOException{
    myNodeNumberAndDV = requestMyNodeNumberAndDV();
    myNodeNumber = myNodeNumberAndDV.getNode_num();
    System.out.println(myNodeNumberAndDV);
    myNeighborIPTable = getNeighborIPTable();
  }

  public void changeLinkWeight() {

    System.out.println("Enter your neighbor that you wish to change the weight for");
    int node = input.nextInt();
    System.out.println("Enter the new weight between you and your neighbor");
    int weight = input.nextInt();
    neighborLinksOnly.replace(node, weight); //Replaces the previous weight with new weight.
    changed = true; //Update changed into true so the DV dvAlgorithim starts

  }

  public void initializeForwardTableAndNeigborsLinks(FWNode fwNode){ //Intiailizes the forwardTable and Neighbor links for every node.
    this.fwNode = fwNode;
    int[] myDV = myNodeNumberAndDV.getDV();
    for(int i =0; i < myDV.length; i++){
      if(myDV[i] < Integer.MAX_VALUE && myDV[i] != 0){
        forwardTable.put(i,i);
        neighborLinksOnly.put(i,myDV[i]);
      }
    }
    fwNode.setForwardTable(forwardTable);
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
    try{Thread.sleep(1000);}catch(InterruptedException e){e.printStackTrace();}
    while(true){
      if(!changed){
        try{sleep(100);} catch (Exception e) {e.printStackTrace();}
      }else{
          changed = false; //Sets changed into false so that algorithm stops after this iteration until changed is true again.


          int[] myDVContents = Arrays.copyOf(myNodeNumberAndDV.getDV(), myNodeNumberAndDV.getDV().length);
          //This if statement resets the DV into only the data it directly has.
          for(int i = 0; i < myDVContents.length; i++){
            if(i == myNodeNumber){
              myDVContents[i] = 0;
            }else if (neighborLinksOnly.containsKey(i)){
              myDVContents[i] = neighborLinksOnly.get(i);
            }else {
              myDVContents[i] = Integer.MAX_VALUE;
            }
          }
          DV neighborDVtemp;
          Collection<DV> collectionOfNeighbors = neighbors.values(); //Gets the DVs of all the neighbors.
          Iterator<DV> iteratorOfNeighbors;
          int[] neighborDVContents;
          int neighborNodeNumber;
            for(int i = 0; i < myDVContents.length; i++){
              iteratorOfNeighbors = collectionOfNeighbors.iterator();
              while(iteratorOfNeighbors.hasNext()){//Does the below for each neighbor.
                neighborDVtemp = iteratorOfNeighbors.next(); //Gets the DV of the neighbor.
                neighborDVContents = neighborDVtemp.getDV(); //Gets the contents of the DV for the neighbor.
                neighborNodeNumber = neighborDVtemp.getNode_num(); //Gets the node number of the neighbor.
                    if(neighborDVContents[i] != Integer.MAX_VALUE){ //Checks to make sure the neighbors path is not Integer.MAX_VALUE to avoid overflow.
                      //Checks to see if (the sum of the neighbors path to the destination node and
                      //the current nodes path to its neighbor) is faster than the current nodes path to the destination node.
                      if(myDVContents[i] > (neighborDVContents[i] + neighborLinksOnly.get(neighborNodeNumber))){
                        myDVContents[i] = neighborDVContents[i] + neighborLinksOnly.get(neighborNodeNumber); //If it is, assign new weight to destination node.
                        if(forwardTable.get(i) != null){
                          forwardTable.replace(i, neighborNodeNumber); //Update the forward table.
                        }else{
                          forwardTable.put(i, neighborNodeNumber);
                        }

                      }
                    }
              }
            }//Check against my own Links to see if they are faster???

          if(!(Arrays.equals(myDVContents, myNodeNumberAndDV.getDV()))){ //Checks to see if my DV was changed at all.
            System.out.println("DV updated");
            System.out.println("Original: " + myNodeNumberAndDV);
            myNodeNumberAndDV.setDV(myDVContents);
            System.out.println("Updated: " + myNodeNumberAndDV);
            dvSender.setDV(myNodeNumberAndDV);
            //try{sleep(10000);} catch (Exception e) {e.printStackTrace();}
            dvSender.run(); //If my DV changed, then send my updated DV to my neighbors.
          }
          fwNode.setForwardTable(forwardTable); //Update FWNodes forwarding table.
          if(forwardTable.size() == (myDVContents.length - 1)){
            fwNode.signalReadyToForward();
          }
          System.out.println("Forward Table: " + forwardTable);
      }//end of else statement
    }//end of while
  }

  public void run(){
    dvAlgorithim();
  }

  // public static void main(String[] args) throws IOException{
  //   if (args.length != 2){
  //     System.err.println("Usage: java ControlPlane <coordinator-ip> <coordinator-portNumber>");
  //     System.exit(1);
  //   }
  //
  //   ControlPlane controlPlane = new ControlPlane(args[0], Integer.parseInt(args[1]));
  //   controlPlane.initialize();
  //   controlPlane.dvSwap();
  //   controlPlane.start();
  //   // DVNode dvNode = new DVNode(args[0], Integer.parseInt(args[1]));
  //   // dvNode.initialize();
  //   // //dvNode.getDVFromNeighbors();
  //   // dvNode.dvSwapSetUp();
  //   // dvNode.dvSwap();Fg
  //
  // }
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
