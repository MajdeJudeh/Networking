import java.net.URL;
import java.io.*;
import java.net.MalformedURLException;
public class IPchecker {
  public static String checkIP( ) {
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
    }
    return ip;
  }
}
