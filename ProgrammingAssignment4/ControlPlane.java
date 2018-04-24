import java.*;
public class ControlPlane extends Thread implements ChangeListener{
  DV neighborDV;
  boolean changed = false;

  public ControlPlane(){

  }
  @Override
  public void changed(DV dv){
    this.neighborDV = dv;
    changed = true;
  }

  public void DVAlgorithim(){
    while(true){
      if(!changed){
        try{sleep(1000);} catch (Exception e) {e.printStackTrace();}
      }else{
        changed = false;

      }
    }//end of while
  }
}
