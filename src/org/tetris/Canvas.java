package org.tetris;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

import static org.tetris.GameWindow.BLOCK_SIZE;

class Canvas extends JComponent {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean isFirstHold;
    private int holdIndex = -1;
    private int[] nextShapeIndex;
    private Color getColor(int shapeIndex) {
        Color color;
        switch (shapeIndex) {
            case 0 -> color = Color.YELLOW;
            case 1 -> color = Color.CYAN;
            case 2 -> color = Color.RED;
            case 3 -> color = Color.GREEN;
            case 4 -> color = Color.ORANGE;
            case 5 -> color = Color.BLUE;
            case 6 -> color = Color.MAGENTA;
            default -> color = Color.DARK_GRAY;
        }
        return color;
    }
    // hold 区方块相关内容
    public void setIsFirstHold(boolean isFirstHold) {
        this.isFirstHold = isFirstHold;
    }
    public void setHoldIndex(int holdIndex) {
        this.holdIndex = holdIndex;
    }
    // next 区方块相关内容
    public void setNextShapeIndex(int[] nextShapeIndex) {
        this.nextShapeIndex = nextShapeIndex;
    }
    // 画出整个界面
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // 主游戏区
        for (int i = 0; i < GameWindow.VERTICAL_NODES; i++) {
            for (int j = 0; j < GameWindow.HORIZON_NODES; j++) {
                if (GameWindow.space[i][j] != 0) {
                    Color color = getColor(GameWindow.colorSpace[i][j]);
                    if (GameWindow.space[i][j] == 3) {
                        color = Color.GRAY;
                    }
                    g2.setColor(color);
                    g2.fillRect(j * BLOCK_SIZE + GameWindow.OFFSET, i * BLOCK_SIZE + 5, BLOCK_SIZE, BLOCK_SIZE);
                }
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRect(j * BLOCK_SIZE + GameWindow.OFFSET, i * BLOCK_SIZE + 5, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        // hold 区
        g2.drawRect(0, 0, 100, 100);
        if (holdIndex >= 0) {
            Color color = isFirstHold ? getColor(holdIndex) : Color.LIGHT_GRAY;
            drawShape(g2, GameWindow.shapes[holdIndex], 40, 35, BLOCK_SIZE, color);
        }
        // next 区
        if (nextShapeIndex != null && nextShapeIndex.length == 6) {
            for (int i = 0; i < 6; i++) {
                int[][] shape = GameWindow.shapes[nextShapeIndex[i]];
                drawShape(g2, shape, i * 60 + 30, 360, BLOCK_SIZE - 5, getColor(nextShapeIndex[i]));
            }
        }
        // 对手区
        for (int i = 0; i < GameWindow.VERTICAL_NODES; i++) {
            for (int j = 0; j < GameWindow.HORIZON_NODES; j++) {
                if (GameWindow.otherSpace[i][j] != 0) {
                    Color color = getColor(GameWindow.otherColorSpace[i][j]);
                    if (GameWindow.otherSpace[i][j] == 3) {
                        color = Color.GRAY;
                    }
                    g2.setColor(color);
                    g2.fillRect(j * BLOCK_SIZE + GameWindow.OFFSET + 400, i * BLOCK_SIZE + 5, BLOCK_SIZE, BLOCK_SIZE);
                }
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRect(j * BLOCK_SIZE + GameWindow.OFFSET + 400, i * BLOCK_SIZE + 5, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        // 得分/发送垃圾行/用户名/胜负数量
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        g2.drawString("Score", 10, 150);
        g2.drawString(String.valueOf(GameWindow.score), 30, 170);
        g2.drawString("Send", 10, 200);
        g2.drawString("Garbage", 20, 220);
        g2.drawString(String.valueOf(GameWindow.sendGarbageCount), 30, 240);
        g2.drawString("Username", 10, 270);
        g2.drawString(GameWindow.username, 30, 290);
        g2.drawString("Win", 10, 320);
        g2.drawString(String.valueOf(GameWindow.winCount), 30, 340);
        g2.drawString("Lose", 10, 370);
        g2.drawString(String.valueOf(GameWindow.loseCount), 30, 390);
        // 对方用户名/胜负数量
        g2.drawString("Opponent", 420, 150);
        g2.drawString(GameWindow.otherUsername, 440, 180);
        g2.drawString("Win", 420, 210);
        g2.drawString(String.valueOf(GameWindow.otherWinCount), 440, 230);
        g2.drawString("Lose", 420, 260);
        g2.drawString(String.valueOf(GameWindow.otherLoseCount), 440, 280);
    }
    private void drawShape(Graphics2D g2, int[][] holdShape, int offsetY, int offsetX, int size, Color color) {
        for (int[] ints : holdShape) {
            g2.setColor(color);
            g2.fillRect(ints[0] * size + offsetX, ints[1] * size + offsetY, size, size);
        }
        g2.setColor(Color.GRAY);
        for (int[] ints : holdShape) {
            g2.drawRect(ints[0] * size + offsetX, ints[1] * size + offsetY, size, size);
        }
    }
}
