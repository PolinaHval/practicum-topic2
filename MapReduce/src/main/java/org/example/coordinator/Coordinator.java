package org.example.coordinator;

import org.example.mapReduce.MapTask;
import org.example.mapReduce.ReduceTask;
import org.example.mapReduce.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Coordinator {
  private final Queue<TaskWrapper<MapTask>> mapTasks = new LinkedList<>();
  private final Queue<TaskWrapper<ReduceTask>> reduceTasks = new LinkedList<>();

  private final Set<Integer> completedMaps = ConcurrentHashMap.newKeySet();
  private final Set<Integer> completedReduces = ConcurrentHashMap.newKeySet();

  private final int numReduceTasks;
  private final int totalMapTasks;
  private final long taskTimeoutMs = 5000;

  public Coordinator(List<String> inputFiles, int numReduceTasks) {
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("Список файлов не может быть пустым");
    }
    if (numReduceTasks <= 0) {
      throw new IllegalArgumentException("Количество reduce-задач должно быть положительным");
    }

    this.numReduceTasks = numReduceTasks;
    this.totalMapTasks = inputFiles.size();

    int taskId = 0;
    for (String file : inputFiles) {
      mapTasks.add(new TaskWrapper<>(new MapTask(taskId++, file, numReduceTasks)));
    }
  }

  public synchronized Task requestTask() {
    checkTimeouts();

    for (TaskWrapper<MapTask> wrapper : mapTasks) {
      if (wrapper.status == TaskStatus.PENDING) {
        wrapper.status = TaskStatus.IN_PROGRESS;
        wrapper.timestamp = System.currentTimeMillis();
        return wrapper.task;
      }
    }

    if (completedMaps.size() == totalMapTasks && reduceTasks.isEmpty()) {
      for (int i = 0; i < numReduceTasks; i++) {
        List<String> files = getIntermediateFiles(i);
        reduceTasks.add(new TaskWrapper<>(new ReduceTask(i, files)));
      }
    }

    for (TaskWrapper<ReduceTask> wrapper : reduceTasks) {
      if (wrapper.status == TaskStatus.PENDING) {
        wrapper.status = TaskStatus.IN_PROGRESS;
        wrapper.timestamp = System.currentTimeMillis();
        return wrapper.task;
      }
    }

    return null;
  }

  public synchronized void notifyMapDone(int taskId) {
    completedMaps.add(taskId);
    mapTasks.stream()
        .filter(w -> w.task.taskId == taskId)
        .forEach(w -> w.status = TaskStatus.DONE);
  }

  public synchronized void notifyReduceDone(int reduceId) {
    completedReduces.add(reduceId);
    reduceTasks.stream()
        .filter(w -> w.task.reduceId == reduceId)
        .forEach(w -> w.status = TaskStatus.DONE);
  }

  private List<String> getIntermediateFiles(int reduceId) {
    List<String> files = new ArrayList<>();
    for (int i = 0; i < totalMapTasks; i++) {
      files.add("mr-" + i + "-" + reduceId);
    }
    return files;
  }

  private void checkTimeouts() {
    long now = System.currentTimeMillis();
    for (TaskWrapper<MapTask> w : mapTasks) {
      if (w.status == TaskStatus.IN_PROGRESS && now - w.timestamp > taskTimeoutMs) {
        w.status = TaskStatus.PENDING;
        w.timestamp = 0;
      }
    }
    for (TaskWrapper<ReduceTask> w : reduceTasks) {
      if (w.status == TaskStatus.IN_PROGRESS && now - w.timestamp > taskTimeoutMs) {
        w.status = TaskStatus.PENDING;
        w.timestamp = 0;
      }
    }
  }

  public boolean isJobDone() {
    return completedReduces.size() == numReduceTasks;
  }

  public void shutdown() {
    mapTasks.clear();
    reduceTasks.clear();
    completedMaps.clear();
    completedReduces.clear();
  }

  private enum TaskStatus {
    PENDING, IN_PROGRESS, DONE
  }

  private static class TaskWrapper<T extends Task> {
    final T task;
    TaskStatus status = TaskStatus.PENDING;
    long timestamp = 0;

    TaskWrapper(T task) {
      this.task = task;
    }
  }
}
