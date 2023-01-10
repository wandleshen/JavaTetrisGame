package org.tetris;

import javax.swing.*;
import java.io.Serial;

public class LoginWindow extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    public LoginWindow() {
        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        JButton exit = new JButton("Exit");
        JTextField username = new JTextField(10);
        JTextField password = new JPasswordField(10);
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        login.addActionListener(e -> {
            if (username.getText().equals("") || password.getText().equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Please enter the username and password",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (Database.login(username.getText(), password.getText())) {
                    JOptionPane.showMessageDialog(null,
                            "Login successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    GameWindow.username = username.getText();
                    dispose();
                    Tetris.startRoomBoard();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Login failed",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        register.addActionListener(e -> {
            if (username.getText().equals("") || password.getText().equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Please enter the username and password",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (Database.register(username.getText(), password.getText())) {
                    JOptionPane.showMessageDialog(null,
                            "Register successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Register failed",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        exit.addActionListener(e -> {
            Client.Exit();
            System.exit(0);
        });
        login.setBounds(50, 130 , 200 , 25);
        register.setBounds(50, 160, 200, 25);
        usernameLabel.setBounds(40, 25, 200, 20);
        username.setBounds(50, 45, 200, 25);
        passwordLabel.setBounds(40, 75, 200, 20);
        password.setBounds(50, 95, 200, 25);
        exit.setBounds(50, 190, 200, 25);
        add(login);
        add(register);
        add(exit);
        add(username);
        add(password);
        add(usernameLabel);
        add(passwordLabel);
        add(new JLabel(""));
    }
}
