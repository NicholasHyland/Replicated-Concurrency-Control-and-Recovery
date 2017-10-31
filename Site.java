// This is a class to model Sites
import java.util.ArrayList;

public class Site {

  boolean isDown;
  int number;
  // has independent lock table
  ArrayList<Variable> variables;

  public Site(int number) {
    this.isDown = false;
    this.number = number;

    //initialize variables at this site
    //if this is an even numbered site
    if (number%2==0){
      int i=1;
      while (i<21){
        //add the 2 odd variables and all even variables
        if ((i%10+1)==number || (i%2)==0){
	         this.variables.add(new Variable(i,(10*i)));	  
        }
        i++;
      }
    }
    //if this is an odd numbered site add only the even variables 
    else {
      int i=2;
      while (i<21){
        this.variables.add(new Variable(i,(10*i)));    
        i+=2;
      }
    }
  }
}
