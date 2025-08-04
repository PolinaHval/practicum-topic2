package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
  private static final int PORT = 11111;
  private static final int maxThread = 10;

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(maxThread);
    System.out.println("Сервер запущен. Порт: " + PORT);

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

        executorService.submit(() -> handleClient(clientSocket));
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      executorService.shutdown();
    }
  }

  private static void handleClient(Socket socket) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){

      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        System.out.println("Получено: " + inputLine);
        out.write("Echo: " + inputLine + "\n");
        out.flush();

        if ("exit".equalsIgnoreCase(inputLine)) {
          break;
        }
      }

      System.out.println("Клиент отключился: " + socket.getInetAddress());

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}