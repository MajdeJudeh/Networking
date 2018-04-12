
import java.nio.ByteBuffer;

public class MessageType {

	int sourceNode;
	int destNode;
	byte[] payload;
	//Constructor that takes in the source node and destination node and the message.
	public MessageType(int sourceNode, int destNode, byte[] payload) {
		this.sourceNode = sourceNode;
		this.destNode = destNode;
		this.payload = payload;
	}
	//Getter to return source Node.
	public int getSourceNode() {
		return sourceNode;
	}
	//Changing source node.
	public void setSourceNode(int sourceNode) {
		this.sourceNode = sourceNode;
	}
	//Getter for DestNode
	public int getDestNode() {
		return destNode;
	}
	//Changing destNode
	public void setDestNode(int destNode) {
		this.destNode = destNode;
	}
	//Receive's byte array of payLoad.
	public byte[] getPayload() {
		return payload;
	}
	//Changes payLoad.
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	//Converts the message and its Source and Destination to a byteArray to send out.
	public byte[] toBytes() {
		ByteBuffer buf = ByteBuffer.allocate(1536);
		buf.clear();
		buf.putInt(sourceNode);
		buf.putInt(destNode);
		buf.put(payload);
		return buf.array();
	}
	//Takes the message and unmartials it.
	public static MessageType bytearray2messagetype(byte[] arr) throws NumberFormatException {
		ByteBuffer buf = ByteBuffer.allocate(arr.length);
		buf = ByteBuffer.wrap(arr);
		int sNode = buf.getInt();
		int dNode = buf.getInt();
		byte[] payl = new byte[arr.length - 8];
		buf.get(payl);
		MessageType mt = new MessageType(sNode, dNode, payl);
		return mt;
	}
	public String toString() {
		return (sourceNode + " to " + destNode + ":" + payload.length);
	}
}
