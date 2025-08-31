package org.example;

public class Main {
  private static final Object lock = new Object();
  private static int number = 0;
  private static final int max = 21;
  private static boolean queue = true;

  public static void main(String[] args) {
    Thread firstThread = new Thread(Main::printEven);
    Thread secondThread = new Thread(Main::printOdd);

    firstThread.start();
    secondThread.start();

    try{
      firstThread.join();
      secondThread.join();
    } catch (InterruptedException e){
      Thread.currentThread().interrupt();
      System.err.println("Главный поток прерван: " + e.getMessage());
    }
  }

  private static void printEven() {
    while (true) {
      synchronized (lock) {
        while (!queue && number <= max) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("FirstThread прерван: " + e.getMessage());
            return;
          }
        }

        if (number > max) {
          lock.notifyAll();
          return;
        }

        System.out.println("Even: " + number);
        number++;
        queue = false;
        lock.notifyAll();
      }
    }
  }

  private static void printOdd() {
    while (true) {
      synchronized (lock) {
        while (queue && number <= max) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("SecondThread прерван: " + e.getMessage());
            return;
          }
        }

        if (number > max) {
          lock.notifyAll();
          return;
        }
          System.out.println("Odd: " + number);
          number++;
          queue = true;
          lock.notifyAll();
      }
    }
  }
}