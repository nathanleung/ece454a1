import org.apache.thrift.TException;
import ece454750s15a1.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.util.Date;
import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Random;

public class FEPasswordHandler implements A1Password.Iface {
  private int before;
  private int after;
  public ConcurrentMap<String, List<String>> liveBEInfo = new ConcurrentHashMap();
  public FEPasswordHandler() {
    before = 0;
    after = 0;
  }
  public int getBefore(){
    return before;
  }
  public int getAfter(){
    return after;
  }
  public void syncBEInfo(ConcurrentMap<String, List<String>> mgmtBEInfo){
    liveBEInfo = mgmtBEInfo;
  }
  public String getRandEntry(ConcurrentMap<String, List<String>> beInfo){
    Random rand = new Random();
    int randomNum = rand.nextInt(beInfo.size());
    int i = 0;
    for (ConcurrentMap.Entry<String, List<String>> e: beInfo.entrySet()){
      if(randomNum == i){
        return e.getKey();
      }else{
        i++;
      }
    }
    return "";
  }
  public void PrintBEEntries(){
    System.out.println("Entries in BE are: ");
    for (ConcurrentMap.Entry<String, List<String>> e: liveBEInfo.entrySet()){
      System.out.println(e.getKey());
    }
  }

  public String hashPassword (String password, short logRounds) throws
  ServiceUnavailableException, org.apache.thrift.TException {
    before++;
    String hashed = getHash(password, logRounds);
    after++;
    return hashed;
  }

  public String getHash(String password, short logRounds)throws
  ServiceUnavailableException, org.apache.thrift.TException{
    String hashed = "";
    System.out.println("Checking nodes");
    PrintBEEntries();
    String randElement = getRandEntry(liveBEInfo);
    String host = randElement.split("_")[0];
    String pport = liveBEInfo.get(randElement).get(0);
    TTransport transport;
    try{
      transport = new TSocket(host, Integer.parseInt(pport));
      transport.open();
      TProtocol protocol = new  TBinaryProtocol(transport);
      A1Password.Client clientPwd = new A1Password.Client(protocol);
      hashed = clientPwd.hashPassword(password, logRounds);
      transport.close();
      return hashed;
    }catch(TTransportException e){
       liveBEInfo.remove(randElement);
       System.out.println("removing node");
       PrintBEEntries();
       if(liveBEInfo.size() > 0){
          return getHash(password, logRounds);
       }else{
          System.out.println("All BE Server down");
          //somehow tell client
       }
    }
    return hashed;
  }

  public boolean checkPassword(String password, String hash) throws
  org.apache.thrift.TException {
    before++;
    boolean isPwdCorrect = getCheck(password, hash);
    
    if(isPwdCorrect){
	  after++;
      return true;
    }
    else{
      after++;
      return false;
    }
  }
  public boolean getCheck(String password, String hash)throws
  org.apache.thrift.TException{
    boolean isPwdCorrect = false;
    System.out.println("Checking nodes");
    PrintBEEntries();
    String randElement = getRandEntry(liveBEInfo);
    String host = randElement.split("_")[0];
    String pport = liveBEInfo.get(randElement).get(0);
    //String host = "ecelinux6"; //sai's data from Hashmap
    //String pport= "1942";  //same as above- figure out how to distribute evenly
    TTransport transport;
    try{
    transport = new TSocket(host, Integer.parseInt(pport));
    transport.open();
    TProtocol protocol = new  TBinaryProtocol(transport);
    A1Password.Client clientPwd = new A1Password.Client(protocol);
    isPwdCorrect = clientPwd.checkPassword(password, hash);
    transport.close();
    return isPwdCorrect;
    }catch(TTransportException e){
       liveBEInfo.remove(randElement);
       System.out.println("removing node");
       PrintBEEntries();
       if(liveBEInfo.size() > 0){
          return getCheck(password, hash);
       }else{
          System.out.println("All BE Server down");
          //somehow tell client
       }
    }
    return isPwdCorrect;
  }
} 

