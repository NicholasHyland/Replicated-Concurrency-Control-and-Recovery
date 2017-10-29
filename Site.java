// This is a class to model Sites

public class Site {

  boolean isDown;
  int number;
  // has independent lock table

  public Site(int number) {
    this.isDown = false;
    this.number = number;
  }

}
