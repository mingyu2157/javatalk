package Client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class PaintApp extends JFrame {
    private final DrawCanvas canvas;  // 그림을 그릴 캔버스
    private final PrintWriter out;    // 서버로 그림 데이터를 전송할 출력 스트림

    public PaintApp() {
        out = ClientEx.getOut();  // ClientEx에서 getOut() 메소드로 PrintWriter를 가져오세요.

        setTitle("그림판");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        canvas = new DrawCanvas();
        add(canvas, BorderLayout.CENTER);
        setLocationRelativeTo(null);  // 화면 중앙에 위치
    }

    // 캔버스 클래스 - 그림을 그릴 영역
    private class DrawCanvas extends JPanel {
        private Image image;
        private Graphics2D g2d;
        private int x, y, prevX, prevY;

        public DrawCanvas() {
            setPreferredSize(new Dimension(500, 500)); // 캔버스 크기 설정
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    prevX = e.getX();
                    prevY = e.getY();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    x = e.getX();
                    y = e.getY();
                    g2d.drawLine(prevX, prevY, x, y);
                    prevX = x;
                    prevY = y;
                    repaint();
                    sendDrawingCommand(prevX, prevY, x, y);  // 서버로 그림 명령 전송
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                image = createImage(getWidth(), getHeight()); // 이미지 크기 초기화
                g2d = (Graphics2D) image.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(new BasicStroke(2));
            }
            g.drawImage(image, 0, 0, null);
        }

        // 서버로 그림 명령을 전송하는 메소드
        private void sendDrawingCommand(int prevX, int prevY, int x, int y) {
            String drawingCommand = String.format("%d,%d,%d,%d", prevX, prevY, x, y);
            out.println("DRAW " + drawingCommand);  // 서버로 그림 명령 전송
        }
    }
}
