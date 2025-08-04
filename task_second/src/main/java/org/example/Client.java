package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
  public static void main(String[] args) {

    try (Socket socket = new Socket("localhost", 11111);
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
         BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

      String userInput;
      while ((userInput = console.readLine()) != null) {
        out.write(userInput + "\n");
        out.flush();

        String response = in.readLine();
        System.out.println("Ответ: " + response);

        if ("exit".equalsIgnoreCase(userInput)) {
          break;
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
