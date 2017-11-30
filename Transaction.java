//This is a class to model Transactions
import java.util.ArrayList;

public class Transaction {

  String name;
  int transactionID;
  boolean isReadOnly;
  int startTime;
  double endTime;
  boolean aborted;
  ArrayList<Operation> pendingOperations = new ArrayList<Operation>();
 // Operation currentOperation;

  public Transaction(String name, boolean isReadOnly, int startTime) {
      this.name = name;
      String transaction = name.replaceAll("T", "");
      this.transactionID = Integer.parseInt(transaction);
      this.isReadOnly = isReadOnly;
      this.startTime = startTime;
  }

}
