import org.apache.thrift.TException;
import ece454750s15a1.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.util.Date;
import org.mindrot.jbcrypt.BCrypt;

public class FEPasswordHandler implements A1Password.Iface {
  private int before;
  private int after;
  public FEPasswordHandler() {
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
    String host = "ecelinux6"; //sai's data from Hashmap
    String pport= "1942";  //same as above- figure out how to distribute evenly
    TTransport transport;
    transport = new TSocket(host, Integer.parseInt(pport));
    transport.open();
    TProtocol protocol = new  TBinaryProtocol(transport);
    A1Password.Client clientPwd = new A1Password.Client(protocol);
    String hashed = clientPwd.hashPassword(password, logRounds);
    //String hashed = BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    transport.close();
    after++;
    return hashed;
  }

  public boolean checkPassword(String password, String hash) throws
  org.apache.thrift.TException {
    before++;
    String host = "ecelinux6"; //sai's data from Hashmap
    String pport= "1942";  //same as above- figure out how to distribute evenly
    TTransport transport;
    transport = new TSocket(host, Integer.parseInt(pport));
    transport.open();
    TProtocol protocol = new  TBinaryProtocol(transport);
    A1Password.Client clientPwd = new A1Password.Client(protocol);
    boolean isPwdCorrect = clientPwd.checkPassword(password, hash);
    transport.close();
    if(isPwdCorrect){
	  after++;
      return true;
    }
    else{
      after++;
      return false;
    }
  }
  
} 

