import java.util.ArrayList;

/**
 * A class for the Transaction object. Stores the name, transaction ID, whether the transaction is read only, the start time,
 * and a list of pending (uncommitted) operations
 */
public class Transaction {

  String name;
  int transactionID;
  boolean isReadOnly;
  int startTime;
  ArrayList<Operation> pendingOperations = new ArrayList<Operation>();

  /**
   * Transaction constructor. Parses the name to assign ID.
   * @param  name       The name from the input file
   * @param  isReadOnly Boolean whether it's a read only transaction
   * @param  startTime  The start time of the transaction
   */
  public Transaction(String name, boolean isReadOnly, int startTime) {
      this.name = name;
      String transaction = name.replaceAll("T", "");
      this.transactionID = Integer.parseInt(transaction);
      this.isReadOnly = isReadOnly;
      this.startTime = startTime;
  }

}
