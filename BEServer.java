/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import org.apache.thrift.TMultiplexedProcessor;
// Generated code
import ece454750s15a1.*;

import java.util.HashMap;

public class BEServer {

  
  public static BEManagementHandler handlerMgmt;
  public static BEPasswordHandler handlerPwd;

  public static TMultiplexedProcessor processor = new TMultiplexedProcessor();

  public static void main(String [] args) {
    try {
  if(args.length != 1){
    System.out.println("specify the port number");
  }
  final String portNum = args[0];

  handlerPwd = new BEPasswordHandler();
  handlerMgmt = new BEManagementHandler(handlerPwd);

  processor.registerProcessor(
        "A1Management",
        new A1Management.Processor(handlerMgmt));
  processor.registerProcessor(
        "A1Password",
        new A1Password.Processor(handlerPwd));

      Runnable simple = new Runnable() {
        public void run() {
          simple(processor, portNum);
        }
      };      

      new Thread(simple).start();
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void simple(TMultiplexedProcessor processor, String portNum) {
    try {
      TServerTransport serverTransport = new TServerSocket(Integer.parseInt(portNum));
      TServer server = new TSimpleServer(
              new Args(serverTransport).processor(processor));

      System.out.println("Starting the simple server...");
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
