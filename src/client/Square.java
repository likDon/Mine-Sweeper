package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Square extends JButton {
    final static int iconWidth = 45;
    final static int iconHeight = 45;

    private int row, col;
    private boolean isMine;
    private boolean isFlag;
    private boolean isClear;
    private int mineNum;
    Game fatherGame;
    static ImageIcon mineIcon;
    static ImageIcon flagIcon;
    static ImageIcon flagWrongIcon;
    static ImageIcon mineRedIcon;
    static ImageIcon defaultIcon;
    static ImageIcon[] numberIcon;

    public Square(int r, int c, Game g) {
        fatherGame = g;
        row = r;
        col = c;
        isMine = false;
        isFlag = false;
        isClear = false;
        mineNum = 0;
        updateIcon();
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { click(e); }

            @Override
            public void mousePressed(MouseEvent e) { }

            @Override
            public void mouseReleased(MouseEvent e) { }

            @Override
            public void mouseEntered(MouseEvent e) { }

            @Override
            public void mouseExited(MouseEvent e) { }
        });
    }

    public void updateIcon() {
        if(!isClear) {
            if (isFlag) {
                setIcon(flagIcon);
            } else {
                setIcon(defaultIcon);
            }
        } else if(isMine) {
            setIcon(mineRedIcon);
        } else {
            setIcon(numberIcon[mineNum]);
        }
    }

    static public void initIcon() {
        mineIcon = new ImageIcon("mine.png");
        mineIcon.setImage(mineIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT));
        defaultIcon = new ImageIcon("default.png");
        defaultIcon.setImage(defaultIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT));
        flagIcon = new ImageIcon("flag.png");
        flagIcon.setImage(flagIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT));
        flagWrongIcon = new ImageIcon("flag_wrong.png");
        flagWrongIcon.setImage(flagWrongIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT));
        mineRedIcon = new ImageIcon("mine_red.png");
        mineRedIcon.setImage(mineRedIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT));

        numberIcon = new ImageIcon[9];
        for (int i=0; i<9;i++){
            numberIcon[i] = new ImageIcon(String.format("number_%d.png", i));
            numberIcon[i].setImage(numberIcon[i].getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT));
        }
    }

    public void clear() {
        isClear = true;
        updateIcon();
    }


    public void setRow(int r) { row = r; }

    public int getRow() { return row; }

    public void setCol(int c) { col = c; }

    public int getCol() { return col; }

    public void addMineNum() { mineNum += 1; }

    public void setMineNum(int n) { mineNum = n; }

    public int getMineNum() { return mineNum; }

    public void setIsMine(boolean b) { isMine = b; }

    public boolean isMine() { return isMine; }

    public void setIsFlag(boolean b) { isFlag = b; }

    public boolean isFlag() { return isFlag; }

    public void setIsClear(boolean b) { isClear = b; }

    public boolean isClear() { return isClear; }

    public void click(MouseEvent e) {
        if (!fatherGame.getStarted()) {
            fatherGame.setStarted(true);
            fatherGame.timerStart();
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (isMine) {
                isClear = true;
                updateIcon();
                fatherGame.lose();
            } else {
                if (!isClear) {
                    fatherGame.clearSquare(row, col);
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (isFlag) {
                fatherGame.leftMineNum += 1;
            } else {
                fatherGame.leftMineNum -= 1;
            }
            isFlag = !isFlag;
            fatherGame.updateMineNumLabel();
            updateIcon();
        }
    }

}
