package org.example.impl;

import org.example.mapReduce.Reducer;

import java.util.List;

public class WordCountReducer implements Reducer {
  @Override
  public String reduce(String key, List<String> values) {
    int sum = 0;
    for (String val : values) {
      sum += Integer.parseInt(val);
    }
    return String.valueOf(sum);
  }
}
