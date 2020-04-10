package gov.nist.mtconnect;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPSourceTask extends SourceTask {
  static final Logger log = LoggerFactory.getLogger(TCPSourceTask.class);
  public static final String IP_ADDRESS = "ip_address";
  public static final String PORT = "port";
  public static final String TOPIC_CONFIG = "topic_config";
  public static final String LINGER_MS = "linger_ms";
  public static final String BATCH_SIZE = "batch_size";
  public static final String SPLIT_SHDR = "split_shdr";
  private Socket socket;
  private InetAddress inetAddress;
  private String ip_address;
  private int port;
  private long batchSize;
  private long lingerMs;
  private String topic;
  private Boolean split_shdr = true;


  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> properties) {
    //1) Unpack properties and cast if necessary
    ip_address = properties.get(IP_ADDRESS);
    port = Integer.parseInt(properties.get(PORT));
    batchSize = Long.parseLong(properties.get(BATCH_SIZE));
    lingerMs = Long.parseLong(properties.get(LINGER_MS));
    topic = properties.get(TOPIC_CONFIG);
    split_shdr = Boolean.parseBoolean(properties.get(SPLIT_SHDR));


    //2) Open a new socket connection to the adapter at the given address and port
    socket = new Socket();
    try {
      inetAddress = InetAddress.getByName(ip_address);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    //2a) the port should be greater or equal to 0, else it will throw an error
    assert (port > 0);

    try {
      //2b) calling the constructor of the SocketAddress class
      SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);

      //2c) connect to the socket with the inetAddress and port number
      socket.connect(socketAddress);
      //System.out.println("Send Buffer size: "+socket.getSendBufferSize());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    List<SourceRecord> records = Collections.emptyList();
    long startAttemptMs;
    ArrayList<String> rawRecords = new ArrayList<String>();
    DataInputStream inputStream = null;
    BufferedReader reader = null;
    BufferedInputStream bufferedInputStream = null;

    try{
    //getInputStream() returns an input stream for the given socket
    //An Input/Output error occurs as this socket is not connected
      bufferedInputStream = new BufferedInputStream(socket.getInputStream());
      inputStream = new DataInputStream(bufferedInputStream);

      String SHDRrecord;

      reader = new BufferedReader(new InputStreamReader(inputStream));
      int batchCount =1 ;
      startAttemptMs = System.currentTimeMillis();
      while ((SHDRrecord = reader.readLine()) != null  && (batchCount < batchSize) && (System.currentTimeMillis() - startAttemptMs < lingerMs)) {
        rawRecords.add(SHDRrecord);
        batchCount++;
      }
      rawRecords.add(SHDRrecord);

      records = StreamToRecords(rawRecords);
      System.out.println(records.size());
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