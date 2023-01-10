package org.tetris;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private static DataOutputStream out;
    private static DataInputStream in;
    public static void main(String[] args) {
        try {
            Socket client = new Socket("127.0.0.1", 8888);
            out = new DataOutputStream(client.getOutputStream());
            in = new DataInputStream(client.getInputStream());
            client.setSoTimeout(100);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Thread thread = new Thread(MusicPlayer::play);
        thread.start();
        Tetris.startLoginBoard();
    }
    public static void reconnect() {
        try {
            Socket client = new Socket("127.0.0.1", 8888);
            out = new DataOutputStream(client.getOutputStream());
            in = new DataInputStream(client.getInputStream());
            client.setSoTimeout(100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendGarbageLine(int num) {
        try {
            if (num == 0)
                return;
            String msg = "GarbageLine: " + num;
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendGameOver(int score) {
        try {
            String msg = "GameOver: " + score;
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendSpace() {
        try {
            String msg = "Space: "
                    + Arrays.deepToString(GameWindow.space)
                    + ": " + Arrays.deepToString(GameWindow.colorSpace);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void CreateRoom() {
        try {
            String msg = "CreateRoom";
            GameWindow.isPlayerOne = true;
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void JoinRoom(int roomNumber) {
        try {
            String msg = "JoinRoom: " + roomNumber;
            GameWindow.isPlayerOne = false;
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendUsername(String username) {
        try {
            String msg = "Username: " + username;
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendGameDone() {
        try {
            String msg = "GameDone";
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void Exit() {
        try {
            String msg = "QuitServer";
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getMsg() throws IOException {
        try {
            return in.readUTF();
        } catch (IOException e) {
            return "Error";
        }
    }
}
