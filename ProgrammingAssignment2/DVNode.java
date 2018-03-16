import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DVNode{

  private static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
      try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
           ObjectInput in = new ObjectInputStream(bis)) {
          return in.readObject();
      }
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

  public static DV requestMyNodeNumberAndDV(String myIPNumber, String coordinatorIP, Integer coordinatorPort) throws IOException{
    DatagramSocket socket = new DatagramSocket();
    byte[] buf = new byte[512];
    buf = myIPNumber.getBytes();
    InetAddress coordinatorAddress = InetAddress.getByName(coordinatorIP);
    DatagramPacket myPacket = new DatagramPacket(buf, buf.length, coordinatorAddress, coordinatorPort);
    socket.send(myPacket);

    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    socket.receive(packet);
    byte[] dataInPacket = packet.getData();
    System.out.println(dataInPacket);
    DV receivedDV = null;
    try{
      receivedDV = (DV)convertFromBytes(dataInPacket);
    } catch (ClassNotFoundException e){
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("My node number: " + receivedDV);
    return receivedDV;
  }

  public static void main(String[] args) throws IOException{
    if (args.length != 3){
      System.err.println("Usage: java DVNode <coordinator-ip> <coordinator-portNumber> <DVNode-PortNumber>");
      System.exit(1);
    }
    String dvCoordinatorIP = args[0];
    int dvCoordinatorPortNumber = Integer.parseInt(args[1]);
    String myIPNumber = getIP();
    int myPortNumber = Integer.parseInt(args[2]);
    DV myNodeNumberAndDV = requestMyNodeNumberAndDV(myIPNumber, dvCoordinatorIP, dvCoordinatorPortNumber);
    Integer myNodeNumber = myNodeNumberAndDV.getNodeNumber();
    int[] myDV = myNodeNumberAndDV.getDV();

    System.out.println("My node number: " + myNodeNumber);
    System.out.println("DV contents: " + myDV);
  }
}
