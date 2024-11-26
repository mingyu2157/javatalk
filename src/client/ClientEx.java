package Client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientEx {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("채팅");
    private JTextArea messageArea = new JTextArea(15, 50);
    private JTextField inputField = new JTextField(50);

    public ClientEx() {
        try {
            Socket socket = new Socket("localhost", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // GUI 구성
            messageArea.setEditable(false);
            frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
            frame.getContentPane().add(inputField, BorderLayout.SOUTH);
            frame.pack();
            frame.setVisible(true);

            // 이름 입력 및 초기 선택
            String name = JOptionPane.showInputDialog(frame, "이름을 입력하세요:", "이름 설정", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) {
                name = "익명";
            }
            out.println(name);

            String choice = JOptionPane.showInputDialog(frame, "1: 채팅방 접속 | 2: 랜덤 채팅", "선택", JOptionPane.PLAIN_MESSAGE);
            if ("1".equals(choice)) {
                String roomName = JOptionPane.showInputDialog(frame, "채팅방 이름을 입력하세요:", "채팅방 설정", JOptionPane.PLAIN_MESSAGE);
                out.println("1");
                out.println(roomName);
            } else if ("2".equals(choice)) {
                out.println("2");
            }

            // 메시지 입력
            inputField.addActionListener(e -> {
                String message = inputField.getText();
                out.println(message);
                inputField.setText("");
                if (message.equalsIgnoreCase("bye")) {
                    System.exit(0);
                }
            });

            // 서버 메시지 수신
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        messageArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "서버에 연결할 수 없습니다.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientEx::new);
    }
}