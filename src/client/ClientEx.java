package Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientEx {
    private BufferedReader in;
    private static PrintWriter out;
    private JFrame frame = new JFrame("채팅방 목록");
    private JTextArea messageArea = new JTextArea(20, 60);
    private JTextField inputField = new JTextField(50);
    private String currentRoom = "";
    private String username;
    private static Socket socket; // 인스턴스 변수로 Socket 선언



    // out을 설정하는 메서드
    public static void setOut(PrintWriter out) {
        ClientEx.out = out;
    }

    // out을 반환하는 메서드
    public static PrintWriter getOut() {
        return out;
    }

    public ClientEx(String username) {
        this.username = username;

        try {
            // 소켓 초기화 및 설정
            if (socket == null || socket.isClosed()) {
                socket = new Socket("localhost", 9999);
            }
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            out.println(username); // 사용자 이름 전송
            showRoomSelection();

            // 메시지 입력 처리
            inputField.addActionListener(e -> {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    new Thread(() -> out.println(message)).start();
                    inputField.setText("");
                }
            });

            // 서버 메시지 수신
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String finalMessage = message;
                        SwingUtilities.invokeLater(() -> {
                            if (finalMessage.equals("=========================")) {
                                // messageArea.append("[여기까지 읽었습니다.]\n");
                            } else {
                                messageArea.append(finalMessage + "\n");
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "서버에 연결할 수 없습니다.");
        }
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
                    if (!currentRoom.equals(roomName)) {
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
            frame.setSize(400, 500);
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openChatWindow(String roomName) {
        frame.setTitle(roomName);
        frame.getContentPane().removeAll();
        messageArea.setText(""); // 메시지 영역 초기화
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

        // 그림판 버튼 추가
        JButton paintButton = new JButton("그림판");
        paintButton.addActionListener(e -> {
            // 그림판을 위한 클래스를 호출 (예: PaintApp 클래스)
            new PaintApp().setVisible(true);
        });

        JButton calculatorButton = new JButton("계산기");
        calculatorButton.addActionListener(e -> {
            // 계산기를 위한 클래스를 호출 (예: CalculatorApp 클래스)
            new client.CalculatorApp().createAndShowGUI(); // 독립적인 계산기 창 열기
        });


        // 하단에 버튼 추가
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(leaveButton, BorderLayout.EAST);

        // 기존 채팅방 관련 버튼을 패널에 추가
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(paintButton);
        buttonPanel.add(calculatorButton);

        // 기존 버튼과 새 버튼을 모두 하단에 배치
        JPanel allButtonsPanel = new JPanel();
        allButtonsPanel.setLayout(new BoxLayout(allButtonsPanel, BoxLayout.Y_AXIS)); // 버튼을 세로로 배치
        allButtonsPanel.add(buttonPanel); // 그림판, 계산기 버튼
        allButtonsPanel.add(bottomPanel); // 기존 채팅방 나가기 버튼 및 입력 필드

        frame.getContentPane().add(allButtonsPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    public void sendDrawingCommand(String drawingCommand) {
        out.println("DRAW " + drawingCommand);  // 서버로 그림 명령 전송
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().createAndShowGUI()); // LoginScreen 실행
    }
}
