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
  private final Queue<MapTask> mapTasks = new LinkedList<>();
  private final Queue<ReduceTask> reduceTasks = new LinkedList<>();
  private final Set<Integer> completedMaps = ConcurrentHashMap.newKeySet();
  private final Set<Integer> completedReduces = ConcurrentHashMap.newKeySet();

  private final int numReduceTasks;

  public Coordinator(List<String> inputFiles, int numReduceTasks) {
    this.numReduceTasks = numReduceTasks;
    int taskId = 0;
    for (String file : inputFiles) {
      mapTasks.add(new MapTask(taskId++, file, numReduceTasks));
    }
  }

  public synchronized Task requestTask() {
    if (!mapTasks.isEmpty()) return mapTasks.poll();
    if (mapTasks.isEmpty() && completedMaps.size() == mapTasks.size()) {
      if (reduceTasks.isEmpty()) {
        for (int i = 0; i < numReduceTasks; i++) {
          List<String> files = getIntermediateFiles(i);
          reduceTasks.add(new ReduceTask(i, files));
        }
      }
      return reduceTasks.poll();
    }
    return null;
  }

  public synchronized void notifyMapDone(int taskId) {
    completedMaps.add(taskId);
  }

  public synchronized void notifyReduceDone(int reduceId) {
    completedReduces.add(reduceId);
  }

  private List<String> getIntermediateFiles(int reduceId) {
    List<String> files = new ArrayList<>();
    for (int i = 0; i < completedMaps.size(); i++) {
      files.add("mr-" + i + "-" + reduceId);
    }
    return files;
  }

  public boolean isJobDone() {
    return completedReduces.size() == numReduceTasks;
  }
}
