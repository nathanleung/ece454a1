package ece454750s15a1;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.TProtocolFactory;
import java.io.IOException;   
import org.apache.thrift.async.*;

import org.apache.thrift.TProcessor;
//import ece454750s15a1.*;

import java.util.HashMap;
import java.util.*;

public class BEServer {

  
  public static BEManagementHandler handlerMgmt;
  public static BEPasswordHandler handlerPwd;

  public static A1Management.Processor processorMgmt;
  public static A1Password.Processor processorPwd;
  static volatile boolean finish = false;

  public static void main(String [] args) {
    try {
  // if(args.length != 6){
  //   System.out.println("specify the port number");
  // }
  //final String behost = args[0];
  //final String pport = args[1];
  //final String mport = args[2];
  //final String ncores = args[3];

  //final String fehost = args[4]; // port number to start the FENode
  //final String feport = args[5]; // port number that FESeed is listening

  int i = 0;
      String behost = "";
      String pportTmp = "";
      String mportTmp = "";
      String ncores = "";
      String seeds = "";

      while (i < args.length) {
        if(args[i].equals("-host"))
          behost += args[i+1];
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
      System.out.println(behost);
      System.out.print("pport: ");
      System.out.println(pport);
      System.out.print("mport: ");
      System.out.println(mport);
      System.out.print("ncores: ");
      System.out.println(ncores);
      if(seeds != ""){
        for (int j=0; j<seedsList.size(); j++) {
          String seed = seedsList.get(j);
          int index = seed.indexOf(":"); 
          String seedHost = seed.substring(0, index);
          String seedPort = seed.substring(index+1);
          System.out.print("Seed Host: "); 
          System.out.println(seed.substring(0, index));
          System.out.print("Seed Management Port: ");
          System.out.println(seed.substring(index+1));
        }
      } 
      //default at 0 for now
      String seed = seedsList.get(0);
      int index = seed.indexOf(":");
      String seedHost = seed.substring(0, index);
      String seedPort = seed.substring(index+1);

  handlerPwd = new BEPasswordHandler();
  handlerMgmt = new BEManagementHandler(handlerPwd);
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
    
      try {
        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
        TAsyncClientManager clientManager = new TAsyncClientManager();
        TNonblockingTransport transport = new TNonblockingSocket(seedHost, Integer.parseInt(seedPort)); 

        A1Management.AsyncClient client = new A1Management.AsyncClient(
            protocolFactory, clientManager, transport);

        client.beToFERegistrar(behost, pport, mport, ncores, new AddCallBack());
        System.out.println("After Send Async call.");

      //   int k = 0;
      //   while (!finish) {  
      // try{Thread.sleep(1000);}catch(InterruptedException e){System.out.println(e);}
      //       k++;    
      // System.out.println("Sleep " +k + " Seconds.");
      //   }

      //   System.out.println("Exiting client.");

      } catch (TException x) {
        x.printStackTrace();
      } catch (IOException e) {  
        e.printStackTrace();
      } 
    
    } catch (Exception x) {
      x.printStackTrace();
    }

  }

  public static void simple(TProcessor processor, String portNum) {
    try {
      TServerTransport serverTransport = new TServerSocket(Integer.parseInt(portNum));
      TServer server = new TSimpleServer(
              new Args(serverTransport).processor(processor));

      System.out.println("Starting the be server..."+portNum);
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class AddCallBack 
    implements AsyncMethodCallback<A1Management.AsyncClient.beToFERegistrar_call> {
    
    public void onComplete(A1Management.AsyncClient.beToFERegistrar_call add_call) {
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
