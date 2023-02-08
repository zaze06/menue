package me.alien.info;

import com.pi4j.io.gpio.digital.DigitalInput;
import me.alien.menu.App;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Info extends App {

    Graphics2D g2d;

    Timer t = new Timer();


    @Override
    public boolean start(DigitalInput button1, DigitalInput button2, DigitalInput button3, Graphics2D g2d) {
        this.g2d = g2d;

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //int tmp P
            }
        },0l,100l);

        return true;
    }

    @Override
    public void stop() {

    }
}
