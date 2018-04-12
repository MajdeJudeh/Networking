import java.io.*;
import java.net.*;
import java.util.*;

public class FWNode extends Thread{
  HashMap<Integer, Integer> fwTable;
  int numberOfNeighbors;
  int portNumber;
  PortUser[] neighborConnections;
  DataPlanePort dpPort;
  HashMap<Integer,String> myNeighborIPTable;
  int fwNodeNumber;

  final int MY_PORTNUMBER = 11610;
  public FWNode(HashMap<Integer,String> neighborIPTable, int myFWNodeNumber){
    this.portNumber = MY_PORTNUMBER;
    myNeighborIPTable = neighborIPTable;
    numberOfNeighbors = neighborIPTable.size();
    neighborConnections = new PortUser[numberOfNeighbors];
    dpPort = new DataPlanePort(portNumber, numberOfNeighbors);
    fwNodeNumber = myFWNodeNumber;
  }
  public void getForwardTable(HashMap<Integer, Integer> fwTableFromDVNode){
    fwTable = fwTableFromDVNode;
  }

  public void initialize(){
    new Thread(dpPort).start();

  }
  @Override
  public void run(){

  }

  public void setFWNodeNumber(int nodeNumber){
    fwNodeNumber = nodeNumber;
  }

  public static void main(String args[]) throws IOException{
    if (args.length != 2){
      System.err.println("Usage: java FWNode <coordinator-ip> <coordinator-portNumber>");
      System.exit(1);
    }
    DVNode dvNode = new DVNode(args[0], Integer.parseInt(args[1]));
    dvNode.initialize();

    FWNode fwNode = new FWNode(dvNode.getMyNeighborIPTable(), dvNode.getMyNodeNumber());



  }
}
