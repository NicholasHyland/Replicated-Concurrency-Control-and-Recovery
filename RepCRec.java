import java.io.File;
import java.io.FileNotFoundException;

import java.util.Scanner;
import java.util.ArrayList;


public class RepCRec {

  public static void main(String[] args) throws FileNotFoundException {
    for (int i = 0; i < args.length; i++) {
      String fileName = args[i];
      ArrayList<Operation> operations = getOperations(fileName);
    }
  }

  // Trys to open the File to return the operations
  public static ArrayList<Operation> getOperations(String fileName) throws FileNotFoundException {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    try {
      operations = openFile(fileName);
    }
    catch (Exception e) {
      System.out.println("Sorry could not find file " + fileName + ".");
      System.exit(0);
    }
    return operations;
  }

  // Opens the file creating a new operation each line and adding it to all list of operations
  public static ArrayList<Operation> openFile(String fileName) throws FileNotFoundException {
    Scanner scanner = new Scanner(new File(fileName));
    ArrayList<Operation> operations = new ArrayList<Operation>();
    while (scanner.hasNextLine()) {
      String operationName = scanner.nextLine();
      System.out.println(operationName);
      Operation operation = setOperation(operationName); // still need to create this method
      operations.add(operation);
    }
    return operations;
  }
}
