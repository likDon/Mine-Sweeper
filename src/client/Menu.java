package client;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Menu extends JFrame {
    final static String[] levels = new String[] {"easy", "regular", "hard"};
    static Font font1, font2;
    private Image bgImg;
    private static int WIDTH = 700;
    private static int HEIGHT = 600;

    private BackgroundPanel mainMenuPanel;
    private BackgroundPanel leaderboardPanel;

    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem mainMenuItem;

    private JTextField nameTextField;
    private JComboBox comboBox;
    private JButton newGameBtn;
    private JButton leaderboardBtn;

    public Menu() {
        // set frame in the middle of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2-WIDTH/2, screenSize.height/2-HEIGHT/2);

        // set background
        try {
            bgImg = ImageIO.read(new File("bg.jpg")).getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // set menu bar
        createMenuBar();
        setJMenuBar(menuBar);

        // create 2 panel
        createMainMenu();
        leaderboardPanel = new LeaderBoard(bgImg);

        changeContentPanel(mainMenuPanel);

        setSize(Menu.WIDTH, Menu.HEIGHT);
        setTitle("Mine Sweeper");
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createMenuBar() {
        // create menu bar
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        mainMenuItem = new JMenuItem("Main Menu");
        mainMenuItem.addActionListener((e) -> {
            mainMenuPanel.setOpaque(false);
            changeContentPanel(mainMenuPanel);
        });
        menu.add(mainMenuItem);
        menuBar.add(menu);

        Font font3 = font2.deriveFont(Font.BOLD, 20);
        menu.setFont(font3);
        mainMenuItem.setFont(font3);
    }

    private void createMainMenu() {
        mainMenuPanel = new BackgroundPanel(bgImg);
        mainMenuPanel.setLayout(null);

        comboBox = new JComboBox<>(levels);
        comboBox.setSelectedIndex(1); // default

        newGameBtn = new JButton("New Game");
        leaderboardBtn = new JButton("LeaderBoard");

        newGameBtn.addActionListener((e) -> {
            String name = nameTextField.getText();
            if (name.equals("") || name.contains(" ")) {
                ImageIcon icon = new ImageIcon("lose.png");
                icon.setImage(icon.getImage().getScaledInstance(45, 45, Image.SCALE_DEFAULT));
                int userOption =  JOptionPane.showConfirmDialog(null, "Player name cannot be empty or contain spaces.","",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, icon);
            } else {
                Game game = new Game(levels[comboBox.getSelectedIndex()], name);
            }
        });

        leaderboardBtn.addActionListener((e) -> {
            changeContentPanel(leaderboardPanel);
        });

        JLabel titleLabel = new JLabel("MINE SWEEPER");
        JLabel nameLabel = new JLabel("Player Name: ");
        JLabel levelLabel = new JLabel("Choose Level: ");
        nameTextField = new JTextField();

        nameTextField.setBackground(new Color(255,255,255,150));

        titleLabel.setFont(font1);
        nameLabel.setFont(font2);
        nameTextField.setFont(font2);
        levelLabel.setFont(font2);
        comboBox.setFont(font2.deriveFont(Font.BOLD, 25));
        newGameBtn.setFont(font1.deriveFont(Font.BOLD, 25));
        leaderboardBtn.setFont(font1.deriveFont(Font.BOLD, 25));

        titleLabel.setBounds(180, 90, 400,50);
        nameLabel.setBounds(160, 170, 200,30);
        nameTextField.setBounds(350, 170, 180,35);
        levelLabel.setBounds(155, 210, 200,30);
        comboBox.setBounds(350, 215, 150,30);
        newGameBtn.setBounds(210, 280,270,70);
        leaderboardBtn.setBounds(210, 360,270,80);

        mainMenuPanel.add(titleLabel);
        mainMenuPanel.add(nameLabel);
        mainMenuPanel.add(nameTextField);
        mainMenuPanel.add(levelLabel);
        mainMenuPanel.add(comboBox);
        mainMenuPanel.add(newGameBtn);
        mainMenuPanel.add(leaderboardBtn);
    }


    public void changeContentPanel(Container contentPane) {
        setContentPane(contentPane);
        revalidate();
    }

    public static void loadFont() {
        try {
            font1 = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("font1.ttf"))).deriveFont(Font.BOLD, 40);
            font2 = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("font2.ttf"))).deriveFont(Font.BOLD, 30);
        } catch (Exception e) {
            System.out.println("load font file failed");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        loadFont();
        new Menu();
    }

}
