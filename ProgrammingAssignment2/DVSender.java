import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class DVSender extends Thread {

	//MulticastSocket multOutSocket;
	String multiSocIP;
	DatagramSocket socket;
	/**
	 *
	 * @param multOutSocket -- this socket already joins with every neighbor's multicast IPs
	 */
	public DVSender(String multiSocIP) {
		this.multiSocIP = multiSocIP;
		try{socket = new DatagramSocket();} catch (SocketException e) {e.printStackTrace(); System.exit(1);}

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

	public void run() {
		// create DV object
		int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		DV myDv = new DV(1, array); // = new DV(...)

		for (int j=0; j<10; j++) {
			byte[] dvBA = myDv.getBytes();
			send2Neighbor(dvBA);
			try {sleep(1000);} catch (Exception e) {e.printStackTrace();}
		}
	}

}

class DVReceiver extends Thread {
	MulticastSocket multiInSocket;

	public DVReceiver(MulticastSocket multiInSocket) {
		this.multiInSocket = multiInSocket;
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
		DV myDv = null; // = new DV(...)

		for (int j=0; j<10; j++) {
			// ...
			byte[] dvBA = this.receiveFromNeighbor(512);
			DV dv = DV.bytes2DV(dvBA);
			System.out.println(dv);
		}
	}
}
