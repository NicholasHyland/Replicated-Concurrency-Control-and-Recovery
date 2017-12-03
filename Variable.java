import java.util.ArrayList;

/**
 * A class for the Variable object. Stores variable ID, the value for this variable
 * and the latest commitTime for this variable
 */

public class Variable {

  int number;
  //int site;
  int value;
  int commitTime;

  /**
   * Variable constructor assigns the ID, initalizes the value to ID*10 and the commitTime to 0
   * @param  number The id of this Variable
   */
  public Variable (int number, int site) {
    this.number = number; // 10 * Integer.parseInt((name.split("x."))[0]);
    this.value = number * 10;
    this.commitTime = 0;
  }

  /**
   * Setter for variable value
   * @param v    value to set
   * @param time latest commitTime
   */
  public void setValue(int v, int time) {
    this.value=v;
    this.commitTime=time;
  }

  /**
   * Getter for value  
   * @return the current value of this variable
   */
  public int getValue(){
    return this.value;
  }

}
