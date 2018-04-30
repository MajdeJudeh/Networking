import java.io.*;
import java.net.*;
import java.util.Scanner;

public class NodeDriver{

  public static void main(String[] args) throws IOException, InterruptedException{
    if (args.length != 2){
      System.err.println("Usage: java ControlPlane <coordinator-ip> <coordinator-portNumber>");
      System.exit(1);
    }

    ControlPlane controlPlane = new ControlPlane(args[0], Integer.parseInt(args[1]));
    controlPlane.initialize();
    FWNode fwNode = new FWNode(controlPlane.getMyNeighborIPTable(), controlPlane.getMyNodeNumber());
    fwNode.initialize();
    controlPlane.initializeForwardTableAndNeigborsLinks(fwNode);
    controlPlane.dvSwap();
    controlPlane.start();
    fwNode.start();
    Scanner input = new Scanner(System.in);
		Thread.sleep(5000);

    System.out.println("Type 1 if you wish to change the weight of one of your links to your neighbors. Type any other number to exit");
    while(input.nextInt() == 1){
      controlPlane.changeLinkWeight();
    }
        System.out.println("Is the node a pumpNode. Type 1 for yes and any other number for no");
        if(input.nextInt() == 1){
          ServerSocket serverSocket = new ServerSocket(11611);
          Socket receivingSocket = serverSocket.accept();
          DataInputStream in = new DataInputStream(receivingSocket.getInputStream());

          byte[] byteArray = new byte[1536];

          try{
            while((in.read(byteArray)) != -1){
              MessageType message = MessageType.bytearray2messagetype(byteArray);
                fwNode.forwardData(message);
            }

          } catch (IOException e){
            System.out.println("IOException");
          }
        }

  }
}
