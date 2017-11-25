import java.util.ArrayList;

//This is a class to model Transactions
import java.util.ArrayList;

public class Transaction {

  String name;
  boolean isReadOnly;
  double startTime;
  double endTime;
  boolean aborted;
  ArrayList<Operations> operations;
  Operation currentOperation;

  public Transaction(String name, boolean isReadOnly, int t) {
      this.name = name;
      this.isReadOnly = isReadOnly;
      this.startTime = t;
  }

}
