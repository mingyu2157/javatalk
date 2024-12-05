package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import Server.JDBCConnector;

public class ServerEx {
    private static Map<String, Set<ClientHandler>> rooms = new HashMap<>(); // 채팅방 목록
    private static Queue<ClientHandler> randomQueue = new LinkedList<>(); // 랜덤 채팅 대기열
    private static Map<String, List<String>> chatHistory = new HashMap<>(); // 채팅방 이름 -> 채팅 기록

    public static void main(String[] args) {
        int port = 9999;
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
        private String userName;
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

                // 데이터베이스 연결 확인
//                JDBCConnector connector = new JDBCConnector();

                System.out.println(userName + "님이 연결되었습니다.");

                // 채팅방 목록 전송
                sendRoomList();

                String command;
                while ((command = in.readLine()) != null) {
                    System.out.println("[DEBUG] 수신된 명령: " + command); // 명령 수신 디버깅
                    if (command.startsWith("REGISTER ")) {
                        System.out.println("[DEBUG] REGISTER 명령 처리 중...");
                        String[] parts = command.substring(9).split(" ");
                        String userName = parts[0];
                        String password = parts[1];

                        // 회원가입 처리
                        JDBCConnector connector = new JDBCConnector();
                        boolean success = connector.registerUser(userName, password);

                        if (success) {
                            System.out.println("[DEBUG] 회원가입 성공: " + userName);
                            out.println("SUCCESS");
                        } else {
                            System.out.println("[DEBUG] 회원가입 실패: " + userName);
                            out.println("FAIL");
                        }
                    } else {
                        System.out.println("[디버깅] 처리되지 않은 명령 : " + command);
                    }

                    if (command.startsWith("JOIN ")) {
                        joinRoom(command.substring(5).trim());
                    } else if (command.equals("RANDOM")) {
                        handleRandomChat();
                    } else if (command.equals("BACK")) {
                        roomName = null;
                        sendRoomList();
                    } else if (command.startsWith("LEAVE ")) {
                        leaveRoom(command.substring(6).trim());
                        sendRoomList();
                    } else {
                        broadcast(command);
                    }
                }
            } catch (IOException e) {
                System.out.println(userName + " 연결 종료: " + e.getMessage());
            } finally {
                leaveRoom(roomName);
                closeResources();
            }
        }

        private void sendRoomList() {
            synchronized (rooms) {
                out.println("");
                for (String room : rooms.keySet()) {
                    if (rooms.get(room).contains(this)) {
                        out.println(room);
                    }
                }
                out.println("END");
            }
        }

        private void joinRoom(String roomName) {
            if (roomName == null || roomName.trim().isEmpty()) {
                System.out.println("[조인룸] 유효하지 않은 채팅방 이름입니다.");
                return;
            }

            synchronized (rooms) {
                this.roomName = roomName;
                rooms.putIfAbsent(roomName, new HashSet<>());
                rooms.get(roomName).add(this);

                // 채팅 기록 전송
                synchronized (chatHistory) {
                    List<String> history = chatHistory.get(roomName);
                    System.out.println("[해시맵에서 내역을 가져오는 디버깅] " + roomName + " 채팅 기록 전송 준비");
                    if (history != null) {
                        for (String msg : history) {
                            System.out.println("[해시맵에서 내역을 가져오는 디버깅] 전송 메시지: " + msg); // 전송할 메시지 디버깅
                            out.println(msg); // 클라이언트로 기록 전송
                        }
                        out.println("========================="); // 기록 종료
                        System.out.println("[해시맵에서 내역을 가져오는 디버깅] 채팅 기록 클라이언트로 전송 완료");
                    } else {
                        out.println("[해시맵에서 내역을 가져오는 디버깅] " + roomName + " 채팅 기록 없음");
                        out.println("=========================");
                    }
                }
            }
            broadcast("[알림] " + userName + "님이 입장하셨습니다.");
        }

        private void leaveRoom(String roomName) {
            if (roomName != null) {
                System.out.println("[리브룸] " + userName + "님이 " + roomName + " 방을 나가려고 합니다.");
                synchronized (rooms) {
                    Set<ClientHandler> room = rooms.get(roomName);
                    if (room != null) {
                        room.remove(this);
                        System.out.println("[리브룸] " + userName + "님이 " + roomName + " 방에서 제거되었습니다.");
                        if (room.isEmpty()) {
                            rooms.remove(roomName);
                            System.out.println("[리브룸] 방이 비어 삭제되었습니다: " + roomName);
                        } else {
                            broadcast("[알림] " + userName + "님이 퇴장하셨습니다.");
                        }
                    } else {
                        System.out.println("[리브룸] 방을 찾을 수 없습니다: " + roomName);
                    }
                }
                this.roomName = null; // 현재 방 이름 초기화
                System.out.println("[리브룸] 채팅방 목록에서 제거 완료: " + userName);
            } else {
                System.out.println("[리브룸] roomName이 null입니다. 아무 작업도 수행되지 않았습니다.");
            }
        }


        private void broadcast(String message) {
            synchronized (rooms) {
                System.out.println("[브로드캐스트] 전송 문자: " + message); // 방송 시작 확인
                Set<ClientHandler> room = rooms.get(roomName);
                if (room != null) {
                    // 채팅 기록 저장
                    synchronized (chatHistory) {
                        chatHistory.putIfAbsent(roomName, new ArrayList<>());
                        chatHistory.get(roomName).add(userName + ": " + message); // 기록 저장

                        // 디버깅 코드: 채팅 기록 출력
                        System.out.println("[채팅내역] 현재 채팅방: " + roomName);
                        System.out.println("[채팅내역] 채팅 기록:");
                        for (String msg : chatHistory.get(roomName)) {
                            System.out.println(" - " + msg);
                        }
                    }
                    //메세지 전송
                    for (ClientHandler client : room) {
                        client.out.println(userName + ": " + message);
                        System.out.println("[브로드캐스트] 전송 대상: " + client.userName); // 대상 클라이언트 확인
                    }
                } else {
                    System.out.println("[브로드캐스트] 방이 존재하지 않음: " + roomName); // 방 상태 확인
                }
            }
        }

        private void handleRandomChat() throws IOException {
            System.out.println("[DEBUG] 랜덤 채팅 요청: " + userName);
            ClientHandler partner = null;

            synchronized (randomQueue) {
                if (randomQueue.isEmpty()) {
                    randomQueue.add(this);
                    System.out.println("[랜덤큐] 대기열에 추가: " + userName);
                    out.println("[알림] 랜덤 채팅 대기 중...");
                    while (randomQueue.contains(this)) {
                        try {
                            randomQueue.wait();
                        } catch (InterruptedException ignored) {}
                    }
                } else {
                    partner = randomQueue.poll();
                    System.out.println("[랜덤큐] 매칭 성공: " + userName + " <-> " + partner.userName);
                    randomQueue.notifyAll();

                    // 랜덤 채팅방 이름 생성 및 설정
                    String randomRoomName = "RandomRoom-" + UUID.randomUUID().toString().substring(0, 8);
                    synchronized (rooms) {
                        rooms.put(randomRoomName, new HashSet<>());
                        rooms.get(randomRoomName).add(this);
                        rooms.get(randomRoomName).add(partner);
                    }

                    this.roomName = randomRoomName;
                    partner.roomName = randomRoomName;

                    out.println("[알림] 랜덤 채팅이 생성되었습니다! 방 이름: " + randomRoomName);
                    partner.out.println("[알림] 랜덤 채팅이 생성되었습니다! 방 이름: " + randomRoomName);

                    // 매칭된 클라이언트와 채팅 시작
                    handleDirectChat(partner);
                }
            }
        }

        private void handleDirectChat(ClientHandler partner) {
            System.out.println("[DEBUG] 랜덤 채팅 시작: " + userName + " <-> " + partner.userName);

            Thread currentChatThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("[DEBUG] " + userName + " 메시지: " + message);
                        if (message.equalsIgnoreCase("bye")) {
                            out.println("[알림] 랜덤 채팅 종료.");
                            partner.out.println("[알림] 상대방이 랜덤 채팅을 종료했습니다.");
                            break;
                        }
                        partner.out.println(userName + ": " + message);
                        partner.out.flush();
                    }
                } catch (IOException e) {
                    System.out.println("[ERROR] " + userName + " 랜덤 채팅 중 연결 문제가 발생했습니다: " + e.getMessage());
                }
            });

            Thread partnerChatThread = new Thread(() -> {
                try {
                    String partnerMessage;
                    while ((partnerMessage = partner.in.readLine()) != null) {
                        System.out.println("[DEBUG] " + partner.userName + " 메시지: " + partnerMessage);
                        if (partnerMessage.equalsIgnoreCase("bye")) {
                            partner.out.println("[알림] 랜덤 채팅 종료.");
                            out.println("[알림] 상대방이 랜덤 채팅을 종료했습니다.");
                            break;
                        }
                        out.println(partner.userName + ": " + partnerMessage);
                        out.flush();
                    }
                } catch (IOException e) {
                    System.out.println("[ERROR] " + partner.userName + " 랜덤 채팅 중 연결 문제가 발생했습니다: " + e.getMessage());
                }
            });

            currentChatThread.start();
            partnerChatThread.start();
        }

        private void closeResources() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.out.println("자원 해제 중 오류: " + e.getMessage());
            }
        }
    }
}
