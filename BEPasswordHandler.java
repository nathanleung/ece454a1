package ece454750s15a1;

import org.apache.thrift.TException;
//import ece454750s15a1.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import org.mindrot.jbcrypt.BCrypt;

public class BEPasswordHandler implements A1Password.Iface {
  private int before;
  private int after;
  public BEPasswordHandler() {
    before = 0;
    after = 0;
  }
  public int getBefore(){
    return before;
  }
  public int getAfter(){
    return after;
  }
 
  public String hashPassword (String password, short logRounds) throws
  ServiceUnavailableException, org.apache.thrift.TException {
    before++;
    String hashed = BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    after++;
    return hashed;
  }

  public boolean checkPassword(String password, String hash) throws
  org.apache.thrift.TException {
    before++;
    if(BCrypt.checkpw(password, hash)){
	  after++;
      return true;
    }
    else{
      after++;
      return false;
    }
  }
  
} 

