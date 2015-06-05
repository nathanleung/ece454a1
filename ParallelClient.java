package ece454750s15a1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.io.IOException;  
import org.apache.thrift.TException; 
import org.apache.thrift.async.*;
import org.apache.thrift.TException;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;  
import org.apache.thrift.TException; 
import org.apache.thrift.async.*;
import org.apache.thrift.TException;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

public class ParallelClient {

  static int counter = 10;
  static CountDownLatch latch = new CountDownLatch(counter);


  public static void main(String [] args) {

    if (args.length != 6 || !args[0].contains("simple")) {
      System.out.println("Please enter 'simple' ");
      System.exit(0);
    }

    try {
      final String host = args[1];
      final String pport = args[2];
      final String mport = args[3];
      final String password = args[4];
      final Short logRounds = Short.parseShort(args[5]);
      // for(int i = 0; i < 5; ++i){
      //    System.out.println("Send request i = " + i);
      //    new Thread() {
      //       public void run() {
      //          try {
      //             TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
      //             TAsyncClientManager clientManager = new TAsyncClientManager();
      //             TNonblockingTransport transport = new TNonblockingSocket(host, Integer.parseInt(mport)); 
      //             A1Management.AsyncClient client = new A1Management.AsyncClient(
      //                 protocolFactory, clientManager, transport);
      //       client.getPerfCounters(new AddCallBack(latch, transport));
      //    } catch (TException x) {
      //      x.printStackTrace();
      //    } catch (IOException e) {  
      //      e.printStackTrace();
      //          }
      //       }
      //    }.start();
      //   System.out.println("After Send request i = " + i);
      // }
      for(int j = 0; j < 5; ++j){
         System.out.println("Send request j = " + j);
         new Thread() {
            public void run() {
               try {
                  TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
                  TAsyncClientManager clientManager = new TAsyncClientManager();
                  TNonblockingTransport transport = new TNonblockingSocket(host, Integer.parseInt(pport)); 
                  A1Password.AsyncClient client = new A1Password.AsyncClient(
                      protocolFactory, clientManager, transport);
            client.hashPassword(password, logRounds, new AddHashPwdCallBack(latch, transport, password, host, pport));
         } catch (TException x) {
           x.printStackTrace();
         } catch (IOException e) {  
           e.printStackTrace();
               }
            }
         }.start();
        System.out.println("After Send request j = " + j);
      }
      boolean wait = latch.await(30, TimeUnit.SECONDS);
      System.out.println("latch.await =:" + wait);

      System.out.println("Exiting client.");

    } catch (InterruptedException e) {  
      e.printStackTrace();
    } 
  }

  static class AddCallBack 
    implements AsyncMethodCallback<A1Management.AsyncClient.getPerfCounters_call> {

    private CountDownLatch latch;    
    private TNonblockingTransport transport;    

    public AddCallBack(CountDownLatch latch, TNonblockingTransport transp) {
        this.latch = latch;
        this.transport = transp;
    }
    public void onComplete(A1Management.AsyncClient.getPerfCounters_call add_call) {
        try {
            PerfCounters perfCounts = add_call.getResult();
            System.out.println("numSecondsUp: "+perfCounts.numSecondsUp);
        System.out.println("numRequestsReceived: "+perfCounts.numRequestsReceived);
        System.out.println("numRequestsCompleted: "+perfCounts.numRequestsCompleted);
        } catch (TException e) {
            e.printStackTrace();
  } finally {
            transport.close();
            latch.countDown();
  }
    }
    
    public void onError(Exception e) {
        System.out.println("Error : ");
        e.printStackTrace();
  latch.countDown();
    }
  }
  static class AddHashPwdCallBack 
    implements AsyncMethodCallback<A1Password.AsyncClient.hashPassword_call> {

    private CountDownLatch latch;    
    private TNonblockingTransport transport;    
    private String password;
    private String host;
    private String pport;
    private String hashedPwd;

    public AddHashPwdCallBack(CountDownLatch latch, TNonblockingTransport transp, String password,
                              String host, String pport) {
        this.latch = latch;
        this.transport = transp;
        this.password = password;
        this.host = host;
        this.pport = pport;
    }
    public void onComplete(A1Password.AsyncClient.hashPassword_call add_call) {
        try {
            hashedPwd = add_call.getResult();
            System.out.println("Hashing Password.......");
            System.out.println("Hashed Password: " +  hashedPwd);
            
        } catch (TException e) {
            e.printStackTrace();

        } finally {
                  transport.close();
                  latch.countDown();
                  new Thread() {
                      public void run() {
                         try {
                            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
                            TAsyncClientManager clientManager = new TAsyncClientManager();
                            TNonblockingTransport transport = new TNonblockingSocket(host, Integer.parseInt(pport)); 
                            A1Password.AsyncClient client = new A1Password.AsyncClient(
                                protocolFactory, clientManager, transport);
                      client.checkPassword(password, hashedPwd, new AddCheckPwdCallBack(latch, transport));
                   } catch (TException x) {
                     x.printStackTrace();
                   } catch (IOException e) {  
                     e.printStackTrace();
                         }
                      }
                   }.start();
        }
    }
    
    public void onError(Exception e) {
        System.out.println("Error : ");
        e.printStackTrace();
  latch.countDown();
    }
  } 
  static class AddCheckPwdCallBack 
    implements AsyncMethodCallback<A1Password.AsyncClient.checkPassword_call> {

    private CountDownLatch latch;    
    private TNonblockingTransport transport;    

    public AddCheckPwdCallBack(CountDownLatch latch, TNonblockingTransport transp) {
        this.latch = latch;
        this.transport = transp;
    }
    public void onComplete(A1Password.AsyncClient.checkPassword_call add_call) {
        try {
            boolean match = add_call.getResult();
            System.out.println("Checking Password......");
            if (match){
              System.out.println("Password and Hashed Password matched.");
            }
            else{
              System.out.println("Password and Hashed Password DON'T matched.");
            }
            
        } catch (TException e) {
            e.printStackTrace();
  } finally {
            transport.close();
            latch.countDown();
  }
    }
    
    public void onError(Exception e) {
        System.out.println("Error : ");
        e.printStackTrace();
  latch.countDown();
    }
  }

}
