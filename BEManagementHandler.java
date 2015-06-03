import org.apache.thrift.TException;

// Generated code
import ece454750s15a1.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class BEManagementHandler implements A1Management.Iface {

  //private HashMap<Integer,PerfCounters> map;
  private Date startedUp = new Date();
  public static BEPasswordHandler handler;
  // public BEManagementHandler() {
  //   map = new HashMap<Integer, PerfCounters>();
  // }
  public BEManagementHandler(BEPasswordHandler pwdHandler) {
    //map = new HashMap<Integer, PerfCounters>();
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

  public void registrar(String host, String pport, String mport, String ncores) {
  }
}

