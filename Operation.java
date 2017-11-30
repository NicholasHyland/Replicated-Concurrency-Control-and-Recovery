// This is a class to model Operations

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

  public String getOperationType() {
    return this.operationType;
  }

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

  public void setTime(int t) {
    this.time = t;
  }

  public void setReadSiteIndex(int i) {
    this.readSiteIndex = i;
  }

  public int getReadSiteIndex() {
    return this.readSiteIndex;
  }

}
