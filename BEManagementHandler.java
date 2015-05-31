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
}

