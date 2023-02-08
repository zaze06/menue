package me.alien.brik.brake;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.digital.DigitalInput;
import me.alien.menu.App;
import me.alien.menu.Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class BrickBrake extends App {
    private Graphics2D g2d = null;
    private final boolean[][] brick = new boolean[42][15];

    int x = 64, y = 25, plateX = 59;
    int yDir = -1, xDir = -1;
    int hp = 5;

    private DigitalInput left;
    private DigitalInput right;

    public boolean start(DigitalInput left, DigitalInput button2, DigitalInput right, Graphics2D g2d) {
        this.g2d = g2d;
        this.left = left;
        this.right = right;
        //System.out.println("After getGraphics2D");
        for (boolean[] booleans : brick) {
            Arrays.fill(booleans, true);
        }

        displayUpdate.start();
        gameLoop.start();

        return true;
    }

    @Override
    public void stop() {
        gameLoop.stop();
        displayUpdate.stop();
    }

    Timer gameLoop = new Timer(200, (a) -> {
        if(left.isHigh() && plateX-2 > -1){
            plateX -= 2;
        }
        if(right.isHigh() && plateX+2 < 129){
            plateX += 2;
        }



        x+=xDir;
        y+=yDir;
        try{
            if(brick[x/3][y-5]){
                brick[x/3][y-5] = false;
                yDir*=-1;
            }
        }catch (Exception ignored){}
        if(x+xDir<0||x+xDir>128){
            xDir*=-1;
        }
        if(x<(plateX+10)&&x>plateX&&y==31){
            yDir*=-1;
        }

        if(y > 32){
            hp--;
            x = 64;
            y = 25;

            if(hp == 0){
                plateX = 59;
                yDir = -1;
                xDir = -1;
                hp = 5;

                for (boolean[] booleans : brick) {
                    Arrays.fill(booleans, true);
                }
            }
        }
    });

    Timer displayUpdate = new Timer(100, (a) -> {
        clear();

        for(int x = 0; x < brick.length; x++){
            for(int y = 0; y < brick[x].length; y++){
                if(brick[x][y]) {
                    g2d.fillRect(x*3,y+5,2,1);
                }
            }
        }

        g2d.fillRect(x,y,1,1);
        g2d.fillRect(plateX,31,10,1);

        for(int i = 0; i < hp; i++){
            g2d.fillRect(i*3,0,2,2);
        }

        Main.updateDisplay();
    });

    private void clear() {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 128, 128);
        g2d.setColor(Color.WHITE);
    }
}
