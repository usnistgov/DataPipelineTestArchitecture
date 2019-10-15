package gov.nist.mtconnect;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MTConnectSourceTaskTest {
  @Test
  public void test() {
      MTConnectSourceTask sourceTask = new MTConnectSourceTask();

      try {
          sourceTask.poll();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }
}