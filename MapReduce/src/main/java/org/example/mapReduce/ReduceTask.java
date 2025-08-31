package org.example.mapReduce;

import java.util.List;

public class ReduceTask extends Task {
  public final int reduceId;
  public final List<String> inputFiles;

  public ReduceTask(int reduceId, List<String> inputFiles) {
    this.reduceId = reduceId;
    this.inputFiles = inputFiles;
  }
}
