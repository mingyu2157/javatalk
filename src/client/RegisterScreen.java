package Client;

import Server.JDBCConnector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class RegisterScreen {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterScreen::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("회원가입");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel, frame);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("UserName:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton registerButton = new JButton("회원가입하기");
        registerButton.setBounds(100, 80, 100, 25);
        panel.add(registerButton);

        registerButton.addActionListener((ActionEvent e) -> {
            String userName = userText.getText();
            String password = new String(passwordText.getPassword());

            if (!userName.isEmpty() && !password.isEmpty()) {
                try {
                    Socket socket = ClientEx.getSocket();
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    System.out.println("[DEBUG] 서버로 REGISTER 명령 전송: " + userName + ", " + password); // 디버깅 추가
                    out.println("REGISTER " + userName + " " + password); //REGISTER 이름 패스워드 를 서버로 보냄

                    String response = in.readLine();
                    System.out.println("[DEBUG] 서버 응답: " + response); // 디버깅 추가

                    if ("SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(null, "Registration successful!");
                        frame.dispose();
                        LoginScreen.main(null); // 로그인 화면으로 전환
                    } else {
                        JOptionPane.showMessageDialog(null, "Registration failed: " + response);
                    }
                } catch (IOException ex) {
                    System.out.println("[ERROR] 서버와의 연결 오류: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "모든 필드를 채워주세요.");
            }
        });
    }
}