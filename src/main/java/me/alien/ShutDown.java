package me.alien;

import com.pi4j.io.gpio.digital.DigitalInput;
import me.alien.menu.App;

import java.awt.*;

import static me.alien.menu.Main.updateDisplay;

public class ShutDown extends App {

    @Override
    public boolean[][] getIcon() {
        return new boolean[][]{
                {false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false},
                {false,false,false,false,false,true,true,true,true,false,false,false,false,false,false,true,true,true,true,false,false,false,false,false},
                {false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false},
                {false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false},
                {false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true},
                {false,false,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,false,true,true},
                {false,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,true,true},
                {false,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,true,true},
                {false,false,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,false,true,true},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
                {false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false},
                {false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false},
                {false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false},
                {false,false,false,false,false,true,true,true,true,false,false,false,false,false,false,true,true,true,true,false,false,false,false,false},
                {false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false}
        };
    }

    @Override
    public boolean start(DigitalInput button1, DigitalInput button2, DigitalInput button3, Graphics2D g2d) {
        g2d.drawString("Shutting down in 2 seconds", 0, 10);
        updateDisplay();
        synchronized (this){
            try {
                this.wait(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 128, 128);
        g2d.setColor(Color.WHITE);

        updateDisplay();

        System.exit(0);

        return true;
    }

    @Override
    public void stop() {

    }
}
