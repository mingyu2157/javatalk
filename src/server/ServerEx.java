package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerEx {
    private static final int PORT = 9999;
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("멀티클라이언트 서버가 실행 중입니다...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("새 클라이언트 연결됨: " + socket);
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("bye")) {
                        System.out.println("클라이언트 연결 종료됨: " + socket);
                        break;
                    }

                    System.out.println("클라이언트 메시지: " + message);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println("클라이언트: " + message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 처리 중 오류 발생: " + e.getMessage());
            } finally {
                try {
                    if (out != null) clientWriters.remove(out);
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    System.out.println("소켓 닫는 중 오류 발생: " + e.getMessage());
                }
            }
        }
    }
}
