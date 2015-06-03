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

import org.apache.thrift.TProcessor;
// Generated code
import ece454750s15a1.*;

import java.util.HashMap;

public class BEServer {

  
  public static BEManagementHandler handlerMgmt;
  public static BEPasswordHandler handlerPwd;

  public static A1Management.Processor processorMgmt;
  public static A1Password.Processor processorPwd;

  public static void main(String [] args) {
    try {
  if(args.length != 6){
    System.out.println("specify the port number");
  }
  final String behost = args[0];
  final String pport = args[1];
  final String mport = args[2];
  final String ncores = args[3];

  final String fehost = args[4]; // port number to start the FENode
  final String feport = args[5]; // port number that FESeed is listening

  TTransport transport = new TSocket(fehost, Integer.parseInt(feport));
  transport.open();
  TProtocol protocol = new TBinaryProtocol(transport);
  A1Management.Client clientRegister = new A1Management.Client(protocol);
  clientRegister.registrar(behost, pport, mport, ncores);
  transport.close();
  handlerPwd = new BEPasswordHandler();
  handlerMgmt = new BEManagementHandler(handlerPwd);
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

      System.out.println("Starting the be server..."+portNum);
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
