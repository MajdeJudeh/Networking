public class DV implements java.io.Serializable {
  private int nodeNumber;
  private int[] dv;
  public DV(int nodeN, int[] dvOfNode){
    nodeNumber = nodeN;
    dv = dvOfNode;
  }

  public int[] getDV(){
    return dv;
  }

  public int getNodeNumber(){
    return nodeNumber;
  }
}
