package gov.nist.mtconnect;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.Math.min;
import static org.apache.kafka.common.utils.Utils.sleep;

public class AdapterSourceTask extends SourceTask{
  static final Logger log = LoggerFactory.getLogger(AdapterSourceTask.class);
  public static final String IP_ADDRESS = "ip_address";
  public static final String PORT = "port";
  public static final String TOPIC_CONFIG = "topic_config";
  public static final String LINGER_MS = "linger_ms";
  public static final String BATCH_SIZE = "batch_size";
  public static final String SPLIT_SHDR = "split_shdr";
  public static final String MAX_CONNECTION_ATTEMPTS = "max_connection_attempts";
  public static final String TIMEOUT = "timeout";
  private Socket socket;

  private String ip_address;
  private int port;
  private long batchSize;
  private long lingerMs;
  private long timeout;
  private int maxConnectionAttempts;
  private String topic;
  private Boolean split_shdr = true;
  private Map<String, String> properties;


  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> properties) throws ConnectException {
    log.info("Starting MTConnect Adapter TCP source task");

    //1) Unpack properties and cast if necessary
    ip_address = properties.get(IP_ADDRESS);
    port = Integer.parseInt(properties.get(PORT));
    batchSize = Long.parseLong(properties.get(BATCH_SIZE));
    lingerMs = Long.parseLong(properties.get(LINGER_MS));
    topic = properties.get(TOPIC_CONFIG);
    split_shdr = Boolean.parseBoolean(properties.get(SPLIT_SHDR));
    maxConnectionAttempts = Integer.parseInt(properties.get(MAX_CONNECTION_ATTEMPTS));
    timeout = Long.parseLong(properties.get(TIMEOUT));

    //2) Start the socket
    Socket socket = createSocket(ip_address, port);
  }

  @Override
  public List<SourceRecord> poll() {
    List<SourceRecord> records = Collections.emptyList();
    long startAttemptMs;
    ArrayList<String> rawRecords = new ArrayList<String>();
    DataInputStream inputStream = null;
    BufferedReader reader = null;
    BufferedInputStream bufferedInputStream = null;

    //Try/Catch the heartbeat first
    try {
      log.info("Sending heartbeat ping");
      socket.getOutputStream().write("* PING\n".getBytes());
    }
    //Looking for SocketException: Broken pipe (write failed)
    //Don't know what other kinds of IOExceptions may crop up here and if they can be fixed by restarting
    catch (IOException e){
      log.info("Heartbeat failed, trying to restart task");
      System.out.println("Heartbeat failed, trying to restart task");
      e.printStackTrace();
      this.restart();
      this.poll();
    }

    // If socket to adapter is alive, stream
    try {
      //getInputStream() returns an input stream for the given socket
      //An Input/Output error occurs as this socket is not connected
      bufferedInputStream = new BufferedInputStream(socket.getInputStream());
      inputStream = new DataInputStream(bufferedInputStream);

      String SHDRrecord;

      reader = new BufferedReader(new InputStreamReader(inputStream));
      int batchCount = 1;
      startAttemptMs = System.currentTimeMillis();
      while ((SHDRrecord = reader.readLine()) != null && (batchCount < batchSize) && (System.currentTimeMillis() - startAttemptMs < lingerMs)) {
        System.out.println(SHDRrecord);
        rawRecords.add(SHDRrecord);
        batchCount++;
      }
      if (SHDRrecord != null) {rawRecords.add(SHDRrecord);}

      records = StreamToRecords(rawRecords);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return records;
  }

  @Override
  public void stop() {
    //Right now, the task opens/closes the socket every instantiation.

    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void restart() {
    System.out.println("Attempting to restart the task.");
    log.info("Attempting to restart the task");

    //Actually we won't change the properties, so we're just starting a new socket
    this.socket = createSocket(ip_address, port);
  }

  private Socket createSocket(String ip_address, int port) {
    //1) Open a new socket connection to the adapter at the given address and port
    SocketAddress socketAddress = null;
    InetAddress inetAddress;
    socket = new Socket();
    //1a) the port should be greater or equal to 0, else it will throw an error
    assert (port > 0);

    //1b) create socket address from ip_address and port
    try {
      inetAddress = InetAddress.getByName(ip_address);
      //2b) calling the constructor of the SocketAddress class
      socketAddress = new InetSocketAddress(inetAddress, port);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    //1c) connect to the socket with the socket address
    int attempts = 1;
    Long startConnectTime = System.currentTimeMillis();
    while (!socket.isConnected()){
      try{
        socket.connect(socketAddress);
        log.info("Sending initial ping");
        socket.getOutputStream().write("* PING\n".getBytes());

      }
      catch(IOException e){
        if (attempts > this.maxConnectionAttempts){
          log.info("Exceeded max attempts to connect");
          throw new ConnectException("Exceeded max attempts to connect");
        }
        else if ((System.currentTimeMillis() - startConnectTime) > this.timeout){
          log.info("Connection attempt timed out");
          throw new ConnectException("Connection attempt timed out");
        }
        else if (attempts <= this.maxConnectionAttempts && (System.currentTimeMillis() - startConnectTime) < this.timeout) {
          // If another attempt is allowed, then increment attempts, sleep for some time
          // then create a new socket and repeat
          // 20APR20 -- create new socket because once the original socket on the localport throws an error, it's closed
          // so even if the target socket ip/port is available still need to create a new local one.
          System.out.println("Failed attempt to connect: " + attempts + " of " + this.maxConnectionAttempts);
          long sleepTime = (long) min(Math.pow(2, attempts)*1000, 60000);
          attempts++;
          System.out.println("Attempting reconnect in " + sleepTime/1000 + "seconds");
          sleep(sleepTime);

          socket = new Socket();
        }
      }
    }
    return socket;
  }


  private List<SourceRecord> StreamToRecords(ArrayList<String> rawRecords){
    ArrayList<SourceRecord> records = new ArrayList<>();
    Map<String, String> sourcePartition = new HashMap<String, String>();
    sourcePartition.put(IP_ADDRESS, this.ip_address);

    //Optionally split SHDR into individual timestamp|key|value records
    //Wanted to excise the split shdr code into its own method, but I need the key too
    if (split_shdr){
      String[] rawSHDR;
      String timestamp;
      String key;
      String value;
      String processedSHDR;
      for (int i = 0; i < rawRecords.size(); i++) {
        rawSHDR = rawRecords.get(i).split("\\|");
        timestamp = rawSHDR[0];
        for (int j = 1; j < rawSHDR.length - 1; j = j + 2) {
          key = rawSHDR[j];
          value = rawSHDR[j + 1];
          processedSHDR = timestamp + "|" + key + "|" + value;
          records.add(new SourceRecord(sourcePartition, null, this.topic,null,null, key, null, processedSHDR));
        }
      }
    }
    else {
      for (int i = 0; i < rawRecords.size(); i++) {
        records.add(new SourceRecord(sourcePartition, null, this.topic,null,null, null, null, rawRecords.get(i)));
      }

    }
    return records;
  }
  private ArrayList<String> splitSHDR(ArrayList<String> rawRecords) {
    ArrayList<String> processedRecords = new ArrayList<String>();
    String[] rawSHDR;
    String timestamp;
    String key;
    String value;
    for (int i = 0; i < rawRecords.size(); i++) {
      rawSHDR = rawRecords.get(i).split("\\|");
      timestamp = rawSHDR[0];
      for (int j = 1; j < rawSHDR.length - 1; j = j + 2) {
        key = rawSHDR[j];
        value = rawSHDR[j + 1];
        processedRecords.add(timestamp + "|" + key + "|" + value);
      }
    }
    return processedRecords;
  }

}