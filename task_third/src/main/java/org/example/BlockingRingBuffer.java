package org.example;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingRingBuffer<T> {
  private final Object[] buffer;
  private int head = 0;
  private int tail = 0;
  private int count = 0;
  private boolean closed = false;

  private final ReentrantLock lock = new ReentrantLock();
  private final Condition notEmpty = lock.newCondition();
  private final Condition notFull = lock.newCondition();

  public BlockingRingBuffer(int size) {
    if (size <= 0) throw new IllegalArgumentException("Размер должен быть положительным");
    this.buffer = new Object[size];
  }

  public void put(T item) throws InterruptedException {
    lock.lock();
    try {
      while (count == buffer.length && !closed) {
        notFull.await();
      }

      if (closed) throw new IllegalStateException("Buffer закрыт");

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
        if (closed) return null;
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

  public boolean isEmpty() {
    lock.lock();
    try {
      return count == 0;
    } finally {
      lock.unlock();
    }
  }

  public boolean isFull() {
    lock.lock();
    try {
      return count == buffer.length;
    } finally {
      lock.unlock();
    }
  }

  public void clear() {
    lock.lock();
    try {
      Arrays.fill(buffer, null);
      head = 0;
      tail = 0;
      count = 0;
      notFull.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public void close() {
    lock.lock();
    try {
      closed = true;
      notEmpty.signalAll();
      notFull.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public boolean isClosed() {
    lock.lock();
    try {
      return closed;
    } finally {
      lock.unlock();
    }
  }
}
