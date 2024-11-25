package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerEx {
    private static Map<String, Set<ClientHandler>> rooms = new HashMap<>();
    private static Queue<ClientHandler> randomQueue = new LinkedList<>();

    public static void main(String[] args) {
        int port = 9999; // 고정 포트
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("서버 실행 중입니다. 포트: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private String name;
        private String roomName;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                // 사용자 이름 입력
                out.println("이름을 입력하세요:");
                name = in.readLine();
                if (name == null || name.trim().isEmpty()) {
                    name = "익명";
                }

                while (true) {
                    out.println("1. 채팅방 접속 | 2. 랜덤 채팅");
                    String choice = in.readLine();
                    if (choice == null || choice.trim().isEmpty()) {
                        continue;
                    }

                    if (choice.equals("1")) {
                        out.println("채팅방 이름을 입력하세요:");
                        roomName = in.readLine();
                        if (roomName != null && !roomName.trim().isEmpty()) {
                            joinRoom(roomName);
                            break;
                        }
                    } else if (choice.equals("2")) {
                        handleRandomChat();
                        return;
                    }
                }

                // 방에서 메시지 처리
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("bye")) {
                        leaveRoom();
                        break;
                    }
                    broadcast(message);
                }
            } catch (IOException e) {
                System.out.println(name + " 연결 종료: " + e.getMessage());
            } finally {
                leaveRoom();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleRandomChat() throws IOException {
            synchronized (randomQueue) {
                if (randomQueue.isEmpty()) {
                    randomQueue.add(this);
                    out.println("랜덤 채팅 대기 중입니다...");
                    while (randomQueue.contains(this)) {
                        try {
                            randomQueue.wait();
                        } catch (InterruptedException ignored) {}
                    }
                } else {
                    ClientHandler partner = randomQueue.poll();
                    randomQueue.notifyAll();
                    out.println("랜덤 채팅 연결 완료!");
                    partner.out.println("랜덤 채팅 연결 완료!");
                    handleDirectChat(partner);
                }
            }
        }

        private void handleDirectChat(ClientHandler partner) throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("bye")) {
                    out.println("랜덤 채팅 종료");
                    partner.out.println("랜덤 채팅 종료");
                    break;
                }
                partner.out.println(name + ": " + message);
                out.println("나: " + message);
            }
        }

        private void joinRoom(String roomName) {
            synchronized (rooms) {
                rooms.putIfAbsent(roomName, new HashSet<>());
                rooms.get(roomName).add(this);
            }
            broadcast("[알림] " + name + "님이 입장하셨습니다.");
        }

        private void leaveRoom() {
            if (roomName != null) {
                synchronized (rooms) {
                    Set<ClientHandler> room = rooms.get(roomName);
                    if (room != null) {
                        room.remove(this);
                        if (room.isEmpty()) {
                            rooms.remove(roomName);
                        } else {
                            broadcast("[알림] " + name + "님이 퇴장하셨습니다.");
                        }
                    }
                }
            }
        }

        private void broadcast(String message) {
            synchronized (rooms) {
                Set<ClientHandler> room = rooms.get(roomName);
                if (room != null) {
                    for (ClientHandler client : room) {
                        client.out.println(name + ": " + message);
                        client.out.flush();
                    }
                }
            }
        }
    }
}
