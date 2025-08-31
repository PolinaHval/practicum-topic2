package org.example.mapReduce;


public class MapTask extends Task {
  public final int taskId;
  public final String fileName;
  public final int numReduceTasks;

  public MapTask(int taskId, String fileName, int numReduceTasks) {
    this.taskId = taskId;
    this.fileName = fileName;
    this.numReduceTasks = numReduceTasks;
  }
}
