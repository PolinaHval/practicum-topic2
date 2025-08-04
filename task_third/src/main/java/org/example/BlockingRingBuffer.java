package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingRingBuffer<T> {
  private final Object[] buffer;
  private int head = 0;
  private int tail = 0;
  private int count = 0;

  private final ReentrantLock lock = new ReentrantLock();
  private final Condition notEmpty = lock.newCondition();
  private final Condition notFull = lock.newCondition();

  public BlockingRingBuffer(int size) {
    this.buffer = new Object[size];
  }

  public void put(T item) throws InterruptedException {
    lock.lock();
    try {
      while (count == buffer.length) {
        notFull.await();
      }

      buffer[tail] = item;
      tail = (tail + 1) % buffer.length;
      count++;
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public T take() throws InterruptedException {
    lock.lock();
    try {
      while (count == 0) {
        notEmpty.await();
      }

      T item = (T) buffer[head];
      buffer[head] = null;
      head = (head + 1) % buffer.length;
      count--;
      notFull.signal();
      return item;
    } finally {
      lock.unlock();
    }
  }
}
