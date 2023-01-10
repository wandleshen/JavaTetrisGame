package org.tetris;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class Server {
    private final ServerSocket server;
    // 保存对战房间
    public static ArrayList<Sockets> rooms;
    public static int roomNumber = 0;
    public Server(int port) throws IOException {
        server = new ServerSocket(port);
        rooms = new ArrayList<>();
    }
    static class Sockets {
        Socket one;
        Socket two;
    }
    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for client on port " + server.getLocalPort() + "...");
                Socket server = this.server.accept();
                server.setSoTimeout(0);
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(server);
                Thread t = new Thread(handler);
                t.start();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    public static void main(String[] args) {
        try {
            Server t = new Server(8888);
            t.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket server;
    private int roomNumber = -1;
    private Socket opponent;
    Random random = new Random();
    public ClientHandler(Socket server) {
        this.server = server;
    }
    @Override
    public void run() {
        try {
            System.out.println("ClientHandler started!");
            DataInputStream in = new DataInputStream(server.getInputStream());
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            while (true) {
                // 确定对手，建立连接
                if (opponent == null
                        && roomNumber >= 0 && roomNumber < Server.rooms.size()
                        && Server.rooms.get(roomNumber) != null) {
                    opponent = Server.rooms.get(roomNumber).one == server
                            ? Server.rooms.get(roomNumber).two
                            : Server.rooms.get(roomNumber).one;
                }
                String msg = in.readUTF();
                String[] msgArray = msg.split(": ");
                if (!msgArray[0].equals("Space"))
                    System.out.println("Message from client: " + msg);
                if (msg.equals("QuitServer")) {
                    System.out.println("Client " + server.getRemoteSocketAddress() + " disconnected!");
                    break;
                }
                // 创建房间
                if (msgArray[0].equals("CreateRoom")) {
                    roomNumber = Server.roomNumber;
                    opponent = null;
                    Server.rooms.add(new Server.Sockets());
                    Server.rooms.get(roomNumber).one = server;
                    Server.roomNumber++;
                    out.writeUTF("CreateRoom: " + roomNumber);
                // 加入房间
                } else if (msgArray[0].equals("JoinRoom")) {
                    roomNumber = Integer.parseInt(msgArray[1]);
                    if (roomNumber < Server.rooms.size() && Server.rooms.get(roomNumber).two == null) {
                        Server.rooms.get(roomNumber).two = server;
                        DataOutputStream out1 = new DataOutputStream(
                                Server.rooms.get(roomNumber).one.getOutputStream());
                        out.writeUTF("JoinRoom: " + roomNumber);
                        out.flush();
                        out1.writeUTF("JoinRoom: " + roomNumber);
                        out1.flush();
                        long seed = random.nextLong();
                        out.writeUTF("StartGame: " + seed);
                        out1.writeUTF("StartGame: " + seed);
                        out.flush();
                        out1.flush();
                    } else if (roomNumber == 999) {
                        opponent = null;
                        out.writeUTF("JoinRoom: " + roomNumber);
                        out.flush();
                        long seed = random.nextLong();
                        out.writeUTF("StartGame: " + seed);
                        out.flush();
                    } else{
                        out.writeUTF("JoinRoom: false");
                        out.flush();
                    }
                // 比赛开始，互相传递信息
                } else if (msgArray[0].equals("GameDone")) {
                    opponent = null;
                    roomNumber = -1;
                } else if (opponent != null) {
                    DataOutputStream out1 = new DataOutputStream(opponent.getOutputStream());
                    out1.writeUTF(msg);
                    out1.flush();
                }
            }
            server.close();
        } catch (IOException e) {
            try {
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("OpponentLeave");
                out.flush();
            } catch (IOException e1) {
                System.out.println("Client " + server.getRemoteSocketAddress() + " disconnected!");
            }
        }
    }
}
