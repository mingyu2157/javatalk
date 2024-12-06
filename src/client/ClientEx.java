package Client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientEx {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("채팅방 목록");
    private JTextArea messageArea = new JTextArea(20, 60);
    private JTextField inputField = new JTextField(50);
    private String currentRoom = "";
    private String userName;
    private static Socket socket; // 인스턴스 변수로 Socket 선언

    public ClientEx(String userName) {
        this.userName = userName;

        try {
            // 전역 Socket 객체 초기화
            if (socket == null || socket.isClosed()) {
                socket = new Socket("localhost", 9999); // 서버 주소와 포트
            }
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            out.println(userName);

            showRoomSelection();

            // 메시지 입력 처리
            inputField.addActionListener(e -> {
                String message = inputField.getText();
                if (message.isEmpty()) return;
                out.println(message);
                inputField.setText("");
            });

            // 서버 메시지 수신
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("[채팅내역을 서버로부터 받는 디버그] 서버로부터 수신한 메시지: " + message);
                        String finalMessage = message;
                        SwingUtilities.invokeLater(() -> {
                            if (finalMessage.equals("=========================")) {
//                                messageArea.append("[여기까지 읽었습니다.]\n");
                            } else {
                                messageArea.append(finalMessage + "\n");
                            }
                            System.out.println("[DEBUG] messageArea 업데이트 완료"); // 업데이트 확인
                        });
                    }
                } catch (IOException e) {
                    System.out.println("[ERROR] 메시지 수신 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "서버에 연결할 수 없습니다.");
        }
    }
    public static Socket getSocket() {
        return socket;
    }

    private void showRoomSelection() {
        frame.setTitle("채팅방 목록");
        try {
            List<String> roomNames = new ArrayList<>();
            String line;

            // 서버로부터 채팅방 목록 수신
            while (!(line = in.readLine()).equals("END")) {
                roomNames.add(line);
            }

            // 채팅방 목록 GUI
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel roomListLabel = new JLabel("속한 채팅방 목록 :");
            roomListLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(roomListLabel);

            // 기존에 중복 생성된 GUI 제거 방지
            frame.getContentPane().removeAll();

            for (String roomName : roomNames) {
                JButton roomButton = new JButton(roomName);
                roomButton.addActionListener(e -> {
                    if (!currentRoom.equals(roomName)) { // 현재 채팅방과 동일하면 중복 처리 방지
                        out.println("JOIN " + roomName);
                        currentRoom = roomName;
                        openChatWindow(roomName);
                    }
                });
                panel.add(roomButton);
            }

            JButton joinRoomButton = new JButton("채팅방 입장");
            JButton randomChatButton = new JButton("랜덤 채팅");

            joinRoomButton.addActionListener(e -> {
                String roomName = JOptionPane.showInputDialog(frame, "채팅방 이름을 입력하세요:", "채팅방 입장", JOptionPane.PLAIN_MESSAGE);
                if (roomName != null && !roomName.trim().isEmpty()) {
                    out.println("JOIN " + roomName);
                    currentRoom = roomName;
                    openChatWindow(roomName);
                }
            });

            randomChatButton.addActionListener(e -> {
                out.println("RANDOM");
                currentRoom = "랜덤채팅";
                openChatWindow("랜덤채팅");
            });

            panel.add(joinRoomButton);
            panel.add(randomChatButton);

            frame.getContentPane().add(new JScrollPane(panel));
            frame.setSize(400, 500); // 크기 조정
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openChatWindow(String roomName) {
        frame.setTitle(roomName);
        frame.getContentPane().removeAll();
        messageArea.setText("");
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton leaveButton = new JButton("채팅방 나가기");

        // 랜덤 채팅방에서는 뒤로가기 버튼을 표시하지 않음
        if (!roomName.equals("랜덤채팅")) {
            JButton backButton = new JButton("뒤로가기");
            backButton.addActionListener(e -> {
                out.println("BACK");
                currentRoom = "";
                showRoomSelection();
            });
            bottomPanel.add(backButton, BorderLayout.WEST);
        }

        leaveButton.addActionListener(e -> {
            out.println("LEAVE " + currentRoom); // 현재 방 이름을 사용하여 LEAVE 명령 전송
            currentRoom = ""; // 현재 방 초기화
            showRoomSelection(); // 채팅방 목록 화면 표시
        });

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(leaveButton, BorderLayout.EAST);

        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientEx("테스트 사용자"));
//        SwingUtilities.invokeLater(() -> new LoginScreen().createAndShowGUI()); // LoginScreen 실행
    }
}