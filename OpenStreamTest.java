import java.io.*;
import java.net.*;
public class OpenStreamTest {
  public static void main(String[] args) {
    if (args.length == 1){
      try {
        URL url = new URL(args[0]);
        BufferedReader dis;
        String inputLine;
        dis = new BufferedReader(new InputStreamReader(url.openStream()));
        while ((inputLine = dis.readLine()) != null) {
        System.out.println(inputLine);
        }
        dis.close();
      } catch (MalformedURLException me) {
          System.out.println("MalformedURLException: " + me);
      } catch (IOException ioe) {
          System.out.println("IOException: " + ioe);
      }
    }
    else {
      System.out.println("Usage: java OpenStreamTest <url>");
    }
  }
}
