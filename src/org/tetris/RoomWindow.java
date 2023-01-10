package org.tetris;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serial;

public class RoomWindow extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    // 用于监听 Server 各种消息
    private final Timer timer;
    public RoomWindow() {
        JButton createRoom = new JButton("Create Room");
        JButton joinRoom = new JButton("Join Room");
        JButton exit = new JButton("Exit");
        JTextField roomNumber = new JTextField(10);
        JLabel roomNumberLabel = new JLabel("Room Number:");
        timer = new Timer(100, new TimerHandler());
        timer.start();
        createRoom.addActionListener(e -> {
            Client.CreateRoom();
        });
        joinRoom.addActionListener(e -> {
            if (roomNumber.getText().equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Please enter the room number",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Client.JoinRoom(Integer.parseInt(roomNumber.getText()));
                } catch (NumberFormatException numberFormatException) {
                    JOptionPane.showMessageDialog(null,
                            "Please enter the correct room number",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        exit.addActionListener(e -> {
            Client.Exit();
            System.exit(0);
        });
        createRoom.setBounds(50, 25, 200, 50);
        joinRoom.setBounds(50, 100, 200, 50);
        roomNumberLabel.setBounds(40, 155, 200, 20);
        roomNumber.setBounds(50, 175, 200, 25);
        exit.setBounds(50, 225, 200, 50);
        add(createRoom);
        add(joinRoom);
        add(exit);
        add(roomNumber);
        add(roomNumberLabel);
        add(new JLabel(""));
    }
    // 处理 Server 消息
    class TimerHandler implements ActionListener {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
                String msg = Client.getMsg();
                String[] msgArray = msg.split(": ");
                switch (msgArray[0]) {
                    case "CreateRoom" -> {
                        GameWindow.winCount = Database.getWinCount(GameWindow.username);
                        GameWindow.loseCount = Database.getLoseCount(GameWindow.username);
                        JOptionPane.showMessageDialog(null,
                                "Create room number " + msgArray[1] + " success, please wait",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        timer.stop();
                        dispose();
                        Tetris.startGameBoard();
                    }
                    case "JoinRoom" -> {
                        if (msgArray[1].equals("false")) {
                            JOptionPane.showMessageDialog(null,
                                    "The room does not exist or is full",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            GameWindow.winCount = Database.getWinCount(GameWindow.username);
                            GameWindow.loseCount = Database.getLoseCount(GameWindow.username);
                            timer.stop();
                            dispose();
                            Tetris.startGameBoard();
                        }
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
