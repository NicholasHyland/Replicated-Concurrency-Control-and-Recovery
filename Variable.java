// This is a class to model Variables

public class Variable {

  String name;
  boolean isCopy;
  Site site;
  int value;
  ArrayList<Transaction> transactions;
  boolean isReadLocked;

  public Variable (String name, int value, boolean isCopy) {
    this.name = name;
    this.value = value; // 10 * Integer.parseInt((name.split("x."))[0]);
    this.isCopy = isCopy; // name.contains(".");
  }

}