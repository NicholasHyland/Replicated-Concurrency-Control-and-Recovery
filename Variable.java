import java.util.ArrayList;

// This is a class to model Variables

public class Variable {

  String name;
  int number;
  boolean isCopy;
  Site site;
  int value;
  int commitTime;
  ArrayList<Transaction> transactions;
  boolean isReadLocked;

  public Variable (String name, int value, boolean isCopy) {
    this.name = name;
    this.value = value; // 10 * Integer.parseInt((name.split("x."))[0]);
    this.isCopy = isCopy; // name.contains(".");
  }
  
  public Variable (int number, int value) {
    this.number = number;
    this.value = value;
    this.commitTime = 0;
  }

}
