import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import org.apache.thrift.TProcessor;
// Generated code
import ece454750s15a1.*;

import java.util.HashMap;

public class FEServer {

  
  public static FEManagementHandler handlerMgmt;
  public static FEPasswordHandler handlerPwd;

  public static TProcessor processor;
  public static A1Management.Processor processorMgmt;
  public static A1Password.Processor processorPwd;

  public static void main(String [] args) {
    try {
  if(args.length != 2){
    System.out.println("specify the port number");
  }
  final String pport = args[0];
  final String mport = args[1];

  handlerPwd = new FEPasswordHandler();
  handlerMgmt = new FEManagementHandler(handlerPwd);
  //handlerPwd.setMgmtHandler(handlerMgmt);
  processorMgmt = new A1Management.Processor(handlerMgmt);
  processorPwd = new A1Password.Processor(handlerPwd);
  // processor.registerProcessor(
  //       "A1Management",
  //       new A1Management.Processor(handlerMgmt));
  // processor.registerProcessor(
  //       "A1Password",
  //       new A1Password.Processor(handlerPwd));

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
      TServerTransport serverTransport = new TServerSocket(Integer.parseInt(portNum));
      TServer server = new TSimpleServer(
              new Args(serverTransport).processor(processor));

      System.out.println("Starting the fe server..."+portNum);
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
