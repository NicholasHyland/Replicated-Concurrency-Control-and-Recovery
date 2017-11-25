import java.util.ArrayList;

// This is a class to model Variables

public class Variable {

  int number;
  boolean isCopy;
  int site;
  int value;
  int commitTime;
  ArrayList<Transaction> transactions;
  ArrayList<Lock> lockTable;
  boolean isReadLocked;
  boolean isWriteLocked;

  public Variable (int number, boolean isCopy, int site) {
    this.number = number; // 10 * Integer.parseInt((name.split("x."))[0]);
    this.value = number * 10;
    this.site = site;
    this.isCopy = isCopy; // name.contains(".");
    this.commitTime = 0;
  }

}
