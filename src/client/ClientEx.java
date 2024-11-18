package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientEx {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("클라이언트 채팅");
    private JTextArea messageArea = new JTextArea(15, 50);
    private JTextField inputField = new JTextField(50);
    private String userName; // 사용자 이름

    public ClientEx(String serverAddress) {
        // 사용자 이름 입력받기
        userName = JOptionPane.showInputDialog(frame, "사용자 이름을 입력하세요:", "이름 설정", JOptionPane.PLAIN_MESSAGE);
        if (userName == null || userName.trim().isEmpty()) {
            userName = "익명"; // 이름이 비어 있으면 기본값 설정
        }

        try {
            // 서버 연결
            Socket socket = new Socket(serverAddress, 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // 서버에 사용자 이름 전송
            out.println("[" + userName + "] 님이 입장하셨습니다.");

            // GUI 설정
            messageArea.setEditable(false);
            frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
            frame.getContentPane().add(inputField, BorderLayout.SOUTH);
            frame.pack();

            // 입력 필드 액션
            inputField.addActionListener(e -> {
                String message = inputField.getText();
                if (!message.trim().isEmpty()) {
                    out.println("[" + userName + "]: " + message); // 이름과 함께 메시지 전송
                }
                inputField.setText("");
                if (message.equalsIgnoreCase("bye")) {
                    System.exit(0); // 클라이언트 종료
                }
            });

            // 서버 메시지 수신 스레드
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        messageArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    System.out.println("서버와의 연결이 끊어졌습니다.");
                }
            }).start();
        } catch (IOException e) {
            System.out.println("서버에 연결할 수 없습니다: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String serverAddress = JOptionPane.showInputDialog(
                "서버 주소를 입력하세요 (기본값: localhost):", "localhost");
        ClientEx client = new ClientEx(serverAddress);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
    }
}
