package org.tetris;

import java.awt.*;
import javax.swing.*;

public class Tetris {
    public static final int FRAME_WIDTH = 600;
    public static final int FRAME_HEIGHT = 420;
    public static final Image image = new ImageIcon("./resource/The_Tetris_Company_logo_2019.png").getImage();
    private static GameWindow gameWindow;
    private static RoomWindow roomWindow;
    private static LoginWindow loginWindow;
    public static void startGameBoard() {
        EventQueue.invokeLater(() -> {
            gameWindow = new GameWindow();
            gameWindow.setIconImage(image);
            gameWindow.setTitle("Tetris");
            gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameWindow.setLocationByPlatform(true);
            gameWindow.setSize(FRAME_WIDTH + 25 + GameWindow.OFFSET, FRAME_HEIGHT + 30);
            gameWindow.setResizable(false);
            gameWindow.setVisible(true);
        });
    }
    public static void startRoomBoard() {
        EventQueue.invokeLater(() -> {
            roomWindow = new RoomWindow();
            roomWindow.setIconImage(image);
            roomWindow.setTitle("Tetris");
            roomWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            roomWindow.setLocationByPlatform(true);
            roomWindow.setSize(310, 350);
            roomWindow.setResizable(false);
            roomWindow.setVisible(true);
        });
    }
    public static void startLoginBoard() {
        EventQueue.invokeLater(() -> {
            loginWindow = new LoginWindow();
            loginWindow.setIconImage(image);
            loginWindow.setTitle("Tetris");
            loginWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginWindow.setLocationByPlatform(true);
            loginWindow.setSize(310, 300);
            loginWindow.setResizable(false);
            loginWindow.setVisible(true);
        });
    }
}

