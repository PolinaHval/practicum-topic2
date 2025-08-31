package org.example.worker;

import org.example.coordinator.Coordinator;
import org.example.mapReduce.KeyValue;
import org.example.mapReduce.MapTask;
import org.example.mapReduce.Mapper;
import org.example.mapReduce.ReduceTask;
import org.example.mapReduce.Reducer;
import org.example.mapReduce.Task;
import org.example.util.FileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker implements Runnable{

  private final int id;
  private final Coordinator coordinator;
  private final Mapper mapper;
  private final Reducer reducer;

  public Worker(int id, Coordinator coordinator, Mapper mapper, Reducer reducer) {
    this.id = id;
    this.coordinator = coordinator;
    this.mapper = mapper;
    this.reducer = reducer;
  }

  @Override
  public void run() {
    while (true) {
      Task task = coordinator.requestTask();

      if (task == null) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
        if (coordinator.isJobDone()) break;
        continue;
      }

      try {
        if (task instanceof MapTask mapTask) {
          handleMapTask(mapTask);
          coordinator.notifyMapDone(mapTask.taskId);
        } else if (task instanceof ReduceTask reduceTask) {
          handleReduceTask(reduceTask);
          coordinator.notifyReduceDone(reduceTask.reduceId);
        }
      } catch (Exception e) {
        System.err.println("Worker " + id + " ошибка при выполнении задачи: " + e.getMessage());
      }
    }
  }

  private void handleMapTask(MapTask task) {
    String content = FileUtil.readFile(task.fileName);
    List<KeyValue> kvs = mapper.map(task.fileName, content);

    List<List<KeyValue>> buckets = new ArrayList<>();
    for (int i = 0; i < task.numReduceTasks; i++) buckets.add(new ArrayList<>());

    for (KeyValue kv : kvs) {
      int bucket = Math.abs(kv.key.hashCode()) % task.numReduceTasks;
      buckets.get(bucket).add(kv);
    }

    for (int i = 0; i < task.numReduceTasks; i++) {
      String filename = "mr-" + task.taskId + "-" + i;
      FileUtil.writeKeyValueList(filename, buckets.get(i));
    }
  }

  private void handleReduceTask(ReduceTask task) {
    List<KeyValue> all = new ArrayList<>();
    for (String file : task.inputFiles) {
      all.addAll(FileUtil.readKeyValueList(file));
    }

    Map<String, List<String>> grouped = new HashMap<>();
    for (KeyValue kv : all) {
      grouped.computeIfAbsent(kv.key, k -> new ArrayList<>()).add(kv.value);
    }

    List<String> outputLines = new ArrayList<>();
    for (String key : grouped.keySet().stream().sorted().toList()) {
      String reduced = reducer.reduce(key, grouped.get(key));
      outputLines.add(key + " " + reduced);
    }

    FileUtil.writeLines("result-" + task.reduceId, outputLines);
  }
}
