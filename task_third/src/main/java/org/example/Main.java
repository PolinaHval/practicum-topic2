package org.example;

public class Main {
  public static void main(String[] args) {
    BlockingRingBuffer<Integer> ringBuffer = new BlockingRingBuffer<>(5);

    Runnable producer = () -> {
      try {
        for (int i = 0; i < 20; i++) {
          ringBuffer.put(i);
          System.out.println("Produced: " + i);
          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    Runnable consumer = () -> {
      try {
        Integer value;
        while ((value = ringBuffer.take()) != null) {
          System.out.println("Consumed: " + value);
          Thread.sleep(150);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    Thread pThread = new Thread(producer);
    Thread cThread = new Thread(consumer);

    pThread.start();
    cThread.start();

    try {
      pThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    ringBuffer.close();
  }
}