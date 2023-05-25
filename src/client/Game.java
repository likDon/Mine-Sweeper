package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Game extends JFrame{
    final static int upMargin = 60;
    final static int leftMargin = 40;
    final static Map<String, Integer> levelNameToIdx = new HashMap<>() {{
       put("easy", 0);
       put("regular", 1);
       put("hard", 2);
    }};
    final static int[][] frameInit = {
            {8,8,10,2*leftMargin+8*Square.iconWidth,2*upMargin+8*Square.iconHeight},
            {16,16,40,2*leftMargin+16*Square.iconWidth,2*upMargin+16*Square.iconHeight},
            {16,30,99,2*leftMargin+30*Square.iconWidth,2*upMargin+16*Square.iconHeight}};
    private String level;
    private String name;
    private boolean started;
    private int timeSec;
    private Timer timer;
    private int width, height;
    private int row, col;

    private int mineNum;
    public int leftMineNum;
    private int clearedSquareNum;
    private Square[][] board;

    private JPanel contentPanel;

    private JLabel titleLabel;
    private JLabel mineLabel;
    private JLabel timeLabel;

    Socket socket;
    PrintWriter toServer;
    public Game(String level, String name) {
        setLayout(null);
        try {
            socket = new Socket("localhost", LeaderBoard.PORT);
            toServer = new PrintWriter(socket.getOutputStream());
            System.out.println("connected.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("connection failed.");
        }
        this.level = level;
        this.name = name;

        Square.initIcon();
        initFrame(level);

        board = new Square[row][col];
        for (int i=0;i<row;i++) {
            for (int j = 0; j < col; j++) {
                board[i][j] = new Square(i, j, this);
                board[i][j].setRow(i);
                board[i][j].setCol(j);
            }
        }

        setLayout(new BorderLayout());

        createPanel();
        add(contentPanel);

        initGame();

        setSize(width, height);
        setTitle("Mine Sweeper - " + level);
        setResizable(false);
        setVisible(true);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    }

    public void initFrame(String level) {
        int[] frameArgs = frameInit[levelNameToIdx.get(level)];
        row = frameArgs[0];
        col = frameArgs[1];
        mineNum = frameArgs[2];
        width = frameArgs[3];
        height = frameArgs[4];
        // set frame in the middle of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2-width/2, screenSize.height/2-height/2);
    }

    public void initGame() {
        leftMineNum = mineNum;
        clearedSquareNum = 0;
        mineLabel.setText(String.format("Mine Number: %d", leftMineNum));
        timeLabel.setText("time: 0 s");
        for (int i=0;i<row;i++) {
            for (int j=0;j<col;j++) {
                board[i][j].setIsMine(false);
                board[i][j].setIsFlag(false);
                board[i][j].setIsClear(false);
                board[i][j].setMineNum(0);
                board[i][j].updateIcon();
            }
        }
        generateMine();
    }

    private void createPanel() {
        // set background
        Image bgImg = null;
        try {
            bgImg = ImageIO.read(new File("bg.jpg")).getScaledInstance(width, height, Image.SCALE_DEFAULT);
        } catch(Exception e) {
            e.printStackTrace();
        }
        contentPanel = new BackgroundPanel(bgImg);
        contentPanel.setLayout(null);

        titleLabel = new JLabel("MINE SWEEPER - "+level);
        mineLabel = new JLabel(String.format("Mine Number: %d", leftMineNum));
        timeLabel = new JLabel("Time: 0 s");

        int fontSize = 25;
        if (level=="easy") {
            fontSize = 20;
        }
        Font font3 = Menu.font1.deriveFont(Font.BOLD, 20);

        titleLabel.setFont(Menu.font1);
        mineLabel.setFont(font3);
        timeLabel.setFont(font3);

        titleLabel.setBounds(leftMargin,20, 600, 50);
        mineLabel.setBounds(leftMargin,20,400,40);
        timeLabel.setBounds(width-leftMargin-fontSize*6,20,200,40);

//        contentPanel.add(titleLabel);
        contentPanel.add(mineLabel);
        contentPanel.add(timeLabel);

        for (int i=0;i<row;i++) {
            for (int j=0;j<col;j++) {
                int tx = leftMargin+Square.iconWidth*j;
                int ty = upMargin+50+Square.iconHeight*i;
                board[i][j].setBounds(leftMargin+Square.iconWidth*j,upMargin+Square.iconHeight*i, Square.iconWidth,Square.iconHeight);
                contentPanel.add(board[i][j]);
            }
        }
    }

    public void clearSquare(int r, int c) {
        clearedSquareNum += 1;
        board[r][c].clear();
        if (clearedSquareNum == row*col - mineNum) {
            win();
        }

        if (board[r][c].getMineNum() == 0) { // clear neighbours
            for (int i=r-1;i<r+2;i++) {
                for (int j=c-1;j<c+2;j++) {
                    if (0<=i && i<row && 0<=j && j<col && !board[i][j].isClear()) {
                        clearSquare(i, j);
                    }
                }
            }
        }

    }

    public void generateMine() {
        int r = (int) (Math.random() * row);
        int c = (int) (Math.random() * col);
        for (int i=0; i<mineNum;i++) {
            while (board[r][c].isMine()) {
                r = (int) (Math.random() * row);
                c = (int) (Math.random() * col);
            }
            board[r][c].setIsMine(true);
            updateNeighbourInfo(r, c);
        }
    }
    public void updateNeighbourInfo(int r, int c) {
        for (int i=r-1;i<r+2;i++) {
            for (int j=c-1;j<c+2;j++) {
                if (0<=i && i<row && 0<=j && j<col && !board[i][j].isMine()) {
                    board[i][j].addMineNum();
                }
            }
        }
    }

    public void lose() { // show all the mines, annotate wrong flags
        timer.stop();
        started = false;
        for (int i=0;i<row;i++) {
            for (int j=0;j<col;j++) {
                if (!board[i][j].isClear()){
                    if (!board[i][j].isFlag() && board[i][j].isMine()) {
                        board[i][j].setIcon(Square.mineIcon);
                    } else if (board[i][j].isFlag() && !board[i][j].isMine()) {
                        board[i][j].setIcon(Square.flagWrongIcon);
                    }
                }
            }
        }
        ImageIcon icon = new ImageIcon("lose.png");
        icon.setImage(icon.getImage().getScaledInstance(45, 45, Image.SCALE_DEFAULT));
        tryAgain("Boom! Try again?", icon);
    }

    public void win() {
        timer.stop();
        started = false;
        for (int i=0;i<row;i++) {
            for (int j=0;j<col;j++) {
                if (!board[i][j].isClear() && !board[i][j].isFlag()){
                    board[i][j].setIcon(Square.mineIcon);
                }
            }
        }
        System.out.println("to server "+level + " " + name + " " + timeSec);
        toServer.println(level + " " + name + " " + timeSec);
        toServer.flush();

        ImageIcon icon = new ImageIcon("win.png");
        icon.setImage(icon.getImage().getScaledInstance(45, 45, Image.SCALE_DEFAULT));
        tryAgain("Great game! Record uploaded. Try again?", icon);
    }

    public void tryAgain(String msg, ImageIcon icon) {
        int userOption =  JOptionPane.showConfirmDialog(null,msg,"",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE,icon);
        if (userOption == JOptionPane.YES_OPTION) {
            initGame();
        } else {
            dispose();
        }
    }

    public void updateMineNumLabel() {
        mineLabel.setText("Mine Number: "+ leftMineNum);
    }

    public boolean getStarted() { return started; }

    public void setStarted(boolean b) { started = b; }

    public void timerStart() {
        timeSec = 0;
        timer = new Timer(1000, (e) -> {
            timeSec += 1;
            timeLabel.setText("Time: "+timeSec + " s");
        });
        timer.start();
    }


    public static void main(String[] args) {
        Menu.loadFont();
        new Game("hard", "Alice");}
}
