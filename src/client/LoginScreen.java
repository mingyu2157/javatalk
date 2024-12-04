package Client;

import Server.JDBCConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginScreen {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::createAndShowGUI);
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Login");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel, frame);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
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

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(180, 80, 80, 25);
        panel.add(registerButton);

        loginButton.addActionListener((ActionEvent e) -> {
            String userName = userText.getText();
            String password = new String(passwordText.getPassword());
            JDBCConnector connector = new JDBCConnector();

            if (connector.loginUser(userName, password)) {
                JOptionPane.showMessageDialog(null, "Login successful!");
                frame.dispose();
                new ClientEx(userName); //ClientEx에 사용자 이름 전달
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password!");
            }
        });

        registerButton.addActionListener((ActionEvent e) -> {
            frame.dispose();
            RegisterScreen.main(null); // 회원가입 화면으로 전환
        });
    }
}