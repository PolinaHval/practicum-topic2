package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
  private static final int PORT = 11111;
  private static final int maxThread = 10;

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(maxThread);
    ServerSocket serverSocket = null;

    try {
      serverSocket = new ServerSocket(PORT);
      serverSocket.setSoTimeout(5000);
      System.out.println("Сервер запущен. Порт: " + PORT);

      while (!Thread.currentThread().isInterrupted()) {
        try {
          Socket clientSocket = serverSocket.accept();
          System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

          executorService.submit(() -> handleClient(clientSocket));

        } catch (SocketTimeoutException e) {
        }
      }

    } catch (IOException e) {
      System.err.println("Ошибка сервера: " + e.getMessage());
    } finally {
      if (serverSocket != null && !serverSocket.isClosed()) {
        try {
          serverSocket.close();
        } catch (IOException e) {
          System.err.println("Не удалось закрыть серверный сокет: " + e.getMessage());
        }
      }

      executorService.shutdown();
      try {
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
          executorService.shutdownNow();
        }
      } catch (InterruptedException e) {
        executorService.shutdownNow();
        Thread.currentThread().interrupt();
      }

      System.out.println("Сервер завершил работу");
    }
  }

  private static void handleClient(Socket socket) {
    BufferedReader in = null;
    BufferedWriter out = null;

    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

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
      System.err.println("Ошибка работы с клиентом: " + e.getMessage());
    } finally {
      try {
        if (in != null) in.close();
      } catch (IOException e) {
        System.err.println("Не удалось закрыть InputStream: " + e.getMessage());
      }

      try {
        if (out != null) out.close();
      } catch (IOException e) {
        System.err.println("Не удалось закрыть OutputStream: " + e.getMessage());
      }

      try {
        if (socket != null && !socket.isClosed()) socket.close();
      } catch (IOException e) {
        System.err.println("Не удалось закрыть сокет: " + e.getMessage());
      }
    }
  }
}