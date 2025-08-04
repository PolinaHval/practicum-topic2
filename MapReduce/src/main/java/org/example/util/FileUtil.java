package org.example.util;

import org.example.mapReduce.KeyValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
  public static String readFile(String fileName) {
    try {
      return Files.readString(Path.of(fileName));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeKeyValueList(String fileName, List<KeyValue> list) {
    try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
      for (KeyValue kv : list) {
        writer.write(kv.key + " " + kv.value);
        writer.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<KeyValue> readKeyValueList(String fileName) {
    List<KeyValue> result = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(" ");
        result.add(new KeyValue(parts[0], parts[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public static void writeLines(String fileName, List<String> lines) {
    try {
      Files.write(Path.of(fileName), lines);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
