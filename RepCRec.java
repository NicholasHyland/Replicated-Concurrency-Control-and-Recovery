import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * Models a distirbuted database and implements available copies approach, strict two-phase locking, 
 * multiversion concurrency control, deadlock detection, replication, and failure recovery
 * all with in each Transaction Manager. 
 */
public class RepCRec {

  /**
   * Main class
   * @param  args                  File names. These should be inside the Tests folder
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException {
    for (int i = 0; i < args.length; i++) { //process each test by file name
      String fileName = args[i];
      System.out.println("\nBEGIN " + fileName + "\n");
      ArrayList<Operation> operations = getOperations(fileName);
      TransactionManager transactionManager = new TransactionManager(operations);
      transactionManager.simulate();
      System.out.println("\nEND " + fileName);
    }
  }

  /**
   * Gets the operations from the file
   * @param  fileName              Name of the file
   * @return                       The array list of operations from this file
   * @throws FileNotFoundException 
   */
  public static ArrayList<Operation> getOperations(String fileName) throws FileNotFoundException {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    try {
      operations = openFile(fileName);
    }
    catch (Exception e) {
      System.out.println("Sorry could not process file " + fileName + ".");
      System.exit(0);
    }
    return operations;
  }

  /**
   * Opens the file creating a new operation each line and adding it to all list of operations
   * @param  fileName              Name of the file
   * @return                       The array list of operations from this file
   * @throws FileNotFoundException 
   */
  public static ArrayList<Operation> openFile(String fileName) throws FileNotFoundException {
    Scanner scanner = new Scanner(new File("Tests/" + fileName));
    ArrayList<Operation> operations = new ArrayList<Operation>();
    while (scanner.hasNextLine()) {
      String operationName = scanner.nextLine();
      operationName = operationName.replaceAll(" ", "");
      Operation operation = setOperation(operationName);
      operations.add(operation);
    }
    return operations;
  }

  /**
   * Parses each line and converts to an Operation object
   * @param  operationName The name of the operation
   * @return               The Operation created from this line
   */
  public static Operation setOperation(String operationName) {
    // get arguments of operation
    int start = operationName.indexOf("(");
    int end = operationName.indexOf(")");
    String[] arguments = (operationName.substring(start + 1, end)).split(","); // arguments of the operation
    int argumentsLength = arguments.length;
    // initialize all variables to their default
    String operationType = operationName.substring(0, start); // name of the operation
    String transactionName = null;
    String variableName = null;
    int value = 0;
    int dumpVariable = 0;
    int dumpSite = 0;
    int failSite = 0;
    int recoverSite = 0;
    // depending on the operation type, set the corresponding variables
    switch(operationType) {
      case "begin":
        transactionName = arguments[0];
        break;
      case "beginRO":
        transactionName = arguments[0];
        break;
      case "end":
        transactionName = arguments[0];
        break;
      case "W":
        transactionName = arguments[0];
        variableName = arguments[1];
        value = Integer.parseInt(arguments[2]);
        break;
      case "R":
        transactionName = arguments[0];
        variableName = arguments[1];
        break;
      case "dump":
        if (arguments[0].length() != 0) {
          if (arguments[0].contains("x")) {
            dumpVariable = Integer.parseInt((arguments[0].substring(1, arguments[0].length())));
          }
          else {
            dumpSite = Integer.parseInt(arguments[0]);
          }
        }
        break;
      case "fail":
        failSite = Integer.parseInt(arguments[0]);
        break;
      case "recover":
        recoverSite = Integer.parseInt(arguments[0]);
        break;
    }
    // create new operation and return
    Operation operation = new Operation(operationName, operationType, transactionName, variableName, value, dumpVariable, dumpSite, failSite, recoverSite);
    return operation;
  }

}

