package org.example;

public class Main {
  public static void main(String[] args) {
    BlockingRingBuffer<Integer> ringBuffer = new BlockingRingBuffer<>(5);

    Runnable producer = () -> {
      for (int i = 0; i < 20; i++) {
        try {
          ringBuffer.put(i);
          System.out.println("Produced: " + i);
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    };

    Runnable consumer = () -> {
      for (int i = 0; i < 20; i++) {
        try {
          Integer value = ringBuffer.take();
          System.out.println("Consumed: " + value);
          Thread.sleep(150);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    };

    new Thread(producer).start();
    new Thread(consumer).start();
  }
}