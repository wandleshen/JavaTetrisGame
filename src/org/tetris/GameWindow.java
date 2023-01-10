package org.tetris;

import com.alibaba.fastjson.JSON;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serial;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class GameWindow extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    // 俄罗斯方块主游戏区偏移
    public static final int OFFSET = 100;
    // 方块边长
    public static final int BLOCK_SIZE = 20;
    // 窗口横向方块总数
    public static final int HORIZON_NODES = 10;
    // 窗口竖直方块总数
    public static final int VERTICAL_NODES = 20;
    // 代表方块在窗口上的分布情况，0代表空，1代表正在移动的方块，2代表已经固定的方块
    public static int[][] space = new int[VERTICAL_NODES][HORIZON_NODES];
    public static int[][] colorSpace = new int[VERTICAL_NODES][HORIZON_NODES];
    // 对方的方块分布情况
    public static int[][] otherSpace = new int[VERTICAL_NODES][HORIZON_NODES];
    public static int[][] otherColorSpace = new int[VERTICAL_NODES][HORIZON_NODES];
    // 画布
    private final Canvas canvas;
    // 定时器
    private Timer timer;
    // 游戏得分
    public static int score;
    // 对手游戏得分
    private int otherScore = -1;
    // 发送垃圾行数量
    public static int sendGarbageCount;
    // 用户名
    public static String username = "";
    // 对手用户名
    public static String otherUsername = "";
    // 用于记录结果的玩家
    public static boolean isPlayerOne = false;
    // 胜负数量
    public static int winCount = 0;
    public static int loseCount = 0;
    public static int otherWinCount = 0;
    public static int otherLoseCount = 0;
    // 七种图形，分别是O,I,Z,S,L,J,T。每个图形包含4个方块。其中{0,0}为中心点。
    public static int[][][] shapes = {
            {{-1, 0}, {0, 0}, {-1, -1}, {0, -1}},
            {{-1, 0}, {0, 0}, {1, 0}, {2, 0}},
            {{-1, -1}, {0, -1}, {0, 0}, {1, 0}},
            {{-1, 1}, {0, 1}, {0, 0}, {1, 0}},
            {{-1, -1}, {-1, 0}, {0, 0}, {1, 0}},
            {{-1, 0}, {0, 0}, {1, 0}, {1, -1}},
            {{-1, 0}, {0, 0}, {1, 0}, {0, -1}}};
    // 当前中心点的坐标
    private final Point centerPos = new Point();
    // 当前正在移动的图形的坐标
    private int[][] currentShape = new int[4][2];
    // 现在的一包方块
    private int[] currentPack = new int[]{0, 1, 2, 3, 4, 5, 6};
    // 下一包方块
    private final int[] nextPack = new int[]{0, 1, 2, 3, 4, 5, 6};
    // 当前包的索引
    private int currentPackIndex = 0;
    // 当前形状的索引
    private int currentShapeIndex = 0;
    // 当前 hold 块的索引
    private int holdIndex = -1;
    // 等级，与落块速度相关联
    private int level = 1;
    // 用于随机生成块的随机数生成器和用于生成垃圾行空格位置的随机数生成器
    private static final Random rand = new Random();
    private static final Random garbage = new Random();
    // 交换数组元素
    public static void swap(int[] a, int i, int j){
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
    // 打乱数组
    public static void shuffle(int[] arr) {
        int length = arr.length;
        for ( int i = length; i > 0; i-- ){
            int randInd = rand.nextInt(i);
            swap(arr, randInd, i - 1);
        }
    }

    public GameWindow() {
        JLabel holdText = new JLabel("Hold");
        holdText.setBounds(10, 0, 40, 20);
        add(holdText);
        JLabel nextText = new JLabel("Next");
        nextText.setBounds(310, 0, 40, 20);
        add(nextText);
        canvas = new Canvas();
        addKeyListener(new KeyHandler());
        add(canvas);
        pack();
        Timer otherTimer = new Timer(10, new OtherTimerHandler());
        otherTimer.start();
        resetAllStatics();
    }
    // 开始游戏
    private double speed;
    public void start() {
        if (timer != null) {
            timer.stop();
        }
        choseShape();
        speed = Math.pow((0.8f - (level - 1) * 0.007f), level - 1);
        timer = new Timer((int) (1000.f * speed), new TimerHandler());
        timer.start();
    }
    // 随机选择一个图形并确定位置
    private void choseShape() {
        if (currentPackIndex == 7) {
            currentPack = nextPack.clone();
            shuffle(nextPack);
            currentPackIndex = 0;
        }
        currentShape = shapes[currentPack[currentPackIndex]];
        currentShapeIndex = currentPack[currentPackIndex];
        // 不同的方块拥有不同的起始位置
        if (currentPack[currentPackIndex] == 0) {
            centerPos.x = 5;
        } else {
            centerPos.x = 4;
        }
        centerPos.y = 0;
        isFirstHold = true;
        isHardDrop = false;
        spinShape = 0;
        canvas.setIsFirstHold(true);
        // next 区信息
        int[] nextShapeIndex = new int[6];
        int i = 1;
        while (currentPackIndex + i < 7) {
            nextShapeIndex[i - 1] = currentPack[currentPackIndex + i];
            i++;
        }
        i = 0;
        while (i < currentPackIndex) {
            nextShapeIndex[i + 6 - currentPackIndex] = nextPack[i];
            i++;
        }
        canvas.setNextShapeIndex(nextShapeIndex);
        setShadow();
        currentPackIndex++;
        updateSpace(1);
    }
    // 图形下移
    private boolean isLockDown = false;
    private boolean isHardDrop = false;
    private boolean isSoftDrop = false;
    private boolean moveDown() {
        if (isHardDrop)
            return false;
        for (int i = 0; i < 4; i++) {
            int x = centerPos.x + currentShape[i][0];
            int y = centerPos.y + currentShape[i][1] + 1;
            if (y < 0)
                continue;
            if (y >= VERTICAL_NODES || space[y][x] == 2) {
                if (isLockDown) {
                    isLockDown = false;
                    return true;
                }
                return false;
            }
        }
        isTSpin = false;
        isLockDown = false;
        updateSpace(0);
        centerPos.y++;
        updateSpace(1);
        return true;
    }
    // 检查方块碰撞
    boolean isChecking = false;
    private Boolean checkCollision(int[][] temp, int x, int y) {
        for (int i = 0; i < 4; i++) {
            int tmpX = x + temp[i][0];
            int tmpY = y + temp[i][1];
            if (tmpX < 0 || tmpX >= HORIZON_NODES
                    || tmpY >= VERTICAL_NODES ||
                    tmpY >= 0 && space[tmpY][tmpX] == 2) {
                return false;
            }
        }
        return true;
    }
    // 方块预测
    private int shadowY;
    private void setShadow() {
        int y = Math.max(1, centerPos.y);
        while (checkCollision(currentShape, centerPos.x, y)) {
            y++;
        }
        y--;
        shadowY = y;
        for (int i = 0; i < 4; i++) {
            int x = centerPos.x + currentShape[i][0];
            int tmpY = y + currentShape[i][1];
            if (x < 0 || x >= HORIZON_NODES || tmpY < 0 || tmpY >= VERTICAL_NODES) {
                continue;
            }
            if (space[tmpY][x] == 0) {
                space[tmpY][x] = 3;
                colorSpace[tmpY][x] = currentShapeIndex;
            }
        }
    }
    private void resetShadow() {
        for (int i = 0; i < 4; i++) {
            int x = centerPos.x + currentShape[i][0];
            int y = shadowY + currentShape[i][1];
            if (x < 0 || x >= HORIZON_NODES || y < 0 || y >= VERTICAL_NODES) {
                continue;
            }
            if (shadowY != centerPos.y && space[y][x] == 3)
                space[y][x] = 0;
        }
    }
    // 图形旋转
    // 同时包含一个 t-spin 判断
    Boolean isTSpin = false;
    // 方块的形态
    private int spinShape = 0;
    // 两张踢墙表
    private final int[][][] kickWallTable = {
            {{-1, 0}, {-1, 1}, {0, 2}, {-1, -2}},
            {{1, 0}, {1, -1}, {0, -2}, {1, 2}},
            {{1, 0}, {1, 1}, {0, -2}, {1, -2}},
            {{-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
    };
    private final int[][][] kickWallTableI = {
            {{-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
            {{2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
            {{-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
            {{1, 0}, {-2, 0}, {1, -2}, {-2, 1}}
    };
    private void transform(boolean isClockwise) {
        // O 块不旋转
        if (currentShapeIndex == 0) {
            return;
        }
        int[][] temp = new int[4][2];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(currentShape[i], 0, temp[i], 0, 2);
        }
        int base = 2;
        // 记录起始旋转姿态
        if (spinShape == 1)
            base = 0;
        else if (spinShape == 3)
            base = 1;
        // 旋转
        if (isClockwise) {
            for (int i = 0; i < 4; i++) {
                int t = temp[i][1];
                temp[i][1] = temp[i][0];
                temp[i][0] = -1 * t;
            }
            spinShape = (spinShape + 1) % 4;
        } else {
            for (int i = 0; i < 4; i++) {
                int t = temp[i][1];
                temp[i][1] = -1 * temp[i][0];
                temp[i][0] = t;
            }
            spinShape = (spinShape + 3) % 4;
        }
        int x = centerPos.x;
        int y = centerPos.y;
        // 针对 I 块的形状微调
        if (currentShapeIndex == 1) {
            int step = isClockwise ? 1 : -1;
            switch (spinShape) {
                case 0 -> x += step;
                case 1 -> y += step;
                case 2 -> x -= step;
                case 3 -> y -= step;
            }
        }
        boolean isKickWall = false;
        // 踢墙检测
        if (!checkCollision(temp, x, y)) {
            timer.stop();
            isChecking = true;
            isKickWall = true;
            boolean isDealCol = false;
            int[][] kickWall;
            // 选择踢墙检测表
            if (currentShapeIndex != 1) {
                switch (base) {
                    // R -> 0, R -> 2, L -> 0, L -> 2
                    case 0, 1 -> kickWall = kickWallTable[base * 2 + 1];
                    // 0 -> R, 0 -> L, 2 -> R, 2 -> L
                    default -> kickWall = kickWallTable[spinShape - 1];
                }
            } else {
                if (isClockwise) {
                    switch (spinShape) {
                        // L -> 0, 0 -> 1, 1 -> 2, 2 -> L
                        case 0 -> kickWall = kickWallTableI[3];
                        case 1 -> kickWall = kickWallTableI[0];
                        case 2 -> kickWall = kickWallTableI[2];
                        default -> kickWall = kickWallTableI[1];
                    }
                } else {
                    switch (spinShape) {
                        // 0 -> L, 1 -> 0, 2 -> 1, L -> 2
                        case 0 -> kickWall = kickWallTableI[1];
                        case 1 -> kickWall = kickWallTableI[3];
                        case 2 -> kickWall = kickWallTableI[0];
                        default -> kickWall = kickWallTableI[2];
                    }
                }
            }
            // 开始检测
            for (int i = 0; i < 4; i++) {
                x = centerPos.x + kickWall[i][0];
                y = centerPos.y - kickWall[i][1];
                if (checkCollision(temp, x, y)) {
                    isDealCol = true;
                    break;
                }
            }
            // 不给转
            if (!isDealCol) {
                if (isClockwise)
                    spinShape = (spinShape + 3) % 4;
                else
                    spinShape = (spinShape + 1) % 4;
                isChecking = false;
                timer.start();
                return;
            }
            isChecking = false;
            timer.start();
        }
        updateSpace(0);
        resetShadow();
        centerPos.x = x;
        centerPos.y = y;
        if (currentShapeIndex == 6 && !isKickWall
                && !checkCollision(temp, x, y + 1)
                && !checkCollision(temp, x + 1, y) && !checkCollision(temp, x - 1, y))
            isTSpin = true;
        currentShape = temp;
        setShadow();
        updateSpace(1);
    }
    // 图形左移
    private void moveLeft() {
        for (int i = 0; i < 4; i++) {
            int x = centerPos.x + currentShape[i][0] - 1;
            int y = centerPos.y + currentShape[i][1];
            if (x < 0 || y >= 0 && space[y][x] == 2)
                return;
        }
        resetShadow();
        isLockDown = true;
        updateSpace(0);
        centerPos.x--;
        setShadow();
        updateSpace(1);
    }
    // 图形右移
    private void moveRight() {
        for (int i = 0; i < 4; i++) {
            int x = centerPos.x + currentShape[i][0] + 1;
            int y = centerPos.y + currentShape[i][1];
            if (x >= HORIZON_NODES || y >= 0 && space[y][x] == 2)
                return;
        }
        resetShadow();
        isLockDown = true;
        updateSpace(0);
        centerPos.x++;
        setShadow();
        updateSpace(1);
    }
    // 固定图形
    private void fixBox() {
        updateSpace(2);
    }
    // 消除满行，同时发送垃圾行并计算分数
    // 计算连击次数
    private int combo = -1;
    private void clearLine() {
        int count = 0;
        int y = centerPos.y + currentShape[0][1];
        if (y < 0)
            y = 0;
        int minY = y, maxY = y;
        for (int i = 1; i < 4; i++) {
            y = centerPos.y + currentShape[i][1];
            if (y > maxY) maxY = y;
            else if (y < minY) minY = y;
        }
        minY = Math.max(minY, 0);
        for (y = minY; y <= maxY; y++) {
            int x;
            for (x = 0; x < HORIZON_NODES; x++) {
                if (space[y][x] == 0 || space[y][x] == 3) break;
            }
            if (x == HORIZON_NODES) {
                for (int i = 0; i < HORIZON_NODES; i++) {
                    for (int j = y; j > 0; j--) {
                        space[j][i] = space[j - 1][i];
                        colorSpace[j][i] = colorSpace[j - 1][i];
                    }
                }
                count++;
            }
        }
        combo += 1;
        switch (count) {
            case 0 -> {
                combo = -1;
                if (isTSpin)
                    score += 400 * level;
            }
            case 1 -> {
                score += 100 * level;
                if (isTSpin) {
                    score += 700 * level;
                    Client.sendGarbageLine(2);
                    sendGarbageCount += 2;
                }
            }
            case 2 -> {
                score += 300 * level;
                if (isTSpin) {
                    score += 900 * level;
                    Client.sendGarbageLine(4);
                    sendGarbageCount += 4;
                } else {
                    Client.sendGarbageLine(1);
                    sendGarbageCount += 1;
                }
            }
            case 3 -> {
                score += 500 * level;
                if (isTSpin) {
                    score += 1100 * level;
                    Client.sendGarbageLine(6);
                    sendGarbageCount += 6;
                } else {
                    Client.sendGarbageLine(2);
                    sendGarbageCount += 2;
                }
            }
            case 4 -> {
                Client.sendGarbageLine(4);
                sendGarbageCount += 4;
                score += 800 * level;
            }
        }
        if (count > 0) {
            boolean isAllClear = true;
            for (int i = 0; i < HORIZON_NODES; i++) {
                if (space[VERTICAL_NODES - 1][i] == 2) {
                    isAllClear = false;
                    break;
                }
            }
            if (isAllClear) {
                Client.sendGarbageLine(10);
                sendGarbageCount += 10;
            }
        }
        score += 50 * Math.max(combo, 0) * level;
        Client.sendGarbageLine(Math.min(Math.max(0, combo), 5));
        if (level <= 4)
            level = score / 1000 + 1;
        else
            level = score / 10000 + 5;
        level = Math.min(level, 11);
        isTSpin = false;
    }
    // 将方块加入 hold 框中
    // 第一次 hold，框中没有方块
    boolean isFirstHold = true;
    private void holdCube() {
        if (isFirstHold) {
            resetShadow();
            if (holdIndex == -1) {
                updateSpace(0);
                holdIndex = currentShapeIndex;
                choseShape();
            } else {
                updateSpace(0);
                int t = currentShapeIndex;
                currentShapeIndex = holdIndex;
                holdIndex = t;
                currentShape = shapes[currentShapeIndex];
                if (currentShapeIndex == 0) {
                    centerPos.x = 5;
                } else {
                    centerPos.x = 4;
                }
                centerPos.y = 0;
                updateSpace(1);
                isFirstHold = false;
                canvas.setIsFirstHold(false);
            }
            canvas.setHoldIndex(holdIndex);
        }
    }
    // 生成垃圾行
    private void addGarbageLine(int num) {
        if (centerPos.y + num + 2 >= VERTICAL_NODES || space[centerPos.y + num + 2][centerPos.x] == 2) {
            centerPos.y = 0;
        }
        resetShadow();
        for (int i = 0; i < HORIZON_NODES; i++) {
            for (int j = num; j < VERTICAL_NODES; j++) {
                if (space[j][i] != 1) {
                    space[j - num][i] = space[j][i];
                    colorSpace[j - num][i] = colorSpace[j][i];
                }
            }
        }
        int empty = garbage.nextInt(HORIZON_NODES);
        for (int i = 0; i < HORIZON_NODES; i++) {
            for (int j = VERTICAL_NODES - num; j < VERTICAL_NODES; j++) {
                if (i == empty) {
                    space[j][i] = 0;
                    colorSpace[j][i] = 0;
                } else {
                    space[j][i] = 2;
                    colorSpace[j][i] = 7;
                }
            }
        }
        setShadow();
    }
    // 更新 space 数组
    private void updateSpace(int flag) {
        for (int i = 0; i < 4; i++) {
            int x = centerPos.x + currentShape[i][0];
            int y = centerPos.y + currentShape[i][1];
            if (x >= 0 && y >= 0 && x < HORIZON_NODES && y < VERTICAL_NODES) {
                space[y][x] = flag;
                colorSpace[y][x] = currentShapeIndex;
            }

        }
    }
    //判断是否触顶
    private boolean gameOver() {
        for (int i = 0; i < 4; i++) {
            int y = centerPos.y + currentShape[i][1];
            if (y <= 0) return true;
        }
        return false;
    }
    //定时器监听
    // 是否本方游戏结束
    Boolean isGameOver = false;
    class TimerHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (isGameOver) {
                timer.stop();
                if (otherScore >= 0) {
                    if (otherScore > score) {
                        JOptionPane.showMessageDialog(
                                null, "You lose, opponent's score is " + otherScore);
                        if (isPlayerOne)
                            Database.recordBattle(
                                    username, otherUsername,
                                    String.valueOf(score), String.valueOf(otherScore),
                                    otherUsername);
                    } else if (otherScore < score) {
                        JOptionPane.showMessageDialog(
                                null, "You win, opponent's score is " + otherScore);
                        if (isPlayerOne)
                            Database.recordBattle(
                                    username, otherUsername,
                                    String.valueOf(score), String.valueOf(otherScore),
                                    username);
                    } else {
                        JOptionPane.showMessageDialog(
                                null, "Draw, opponent's score is " + otherScore);
                    }
                    Client.sendGameDone();
                    dispose();
                    Tetris.startRoomBoard();
                } else {
                    timer = new Timer(200, new TimerHandler());
                    timer.start();
                }
            } else {
                if (!moveDown()) {
                    fixBox();
                    clearLine();
                    if (gameOver()) {
                        Client.sendGameOver(score);
                        isGameOver = true;
                    } else {
                        start();
                    }
                } else {
                    // 软降得分
                    if (isSoftDrop)
                        score += 1;
                }
            }
            canvas.repaint();
        }
    }
    // 另一个定时器监听，用于监听各种 Server 发来的信息
    //
    boolean isStart = false;
    class OtherTimerHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!isGameOver)
                Client.sendSpace();
            if (isStart) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                isStart = false;
                start();
                return;
            }
            String msg;
            try {
                msg = Client.getMsg();
            } catch (IOException ex) {
                return;
            }
            String[] msgArray = msg.split(": ");
            switch (msgArray[0]) {
                case "StartGame" -> {
                    rand.setSeed(Long.parseLong(msgArray[1]));
                    if (timer == null) {
                        shuffle(currentPack);
                        shuffle(nextPack);
                        Client.sendUsername(username);
                        JOptionPane.showMessageDialog(null,
                                "Connection established, game will be start in 3 seconds!");
                        isStart = true;
                    }
                }
                case "Space" -> {
                    otherSpace = JSON.parseObject(msgArray[1], int[][].class);
                    otherColorSpace = JSON.parseObject(msgArray[2], int[][].class);
                }
                case "GarbageLine" -> addGarbageLine(Integer.parseInt(msgArray[1]));
                case "GameOver" -> otherScore = Integer.parseInt(msgArray[1]);
                case "Username" -> {
                    otherUsername = msgArray[1];
                    otherWinCount = Database.getWinCount(otherUsername);
                    otherLoseCount = Database.getLoseCount(otherUsername);
                }
                case "OpponentLeave" -> {
                    JOptionPane.showMessageDialog(null, "Opponent leave the game!");
                    Database.recordBattle(
                            username, otherUsername,
                            String.valueOf(score), String.valueOf(-1),
                            username);
                    Client.reconnect();
                    dispose();
                    Tetris.startRoomBoard();
                }
            }
        }
    }
    // 处理方向键
    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP -> {
                    if (!isHardDrop && !isChecking) {
                        transform(true);
                        canvas.repaint();
                    }
                }
                case KeyEvent.VK_Z, KeyEvent.VK_CONTROL -> {
                    if (!isHardDrop && !isChecking) {
                        transform(false);
                        canvas.repaint();
                    }
                }
                case KeyEvent.VK_SPACE -> {
                    updateSpace(0);
                    score += 2 * (shadowY - centerPos.y);
                    centerPos.y = shadowY;
                    updateSpace(1);
                    canvas.repaint();
                    isHardDrop = true;
                }
                case KeyEvent.VK_DOWN -> {
                    timer.setDelay(50);
                    isSoftDrop = true;
                }
                case KeyEvent.VK_LEFT -> {
                    if (!isHardDrop) {
                        moveLeft();
                        canvas.repaint();
                    }
                }
                case KeyEvent.VK_RIGHT -> {
                    if (!isHardDrop) {
                        moveRight();
                        canvas.repaint();
                    }
                }
                case KeyEvent.VK_C, KeyEvent.VK_SHIFT -> {
                    if (!isHardDrop) {
                        holdCube();
                        canvas.repaint();
                    }
                }
                case KeyEvent.VK_P -> {
                }
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                timer.setDelay((int) (1000.f * speed));
                isSoftDrop = false;
            }
        }
    }
    private static void resetAllStatics() {
        isPlayerOne = false;
        score = 0;
        sendGarbageCount = 0;
        otherWinCount = 0;
        otherLoseCount = 0;
        otherUsername = "";
        space = new int[20][10];
        colorSpace = new int[20][10];
        otherSpace = new int[20][10];
        otherColorSpace = new int[20][10];
    }
}
