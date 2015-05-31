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

// Generated code
import ece454750s15a1.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;

import java.util.List;
import java.util.ArrayList;

public class TestClient {
  public static void main(String [] args) {

    if (args.length != 6 || !args[0].contains("simple")) {
      System.out.println("Please enter 'simple' ");
      System.exit(0);
    }

    try {
      String host = args[1];
      String pport = args[2];
      String mport = args[3];
      String password = args[4];
      Short logRounds = Short.parseShort(args[5]);
      TTransport transport;
      transport = new TSocket(host, Integer.parseInt(pport));
      transport.open();

      TTransport transportMgmt;
      transportMgmt = new TSocket(host, Integer.parseInt(mport));
      transportMgmt.open();

      TProtocol protocol = new  TBinaryProtocol(transport);
      TProtocol protocol2 = new  TBinaryProtocol(transportMgmt);
      A1Password.Client clientPwd = new A1Password.Client(protocol);
      A1Management.Client clientMgmt = new A1Management.Client(protocol2);
      // TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, "A1Management");
      // A1Management.Client client = new A1Management.Client(mp);
      
      // TMultiplexedProtocol mp2 = new TMultiplexedProtocol(protocol, "A1Password");
      // A1Password.Client clientPassword = new A1Password.Client(mp2);
      
      perform(clientMgmt);
      performPwd(clientPwd, password, logRounds);

      transport.close();
      transportMgmt.close();
    } catch (TException x) {
      x.printStackTrace();
    } 
  }
  private static void perform(A1Management.Client client) throws TException
  {

    PerfCounters perfCounts = client.getPerfCounters();
    System.out.println("numSecondsUp: "+perfCounts.numSecondsUp);
    System.out.println("numRequestsReceived: "+perfCounts.numRequestsReceived);
    System.out.println("numRequestsCompleted: "+perfCounts.numRequestsCompleted);
    List<String> members = client.getGroupMembers();
    System.out.println("first member: "+members.get(0));
    System.out.println("2nd member: "+members.get(1));

    perfCounts = client.getPerfCounters();
    System.out.println("numSecondsUp: "+perfCounts.numSecondsUp);
  }
  private static void performPwd(A1Password.Client client, String pwd, Short logRou) throws TException
  { 
    System.out.println("Hashing Password.......");
    String hashedPwd = client.hashPassword(pwd, logRou);
    System.out.println("Hashed Password: " +  hashedPwd);
    System.out.println("Checking Password......");
    boolean match = client.checkPassword(pwd, hashedPwd);
    if (match)
      System.out.println("Password and Hashed Password matched.");
    else
      System.out.println("Password and Hashed Password DON'T matched.");
  }
}
