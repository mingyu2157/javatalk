package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CalculatorApp {
    public void createAndShowGUI() {
        JFrame frame = new JFrame("계산기");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setVisible(true);
        JPanel formulaPanel = new JPanel();
        formulaPanel.setBackground(Color.LIGHT_GRAY);
        formulaPanel.setLayout(new BorderLayout());

        JLabel formulaLabel = new JLabel("수식:");
        formulaLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        formulaLabel.setHorizontalAlignment(SwingConstants.LEFT);
        formulaPanel.add(formulaLabel, BorderLayout.WEST);

        JTextField formulaField = new JTextField();
        formulaField.setHorizontalAlignment(SwingConstants.RIGHT);
        formulaField.setEditable(true);
        formulaField.setBackground(Color.WHITE);
        formulaField.setPreferredSize(new Dimension(300, 30));

        JPanel formulaFieldPanel = new JPanel();
        formulaFieldPanel.setBackground(Color.LIGHT_GRAY);
        formulaFieldPanel.add(formulaField);
        formulaPanel.add(formulaFieldPanel, BorderLayout.CENTER);
        frame.add(formulaPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4, 5, 5));

        String[] buttons = {
                "C", "UN", "BK", "/",
                "7", "8", "9", "x",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "0", ".", "=", "%"
        };

        JTextField resultField = new JTextField();
        resultField.setHorizontalAlignment(SwingConstants.RIGHT);
        resultField.setEditable(false);
        resultField.setBackground(Color.WHITE);
        resultField.setPreferredSize(new Dimension(300, 30));

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 18));

            if (text.equals("=")) {
                button.setBackground(Color.GREEN);
            }

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String currentText = formulaField.getText();
                    switch (text) {
                        case "C":
                            formulaField.setText("");
                            resultField.setText(""); // 결과 초기화
                            break;
                        case "UN":
                            if (!currentText.isEmpty()) {
                                formulaField.setText(currentText.substring(0, currentText.length() - 1));
                            }
                            break;
                        case "BK":
                            break; // BK 버튼은 작동하지 않음
                        case "=":
                            try {
                                if (containsMultipleOperators(currentText)) {
                                    resultField.setText("연산은 한 번에 하나만 가능합니다.");
                                } else {
                                    String result = evaluateExpression(currentText);
                                    resultField.setText(result); // 계산 결과를 resultField에 표시
                                    formulaField.setText(""); // 새로운 수식을 입력할 수 있도록 초기화
                                }
                            } catch (ArithmeticException ex) {
                                resultField.setText("0으로 나눌 수 없습니다.");
                            } catch (Exception ex) {
                                resultField.setText("잘못된 수식");
                            }
                            break;
                        default:
                            formulaField.setText(currentText + text);
                            break;
                    }
                }
            });

            buttonPanel.add(button);
        }

        frame.add(buttonPanel, BorderLayout.CENTER);

        JPanel resultPanel = new JPanel();
        resultPanel.setBackground(Color.YELLOW);
        resultPanel.setLayout(new BorderLayout());

        JLabel resultLabel = new JLabel("계산 결과:");
        resultLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        resultLabel.setHorizontalAlignment(SwingConstants.LEFT);
        resultPanel.add(resultLabel, BorderLayout.WEST);

        JPanel resultFieldPanel = new JPanel();
        resultFieldPanel.setBackground(Color.YELLOW);
        resultFieldPanel.add(resultField);
        resultPanel.add(resultFieldPanel, BorderLayout.CENTER);
        frame.add(resultPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static String evaluateExpression(String expression) throws Exception {
        expression = expression.replace("x", "*");

        if (expression.contains("/0")) {
            throw new ArithmeticException("Division by zero");
        }

        return Double.toString(simpleCalculate(expression));
    }

    public static boolean containsMultipleOperators(String expression) {
        int operatorCount = 0;
        for (char c : expression.toCharArray()) {
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                operatorCount++;
            }
            if (operatorCount > 1) {
                return true;
            }
        }
        return false;
    }

    public static double simpleCalculate(String expression) {
        String[] operators = {"+", "-", "*", "/"};
        for (String operator : operators) {
            int index = expression.lastIndexOf(operator);
            if (index != -1) {
                double left = Double.parseDouble(expression.substring(0, index));
                double right = Double.parseDouble(expression.substring(index + 1));
                return switch (operator) {
                    case "+" -> left + right;
                    case "-" -> left - right;
                    case "*" -> left * right;
                    case "/" -> {
                        if (right == 0) throw new ArithmeticException("0으로 나눌 수 없습니다.");
                        yield left / right;
                    }
                    default -> throw new IllegalArgumentException("잘못된 연산자");
                };
            }
        }
        return Double.parseDouble(expression);
    }
}

