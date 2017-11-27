// This is a class to model Sites
import java.util.ArrayList;

public class Site {

  boolean isDown;
  boolean wasDown;
  int wasDownTime;
  int number;
  ArrayList<Variable> variables = new ArrayList<Variable>();
  LockTable lockTable = new LockTable();


  public Site(int number) {
    this.isDown = false;
    this.wasDown = false;
    this.number = number;

    //initialize variables at this site
    //if this is an even numbered site
    if (number % 2 == 0){
      int i = 1;
      while (i < 21){
        //add the 2 odd variables and all even variables
        // if it one of the 2 odd variables, then add - it is not a copy
        if ((i % 10 + 1) == number) {
          this.variables.add(new Variable(i, false, number));
        }
        // if it is an even variable, then add - not a copy if the variable number is the same as the site number
        else if ((i % 2) == 0) {
          if (i == number) {
            this.variables.add(new Variable(i, false, number));
          }
          else {
            this.variables.add(new Variable(i, true, number));
          }
        }
        i++;
      }
    }
    //if this is an odd numbered site add only the even variables
    else {
      int i = 2;
      while (i < 21){
        this.variables.add(new Variable(i, true, number)); // all variables at odd numbered sites are copies
        i += 2;
      }
    }
  }

  public void fail() {
    this.lockTable = new LockTable(); //set lock table to be new lock table - erases previous locks
    this.isDown = true;
  }

  public void update(Operation o) {
    for (Variable v: this.variables) {
      if (v.number == o.variableID) {
        v.setValue(o.value);
      }
    }

  }
}
