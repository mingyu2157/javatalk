package Client;


import server.JDBCConnector;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class RegisterScreen {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterScreen::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Register");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(100, 80, 100, 25);
        panel.add(registerButton);

        registerButton.addActionListener((ActionEvent e) -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            JDBCConnector connector = new JDBCConnector();

            if (connector.registerUser(username, password)) {
                JOptionPane.showMessageDialog(null, "Registration successful!");
                frame.dispose();
                LoginScreen.main(null);
                // 회원가입 후 로그인 화면으로 전환
            } else {
                JOptionPane.showMessageDialog(null, "Registration failed. Try again.");
            }
        });
    }
}