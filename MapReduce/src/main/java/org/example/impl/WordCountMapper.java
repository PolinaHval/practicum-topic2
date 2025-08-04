package org.example.impl;

import org.example.mapReduce.KeyValue;
import org.example.mapReduce.Mapper;

import java.util.ArrayList;
import java.util.List;

public class WordCountMapper implements Mapper {
  @Override
  public List<KeyValue> map(String fileName, String content) {
    List<KeyValue> result = new ArrayList<>();
    String[] words = content.split("\\W+");
    for (String word : words) {
      if (!word.isEmpty()) {
        result.add(new KeyValue(word.toLowerCase(), "1"));
      }
    }
    return result;
  }
}
