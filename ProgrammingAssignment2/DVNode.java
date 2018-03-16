import java.io.*;
import java.net.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DVNode{

  private static DV convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
      try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
           ObjectInput in = new ObjectInputStream(bis)) {
          return (DV)in.readObject();
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

  public static Integer requestMyNodeNumber(String myIPNumber, String coordinatorIP, Integer coordinatorPort) throws IOException{
    DatagramSocket socket = new DatagramSocket();
    byte[] buf = new byte[256];
    buf = myIPNumber.getBytes();
    InetAddress coordinatorAddress = InetAddress.getByName(coordinatorIP);
    DatagramPacket myPacket = new DatagramPacket(buf, buf.length, coordinatorAddress, coordinatorPort);
    socket.send(myPacket);

    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    socket.receive(packet);

    String received = new String(packet.getData(), 0, packet.getLength());
    System.out.println("My node number: " + received);
    Integer myNodeNumber = Integer.parseInt(received);
    return myNodeNumber;
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
    Integer myNodeNumber = requestMyNodeNumber(myIPNumber, dvCoordinatorIP, dvCoordinatorPortNumber);


  }
}
