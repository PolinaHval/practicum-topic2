package org.example;

public class Main {
  private static final Object lock = new Object();
  private static int number = 0;
  private static int max = 20;
  private static boolean queue = true;

  public static void main(String[] args) {
    Thread firstThread = new Thread(Main::printEven);
    Thread secondThread = new Thread(Main::printOdd);

    firstThread.start();
    secondThread.start();
  }

  private static void printEven() {
    while (number <= max) {
      synchronized (lock) {
        while (!queue) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }

        if (number % 2 == 0) {
          System.out.println("Even: " + number);
          number++;
          queue = false;
          lock.notifyAll();
        }
      }
    }
  }

  private static void printOdd() {
    while (number <= max) {
      synchronized (lock) {
        while (queue) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }

        if (number % 2 != 0) {
          System.out.println("Odd: " + number);
          number++;
          queue = true;
          lock.notifyAll();
        }
      }
    }
  }
}