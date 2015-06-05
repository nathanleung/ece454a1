package ece454750s15a1;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.*; 
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.protocol.TProtocolFactory;
import java.io.IOException;   
import org.apache.thrift.async.*;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.*;

import org.apache.thrift.TProcessor;
// Generated code
//import ece454750s15a1.*;

import java.util.HashMap;
import java.util.*;

public class FEServer {

  
  public static FEManagementHandler handlerMgmt;
  public static FEPasswordHandler handlerPwd;

  public static TProcessor processor;
  public static A1Management.Processor processorMgmt;
  public static A1Password.Processor processorPwd;

  static volatile boolean finish = false;

  public static void main(String [] args) {
    try {
      int i = 0;
      String fehost = "";
      String pportTmp = "";
      String mportTmp = "";
      String ncores = "";
      String seeds = "";

      while (i < args.length) {
        if(args[i].equals("-host"))
          fehost += args[i+1];
        else if (args[i].equals("-pport"))
          pportTmp += args[i+1];
        else if (args[i].equals("-mport")) 
          mportTmp += args[i+1];
        else if (args[i].equals("-ncores"))
          ncores += args[i+1];
        else if (args[i].equals("-seeds"))
          seeds += args[i+1];
        i += 2;
      }
      List<String> seedsList = null;
      final String pport = pportTmp;
      final String mport = mportTmp;
      if(seeds != ""){
        seedsList = Arrays.asList(seeds.split(",")); 
      }
      System.out.print("FEHost: ");
      System.out.println(fehost);
      System.out.print("pport: ");
      System.out.println(pport);
      System.out.print("mport: ");
      System.out.println(mport);
      System.out.print("ncores: ");
      System.out.println(ncores);
      boolean isSeed = false;
      String seedHost = "";
      String seedPort = "";
      if(seeds != ""){
        for (int j=0; j<seedsList.size(); j++) {
          String seed = seedsList.get(j);
          int index = seed.indexOf(":"); 
          seedHost = seed.substring(0, index);
          seedPort = seed.substring(index+1);
          System.out.print("Seed Host: "); 
          System.out.println(seed.substring(0, index));
          System.out.print("Seed Management Port: ");
          System.out.println(seed.substring(index+1));

          if (fehost.equals(seedHost) && mport.equals(seedPort)) {
            isSeed = true;
            break;
          }
        }
        if(!isSeed){
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            TAsyncClientManager clientManager = new TAsyncClientManager();
            TNonblockingTransport transport = new TNonblockingSocket(seedHost, Integer.parseInt(seedPort)); 

            A1Management.AsyncClient client = new A1Management.AsyncClient(
                protocolFactory, clientManager, transport);

            client.feToFERegistrar(fehost, pport, mport, ncores, new AddCallBack());
            System.out.println("After Send Async call.");
        }
      }   

      handlerPwd = new FEPasswordHandler();
      handlerMgmt = new FEManagementHandler(handlerPwd);
      //handlerPwd.setMgmtHandler(handlerMgmt);
      processorMgmt = new A1Management.Processor(handlerMgmt);
      processorPwd = new A1Password.Processor(handlerPwd);

      Runnable pwdRun = new Runnable() {
        public void run() {
          simple(processorPwd, pport);
        }
      };   
      Runnable mgmtRun = new Runnable() {
        public void run() {
          simple(processorMgmt, mport);
        }
      };     

      new Thread(pwdRun).start();
      new Thread(mgmtRun).start();
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void simple(TProcessor processor, String portNum) {
    try {
      // TServerTransport serverTransport = new TServerSocket(Integer.parseInt(portNum));
      // TServer server = new TThreadPoolServer(
      //         new TThreadPoolServer.Args(serverTransport).processor(processor).minWorkerThreads(2).maxWorkerThreads(4));
      TNonblockingServerSocket socket = new TNonblockingServerSocket(Integer.parseInt(portNum));  
      THsHaServer.Args arg = new THsHaServer.Args(socket); 
      arg.protocolFactory(new TBinaryProtocol.Factory());  
      arg.transportFactory(new TFramedTransport.Factory()); 
      arg.processorFactory(new TProcessorFactory(processor));  
      arg.workerThreads(5);

      TServer server = new THsHaServer(arg);  

      System.out.println("Starting the fe server..."+portNum);
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class AddCallBack 
    implements AsyncMethodCallback<A1Management.AsyncClient.feToFERegistrar_call> {
    
    public void onComplete(A1Management.AsyncClient.feToFERegistrar_call add_call) {
        try {
            add_call.getResult();
            System.out.println("Add from server: " +"TEST");
        } catch (TException e) {
            e.printStackTrace();
        }
        finish = true;
    }
    
    public void onError(Exception e) {
        System.out.println("Error : ");
        e.printStackTrace();
        finish = true;
    }
  }

}
