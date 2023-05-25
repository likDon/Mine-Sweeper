package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class LeaderBoard extends BackgroundPanel {
    final static int PORT = 9009;
    private Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;

    JComboBox comboBox;
    String[] info;
    JLabel[] records;

    public LeaderBoard(Image img) {
        super(img);
        setLayout(null);

        JLabel titleLabel = new JLabel("Leader Board");
        openConnection();
        createComboBox();

        titleLabel.setFont(Menu.font1);
        Font font3 = Menu.font2.deriveFont(Font.BOLD, 25);
        comboBox.setFont(font3);

        titleLabel.setBounds(180,50,400,50);
        comboBox.setBounds(260,105,150,30);

        add(titleLabel);
        add(comboBox);

        info = new String[10];
        records = new JLabel[10];
        for (int i=0; i<10; i++) {
            info[i] = new String();
            records[i] = new JLabel();
            records[i].setFont(font3);
            records[i].setBounds(280, 140+i*35,400,35);
            add(records[i]);
        }
        update("regular");
    }

    public void createComboBox() {
        String[] levels = new String[] {"easy", "regular", "hard"};
        comboBox = new JComboBox<>(levels);
        comboBox.setPreferredSize(new Dimension(200,30));
        comboBox.setSelectedIndex(1); // default
        comboBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                update(levels[comboBox.getSelectedIndex()]);
            }
        });

        comboBox.setFont(Menu.font2);
        comboBox.setBounds(100,100,300,30);
    }

    public void openConnection() {
        try {
            socket = new Socket("localhost", PORT);
            System.out.println("connected.");
            fromServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            toServer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("connection failed.");
        }

    }

    public void update(String level) {
        int rank = 0;
        System.out.println("update:"+level);
        toServer.println(level);
        toServer.flush();
        try {
            while (true) {
                String s = fromServer.readLine();
                if (s.equals("finish")) {
                    break;
                }
                info[rank] = s;
                rank += 1;
            }

            System.out.println(rank);
            while (rank < 10) {

                info[rank] = "";
                rank += 1;
            }
            for (int i=0; i<10; i++) {
                records[i].setText(info[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
