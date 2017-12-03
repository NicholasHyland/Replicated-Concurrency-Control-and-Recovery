/**
 * A class for the Operation object. An operation is read from the input file. 
 * There are 8 types: begin, beginRO, R, W, end, fail, recover, dump
 */
public class Operation {

  String operationName;
  String operationType; //required for all
  String transactionName;
  int transactionID; //required for begin, beginRO, R, W, end
  int variableID; // required for R, W,
  int value; // required for W
  int dumpVariable; // required for dump(i)
  int dumpSite; // required for dump(xj)
  int failSite; // required for fail(i)
  int recoverSite; // required for recover(i)
  int time; //time this r/w operation accessed a site
  int readSiteIndex; //the site that a read transaction/operation read from if its an even variable

  /**
   * Operation constructor
   * @param  operationName   The name from the file
   * @param  operationType   Type of operation (begin, beginRO, R, W, end, fail, recover, dump)
   * @param  transactionName Name from the input file (T1). This is parsed to get transaction
   * @param  variableName    The variable this transaction is reading or writing 
   * @param  value           The value this transaction is trying to writes
   * @param  dumpVariable    The variable for a dump operation
   * @param  dumpSite        The site ID for a dump operation
   * @param  failSite        The site ID for a fail operation
   * @param  recoverSite     The site ID for a recover operation
   */
  public Operation(String operationName, String operationType, String transactionName, String variableName, int value, int dumpVariable, int dumpSite, int failSite, int recoverSite) {
    this.operationName = operationName;
    this.operationType = operationType;
    this.transactionName = transactionName;
    if (transactionName != null) {
      String transaction = transactionName.replaceAll("T", "");
      this.transactionID = Integer.parseInt(transaction);
    }
    if (variableName != null) {
      String variable = variableName.replaceAll("x", "");
      this.variableID = Integer.parseInt(variable);
    }
    this.value = value;
    this.dumpVariable = dumpVariable;
    this.dumpSite = dumpSite;
    this.failSite = failSite;
    this.recoverSite = recoverSite;
  }

  /**
   * Getter for operation type
   * @return the type of this operation
   */
  public String getOperationType() {
    return this.operationType;
  }
  /**
   * Used for debugging. Prints information about the operation.
   */
  public void printOperation() {
    System.out.print(this.operationName + ": ");
    switch(this.operationType) {
      case "begin":
        System.out.println("Transaction " + transactionID + " begins");
        break;
      case "beginRO":
        System.out.println("Read-Only Transaction " + transactionID + " begins");
      case "end":
        System.out.println("Transaction " + transactionID + " ends");
        break;
      case "W":
        System.out.println("Transaction " + transactionID + " writes " + value + " to variable " + variableID);
        break;
      case "R":
        System.out.println("Transaction " + transactionID + " reads " + variableID);
        break;
      case "dump":
        if (dumpVariable != 0) {
          System.out.println("Give all committed values of all copies of variable x" + dumpVariable);
        }
        else if (dumpSite != 0) {
          System.out.println("Give all committed values of all copies of all variables at site " + dumpSite);
        }
        else {
          System.out.println("Give all committed values of all copies of all variables");
        }
        break;
      case "fail":
        System.out.println("Site " + failSite + " fails");
        break;
      case "recover":
        System.out.println("Site " + recoverSite + " recovers");
        break;
    }
  }

  /**
   * A setter for operation time
   * @param t The time to set
   */
  public void setTime(int t) {
    this.time = t;
  }

  /**
   * A setter for the site index. 
   * Used only by read operations to keep track of which site it read from for an even variable.
   * @param i The site index
   */
  public void setReadSiteIndex(int i) {
    this.readSiteIndex = i;
  }

  /**
   * A getter for the site index.
   * @return The site index
   */
  public int getReadSiteIndex() {
    return this.readSiteIndex;
  }

}
