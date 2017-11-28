// This is a class to model Sites
import java.util.ArrayList;

public class Site {

  boolean isDown;
  boolean wasDown;
  int latestDownTime;
  int latestCommitTime;
  int number;
  ArrayList<Variable> variables = new ArrayList<Variable>();
  ArrayList<Update> updates = new ArrayList<Update>();
  LockTable lockTable = new LockTable();


  public Site(int number) {
    this.isDown = false;
    this.wasDown = false;
    this.number = number;
    this.latestCommitTime=0;

    //initialize variables at this site
    //if this is an even numbered site
    if ((number % 2) == 0) {
      int i = 1;
      while (i < 21) {
        if (((i % 10) + 1) == number) {
          this.variables.add(new Variable(i, number));
        }
        else if ((i % 2) == 0) {
          this.variables.add(new Variable(i, number));
        }
        i++;
      }
    }
    else {
      int i = 2;
      while (i < 21) {
        this.variables.add(new Variable(i, number));
        i += 2;
      }
    }
  }

  public void fail() {
    this.lockTable = new LockTable(); //set lock table to be new lock table - erases previous locks
    this.isDown = true;
  }

  public void update(Operation o, int time) {
    for (Variable v: this.variables) {
      if (v.number == o.variableID) {
        v.setValue(o.value);
      }
    }
    this.latestCommitTime=time;
  }
}
