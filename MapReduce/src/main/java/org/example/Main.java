package org.example;

import org.example.coordinator.Coordinator;
import org.example.impl.WordCountMapper;
import org.example.impl.WordCountReducer;
import org.example.worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<String> inputFiles = List.of("MapReduce/src/main/resources/input1.txt","MapReduce/src/main/resources/input2.txt");
    int numReduceTasks = 3;
    int numWorkers = 4;

    Coordinator coordinator = new Coordinator(inputFiles, numReduceTasks);

    List<Thread> workers = new ArrayList<>();
    for (int i = 0; i < numWorkers; i++) {
      Worker worker = new Worker(i, coordinator, new WordCountMapper(), new WordCountReducer());
      Thread thread = new Thread(worker, "Worker-" + i);
      thread.start();
      workers.add(thread);
    }

    for (Thread t : workers) {
      try {
        t.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.println("Главный поток прерван, завершаем");
        return;
      }
    }

    System.out.println("MapReduce завершён");
    coordinator.shutdown();
  }
}