// This is a class to model Operations

public class Operation {

  String operationType; //required for all
  String transactionName; //required for begin, beginRO, R, W, end
  String variableName; // required for R, W,
  int value; // required for W
  int dumpVariable; // required for dump(i)
  int dumpSite; // required for dump(xj)
  int failSite; // required for fail(i)
  int recoverSite; // required for recover(i)

  public Operation(String operationType, String transactionName, String variableName, int value, int dumpVariable, int dumpSite, int failSite, int recoverSite) {
    this.operationType = operationType;
    this.transactionName = transactionName;
    this.variableName = variableName;
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
    switch(this.operationType) {
      case "begin":
        System.out.println("Transaction " + transactionName + " begins");
        break;
      case "beginRO":
        System.out.println("Read-Only Transaction " + transactionName + " begins");
      case "end":
        System.out.println("Transaction " + transactionName + " ends");
        break;
      case "W":
        System.out.println("Transaction " + transactionName + " writes " + value + " to variable " + variableName);
        break;
      case "R":
        System.out.println("Transaction " + transactionName + " reads " + variableName);
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

}
