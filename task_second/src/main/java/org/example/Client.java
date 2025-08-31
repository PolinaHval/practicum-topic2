package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {
  public static void main(String[] args) {

    try (Socket socket = new Socket("localhost", 11111)) {

      socket.setSoTimeout(5000);

      try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
           BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

        String userInput;
        while ((userInput = console.readLine()) != null) {
          out.write(userInput + "\n");
          out.flush();

          try {
            String response = in.readLine();
            System.out.println("Ответ: " + response);
          } catch (SocketTimeoutException e) {
            System.err.println("Время ожидания ответа сервера истекло");
          }

          if ("exit".equalsIgnoreCase(userInput)) {
            break;
          }
        }

      }

    } catch (IOException e) {
      System.err.println("Ошибка при подключении или работе с сокетом: " + e.getMessage());
    }
  }
}
