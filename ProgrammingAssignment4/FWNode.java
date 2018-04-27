import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class FWNode extends Thread{
  HashMap<Integer, Integer> fwTable;
  int numberOfNeighbors;
  int portNumber;
  // PortUser[] neighborConnections;
  DataPlanePort dpPort;
  HashMap<Integer,String> myNeighborIPTable;
  int fwNodeNumber;
  Set<Integer> neighborNodeNumbers;
  HashMap<Integer,PortUser> portUsersForNeighbors;
  boolean readyToForward;
  final int MY_PORTNUMBER = 11610;
  public FWNode(HashMap<Integer,String> neighborIPTable, int myFWNodeNumber){
    this.portNumber = MY_PORTNUMBER;
    myNeighborIPTable = neighborIPTable;
    numberOfNeighbors = myNeighborIPTable.size();
    neighborNodeNumbers = myNeighborIPTable.keySet();
    // neighborConnections = new PortUser[numberOfNeighbors];
    portUsersForNeighbors = new HashMap<Integer,PortUser>(numberOfNeighbors);
    dpPort = new DataPlanePort(portNumber, numberOfNeighbors);
    fwNodeNumber = myFWNodeNumber;
    readyToForward = false;
  }
  public void setForwardTable(HashMap<Integer, Integer> fwTableFromDVNode){
    fwTable = fwTableFromDVNode;
  }
  public boolean getReadyToForwardStatus(){
    return readyToForward;
  }
  public void forwardData(MessageType msg){
      Integer portUserNeeded = fwTable.get(msg.getDestNode());
      portUsersForNeighbors.get(portUserNeeded).send(msg);
  }

  public void sendData(int destNode, byte[] packet){
    if(destNode == fwNodeNumber){
      System.out.println("You are trying to send a packet to yourself");
    }else{
      MessageType msg = new MessageType(fwNodeNumber, destNode, packet);
      forwardData(msg);
    }
  }

  public void signalReadyToForward(){
    readyToForward = true;
  }
  public void receiveData(){
    ConcurrentLinkedQueue<MessageType> messagesReceived = dpPort.getQue();
      if(messagesReceived.peek() != null){
            MessageType receivedMessage = messagesReceived.poll();
            if(receivedMessage.getDestNode() == fwNodeNumber){
              System.out.println("Message has been received from node Number: " + receivedMessage.getSourceNode());
              System.out.println("Message size: " + receivedMessage.getPayload().length);
            }
            else{
              System.out.println("Send to: " + receivedMessage.getDestNode());
              forwardData(receivedMessage);
            }
      }


  }
  public void initialize() throws InterruptedException{
    new Thread(dpPort).start();
    Iterator<Integer> nextNeigborNodeNumber = neighborNodeNumbers.iterator();
    int currentNeigborNodeNumber;
    String currentNeigborIP;
    // int currentPortUser = 0;
    PortUser currentPortUser;
    while(nextNeigborNodeNumber.hasNext()){
      currentNeigborNodeNumber = nextNeigborNodeNumber.next();
      currentNeigborIP = myNeighborIPTable.get(currentNeigborNodeNumber);
      System.out.println("Current neighborIPNumber: " + currentNeigborIP);
      currentPortUser = new PortUser(currentNeigborNodeNumber, currentNeigborIP, portNumber);
      currentPortUser.initialize();
      portUsersForNeighbors.put(currentNeigborNodeNumber, currentPortUser);
      // neighborConnections[currentPortUser] = new PortUser(currentNeigborNodeNumber, currentNeigborIP, portNumber);
      // neighborConnections[currentPortUser].initialize();
      // currentPortUser ++;
      System.out.println("Connected a port user for node neigbor with ID:" + currentNeigborNodeNumber);

    }
    System.out.println("Finished Connecting");
  }

  public void run(){
    receiveData();

  }


  //
  // public static void main(String args[]) throws IOException, InterruptedException{
  //   if (args.length != 2){
  //     System.err.println("Usage: java FWNode <coordinator-ip> <coordinator-portNumber>");
  //     System.exit(1);
  //   }
  //   DVNode dvNode = new DVNode(args[0], Integer.parseInt(args[1]));
  //   dvNode.initialize();
  //
  //   FWNode fwNode = new FWNode(dvNode.getMyNeighborIPTable(), dvNode.getMyNodeNumber());
  //   System.out.println("Before fwNode initialize");
  //   fwNode.initialize();
  //   Scanner sc = new Scanner(System.in);
  //   System.out.println("Enter the total number of nodes for this network");
  //   int totalNumberOfNodes = sc.nextInt();
  //   int destinationNode;
  //   HashMap<Integer, Integer> fwTableFromDVNode = new HashMap<Integer, Integer>(totalNumberOfNodes - 1);
  //   for(int i = 0; i < totalNumberOfNodes - 1; i++){
  //     System.out.println("Enter the destination node");
  //     destinationNode = sc.nextInt();
  //     System.out.println("Enter the neighbor node to forward to");
  //     int neigborNode = sc.nextInt();
  //     fwTableFromDVNode.put(destinationNode, neigborNode);
  //   }
  //   fwNode.setForwardTable(fwTableFromDVNode);
  //   System.out.println("forwardTable Generated");
  //   int destNode;
  //   byte[] pack = new byte[1024];
  //   fwNode.start();
  //   do{
  //     System.out.println("Choose a node to send data to, type -1 to quit");
  //     destNode = sc.nextInt();
  //     if(destNode != -1){
  //       Arrays.fill(pack, (byte)destNode);
  //       fwNode.sendData(destNode, pack);
  //     }
  //   }while(destNode != -1);
  //
  //
  // }
}
