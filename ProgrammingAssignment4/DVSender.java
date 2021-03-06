import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Set;

class DVSender extends Thread {

	//MulticastSocket multOutSocket;
	String multiSocIP;
	DatagramSocket socket;
	DV dvToSend;
	/**
	 *
	 * @param multOutSocket -- this socket already joins with every neighbor's multicast IPs
	 */
	public DVSender(String multiSocIP, DV dvToSend) {
		this.multiSocIP = multiSocIP;
		try{socket = new DatagramSocket();} catch (SocketException e) {e.printStackTrace(); System.exit(1);}
		this.dvToSend = dvToSend;
	}

	public void send2Neighbor(byte[] payload) {
		InetAddress address = null;
		try{
			address = InetAddress.getByName(multiSocIP);
		} catch (UnknownHostException e){
			e.printStackTrace();
			System.exit(1);
		}
		DatagramPacket payLoadPacket = new DatagramPacket(payload, payload.length, address, 11688);
		try{
			socket.send(payLoadPacket);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void setDV(DV newDV){
		this.dvToSend = newDV;
	}

	public void run() {
		// create DV object
		// int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		// DV myDv = new DV(1, array); // = new DV(...)
			for(int i = 0; i < 3; i++){
				byte[] dvBA = dvToSend.getBytes();
				send2Neighbor(dvBA);
			}

	}

}

class DVReceiver extends Thread {
	MulticastSocket multiInSocket;
	Set<Integer> nodeNumbers;
	ChangeListener cl;
	public DVReceiver(MulticastSocket multiInSocket, Set<Integer> nodeNumbers, ChangeListener cl) { //Takes in a changeListener that is controlPLane.
		this.multiInSocket = multiInSocket;
		this.nodeNumbers = nodeNumbers;
		this.cl = cl;
	}

	public byte[] receiveFromNeighbor(int packSize) {
		DatagramPacket inPack = null;
		byte[] buffer = new byte[packSize];
		try {
			inPack = new DatagramPacket(buffer, buffer.length);
			multiInSocket.receive(inPack);
			// ...

		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		byte[] data = inPack.getData();
		return data;
	}

	public void run() {
		// create DV object
		DV neighborDV = null; // = new DV(...)
		byte[] dvBA;
		while(true){
			dvBA = this.receiveFromNeighbor(512); //After receiving data
			neighborDV = DV.bytes2DV(dvBA); // Convert the contents to a DV.

			if(nodeNumbers.contains(neighborDV.getNode_num())){
				cl.changed(neighborDV); //Send the DV to ControlPlane.
			}
		}
		// for (int j=0; j<10; j++) {
		// 	// ...
		// 	byte[] dvBA = this.receiveFromNeighbor(512);
		// 	DV dv = DV.bytes2DV(dvBA);
		// 	System.out.println(dv);
		// }
	}
}
