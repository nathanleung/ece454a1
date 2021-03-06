package ece454750s15a1;

import org.apache.thrift.TException;

import ece454750s15a1.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

public class FEManagementHandler implements A1Management.Iface {

  private Date startedUp = new Date();
  public static FEPasswordHandler handler;
  public ConcurrentMap<String, List<String>> liveBEInfo = new ConcurrentHashMap();
  public ConcurrentMap<String, List<String>> liveFEInfo = new ConcurrentHashMap();

  public FEManagementHandler(FEPasswordHandler pwdHandler) {
    this.handler = pwdHandler;
  }

  public PerfCounters getPerfCounters (){
    Date currentTime = new Date();
    long secondsDiff = (currentTime.getTime()-startedUp.getTime())/1000;
    PerfCounters perfCounts = new PerfCounters();
    perfCounts.numSecondsUp = (int)secondsDiff;
    perfCounts.numRequestsReceived = handler.getBefore();
    perfCounts.numRequestsCompleted = handler.getAfter();
    return perfCounts;
  }
  public List<String> getGroupMembers (){
      List<String> members = new ArrayList<String>();
      members.add("nhleung");
      members.add("swmaung");
      return members;
  }
  public void beToFERegistrar(String host, String pport, String mport, String ncores) {
    List<String> valSet = new ArrayList<String>();
    valSet.add(pport);
    valSet.add(mport);
    valSet.add(ncores);
    liveBEInfo.put(host+"_"+mport, valSet);
    System.out.println("In BE Registrar Function");
    for (ConcurrentMap.Entry<String, List<String>> e: liveBEInfo.entrySet()){
      System.out.println(e.getKey() + "," + e.getValue());
    }
    handler.syncBEInfo(liveBEInfo);
  }
  public void feToFERegistrar(String host, String pport, String mport, String ncores) {
    List<String> valSet = new ArrayList<String>();
    valSet.add(pport);
    valSet.add(mport);
    valSet.add(ncores);
    liveFEInfo.put(host+"_"+mport, valSet);
    System.out.println("In FE Registrar Function");
    for (ConcurrentMap.Entry<String, List<String>> e: liveFEInfo.entrySet()){
      System.out.println(e.getKey() + "," + e.getValue());
    }
    //handler.syncBEInfo(liveBEInfo);
  }

}

