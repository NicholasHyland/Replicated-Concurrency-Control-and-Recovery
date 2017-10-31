import java.util.ArrayList;

//This is a class to model Transactions

public class Transaction {

  String name;
  boolean isReadOnly;
  double startTime;
  double endTime;
  ArrayList<Operation> operations;
  Operation currentOperation;

  public Transaction(String name, boolean isReadOnly, int t) {
      this.name = name;
      this.isReadOnly = isReadOnly;
      this.startTime = t;
  }

}
