package me.alien.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Debug extends JPanel {
    JFrame frame = new JFrame();
    BufferedImage data = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);

    public Debug(){
        frame.setSize(Main.getWidth(), Main.getHeight());
        frame.add(this);
        frame.setVisible(true);
    }

    public void setData(BufferedImage data) {
        this.data = data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(data,0,0,null);
    }
}
