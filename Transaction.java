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

  public Transaction(String name, boolean isReadOnly) {
      this.name = name;
      this.isReadOnly = isReadOnly;
  }

}
