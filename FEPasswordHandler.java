package ece454750s15a1;

import org.apache.thrift.TException;
//import ece454750s15a1.*;
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
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class FEPasswordHandler implements A1Password.Iface {
  private AtomicInteger before;
  private AtomicInteger after;
  public ConcurrentMap<String, List<String>> liveBEInfo = new ConcurrentHashMap();
  public FEPasswordHandler() {
    before = new AtomicInteger();
    after = new AtomicInteger();
  }
  public int getBefore(){
    return before.get();
  }
  public int getAfter(){
    return after.get();
  }
  public void syncBEInfo(ConcurrentMap<String, List<String>> mgmtBEInfo){
    liveBEInfo = mgmtBEInfo;
  }
  public String getRandEntry(ConcurrentMap<String, List<String>> beInfo){
    Random rand = new Random();
    double randomNum = rand.nextDouble();
    double currProb = 0;
    int  totalCores = 0;
    for (ConcurrentMap.Entry<String, List<String>> e: beInfo.entrySet()){
    	totalCores+= Integer.parseInt(e.getValue().get(2));
    }
    for (ConcurrentMap.Entry<String, List<String>> e: beInfo.entrySet()){
    	double coreProb = Integer.parseInt(e.getValue().get(2))/(double)totalCores;
    	System.out.println("randomNum: "+randomNum+", coreProb: "+coreProb+", totalCores: "+totalCores);
      if(randomNum >= currProb && randomNum < currProb+coreProb){
        return e.getKey();
      }else{
        currProb+=coreProb;
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
  public boolean waitSixtySeconds(){
      System.out.println("In timer");
      Date startTime = new Date();
      Date currentTime = new Date();
      while(((currentTime.getTime()-startTime.getTime())/1000) < 60){
        try {
            Thread.sleep(1000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if(liveBEInfo.size() > 0){
          return true;
        }
        currentTime = new Date();
      }
      return false;
  }

  public String hashPassword (String password, short logRounds) throws
  ServiceUnavailableException, org.apache.thrift.TException {
    before.getAndAdd(1);
    String hashed = getHash(password, logRounds);
    after.getAndAdd(1);
    return hashed;
  }

  public String getHash(String password, short logRounds)throws
  ServiceUnavailableException, org.apache.thrift.TException{
    String hashed = "";
    //System.out.println("Checking nodes");
    //PrintBEEntries();
    if(liveBEInfo.size() <= 0){
      if(!waitSixtySeconds())
          throw new ServiceUnavailableException("No service requests could be processed. Timeout at 1 minute");
    }
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
       e.printStackTrace();
       liveBEInfo.remove(randElement);
       System.out.println("removing node");
       PrintBEEntries();
       if(liveBEInfo.size() > 0){
          return getHash(password, logRounds);
       }else{
          System.out.println("All BE Server down");
          //somehow tell client
          if(waitSixtySeconds()){
            return getHash(password, logRounds);
          }else{
            throw new ServiceUnavailableException("No service requests could be processed. Timeout at 1 minute");
          }
       }
    }
    //return hashed;
  }

  public boolean checkPassword(String password, String hash) throws
  ServiceUnavailableException, org.apache.thrift.TException {
    before.getAndAdd(1);
    boolean isPwdCorrect = getCheck(password, hash);
    
    if(isPwdCorrect){
	  after.getAndAdd(1);
      return true;
    }
    else{
      after.getAndAdd(1);
      return false;
    }
  }
  public boolean getCheck(String password, String hash)throws
  ServiceUnavailableException, org.apache.thrift.TException{
    boolean isPwdCorrect = false;
    //System.out.println("Checking nodes");
    //PrintBEEntries();
    if(liveBEInfo.size() <= 0){
      if(!waitSixtySeconds())
          throw new ServiceUnavailableException("No service requests could be processed. Timeout at 1 minute");
    }
    String randElement = getRandEntry(liveBEInfo);
    String host = randElement.split("_")[0];
    String pport = liveBEInfo.get(randElement).get(0);

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
       e.printStackTrace();
       liveBEInfo.remove(randElement);
       System.out.println("removing node");
       PrintBEEntries();
       if(liveBEInfo.size() > 0){
          return getCheck(password, hash);
       }else{
          System.out.println("All BE Server down");
          //somehow tell client
          if(waitSixtySeconds()){
            return getCheck(password, hash);
          }else{
            throw new ServiceUnavailableException("No service requests could be processed. Timeout at 1 minute");
          }
       }
    }
    //return isPwdCorrect;
  }
} 

