package me.alien.setting;

import com.pi4j.io.gpio.digital.DigitalInput;
import me.alien.menu.App;
import me.alien.menu.Main;
import me.alien.spotify.Loader;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static me.alien.menu.Main.updateDisplay;

public class Settings extends App {
    @Override
    public boolean[][] getIcon() {
        return new boolean[][]{
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,true,false,false,false,false,true,true,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,true,false,false,false,false,true,true,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,true,true,true,false,false,false,true,true,false,false,false,true,true,true,false,false,false,false,false},
                {false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false},
                {false,false,false,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,false,false,false},
                {false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
                {false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
                {false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
                {false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false},
                {false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
                {false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
                {false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false},
                {false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
                {false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
                {false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
                {false,false,false,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,false,false,false},
                {false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false},
                {false,false,false,false,false,true,true,true,false,false,false,true,true,false,false,false,true,true,true,false,false,false,false,false},
                {false,false,false,false,false,false,true,false,false,false,false,true,true,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,true,false,false,false,false,true,true,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false}
        };
    }

    @Override
    public boolean start(DigitalInput button1, DigitalInput button2, DigitalInput button3, Graphics2D g2d) {
        g2d.drawString("This application curently have no interface",0,10);
        g2d.drawString("Quiting in 2 secounds",0,20);
        updateDisplay();
        synchronized (this){
            try {
                this.wait(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    @Override
    public void stop() {

    }

    public JSONObject getConfig(String config){
        JSONObject configFile = new JSONObject(Loader.loadFile("./config.json"));
        if(configFile.has(config)){
            return configFile.getJSONObject(config);
        }else {
            return new JSONObject();
        }
    }

    public void setConfig(String config, JSONObject data){
        JSONObject configFile = new JSONObject(Loader.loadFile("config.json"));
        configFile.put(config, data);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("./config.json"));
            out.write(configFile.toString(4));
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
