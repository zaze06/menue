package me.alien.menu;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.util.Console;
import me.alien.GPIOPin;
import me.alien.ShutDown;
import me.alien.brik.brake.BrickBrake;
import me.alien.lib.SSD1306;
import me.alien.lib.SSD1306_Defines;
import me.alien.setting.Settings;
import me.alien.spotify.Spotify;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static me.alien.util.LoadImage.getImageFromResources;

public class Main {
    private static Settings config = null;
    private static SSD1306 display;
    private static Debug debug;
    private DigitalInput button1 = null;
    private DigitalInput button2 = null;
    private DigitalInput button3 = null;
    private DigitalInput home = null;
    ArrayList<App> apps = new ArrayList<>();
    int selected = 0;

    private static final BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    private final Graphics2D g2d = img.createGraphics();

    public static void main(String[] args) throws IOException, InterruptedException {
        new Main();
    }

    boolean[][] next = getImageFromResources("images/next.bin");

    boolean[][] before = getImageFromResources("images/before.bin");
    boolean[][] selectedIcon = getImageFromResources("images/selected.bin");
    boolean[][] notSelectedIcon = getImageFromResources("images/not_selected.bin");

    public static int getWidth(){
        return 128;
    }

    public static int getHeight(){
        return 64;
    }
    private static byte[] buffer;
    private int valueAllOn = 0xff;
    private int valueAllOff = 0x00;
    private static int pages;
    private static final int DC_BIT = 6;

    boolean startedApp = false;

    Timer menue = new Timer(100, (a) -> {
        clear();

        for(int i = 0; i < apps.size(); i++){
            boolean[][] icon = notSelectedIcon;
            if(i == selected){
                icon = selectedIcon;
            }
            for(int x = 0; x < icon.length; x++){
                for(int y = 0; y < icon[x].length; y++){
                    if(icon[x][y]){
                        g2d.fillRect(x+((getWidth()/2-(((int)(apps.size()/2.0*8))+(int)(apps.size()/2.0)))+((i*8)+i) - icon.length/2),y,1,1);
                    }
                }
            }
        }

        boolean[][] icon = apps.get(selected).getIcon();

        if(!(icon.length < 41 && icon.length > 9 && icon[0].length < 41 && icon[0].length > 9)){
            icon = App.getDefaultIcon();
        }

        for(int x = 0; x < icon.length; x++){
            for(int y = 0; y < icon[x].length; y++){
                if(icon[x][y]){
                    g2d.fillRect(x+(getWidth()/2- icon.length/2),y+10,1,1);
                }
            }
        }
        g2d.setFont(new Font(g2d.getFont().getName(), Font.PLAIN, 10));
        g2d.drawString(apps.get(selected).getClass().getSimpleName(), (getWidth()/2)-(g2d.getFontMetrics().stringWidth(apps.get(selected).getClass().getSimpleName())/2), icon[0].length+8+10);

        if((selected-1 >= 0)){
            for(int x = 0; x < before.length; x++){
                for(int y = 0; y < before[x].length; y++){
                    if(before[x][y]){
                        g2d.fillRect(x,y+(getHeight()/2-before[x].length/2),1,1);
                    }
                }
            }
        }

        if((selected+1 < apps.size())){
            for(int x = 0; x < next.length; x++){
                for(int y = 0; y < next[x].length; y++){
                    if(next[x][y]){
                        g2d.fillRect(x+(getWidth()-next.length),y+(getHeight()/2-next[x].length/2),1,1);
                    }
                }
            }
        }

        updateDisplay();
    });
    private static int requester;
    private static boolean takeOver;
    private static boolean quit;
    private boolean flag;

    DigitalStateChangeListener FOREWORD = event -> {
        if(event.state() == DigitalState.HIGH && !startedApp && !flag){
            if (!(selected + 1 > apps.size() - 1)) {
                selected++;
                flag = true;
            }
        }
        if(event.state() == DigitalState.LOW && !startedApp && flag){
            flag = false;
        }
    };
    DigitalStateChangeListener BACK = event -> {
        if(event.state() == DigitalState.HIGH && !startedApp && !flag){
            if (!(selected - 1 < 0)) {
                selected--;
                flag = true;
            }
        }
        if(event.state() == DigitalState.LOW && !startedApp && flag){
            flag = false;
        }
    };
    DigitalStateChangeListener SELECT = event -> {
        if(event.state() == DigitalState.HIGH && !startedApp && !flag){
            clear();
            startedApp = true;
            menue.stop();
            if (takeOver) {
                if(apps.get(requester).start(button1, button2, button3, g2d)) {
                    System.out.println("Started: " + apps.get(requester).getName());
                }else{
                    //error.start("Cant start application "+apps.get(requester).getName()+" becuse of "+apps.get(requester).getError());
                }
            }else {
                apps.get(selected).start(button1, button2, button3, g2d);
                System.out.println("Starting: "+apps.get(selected).getClass().getSimpleName());
            }
        }
    };
    DigitalStateChangeListener HOME = event -> {
        if(event.state() == DigitalState.HIGH && startedApp){
            clear();
            startedApp = false;
            if(quit) {
                apps.get(requester).stop();
            }else {
                apps.get(selected).stop();
            }
            menue.start();
        }
    };

    Timer buttons = new Timer(100, (a) -> {

        if (button3.isHigh() && !startedApp && !flag) {
            if (!(selected + 1 > apps.size() - 1)) {
                selected++;
                flag = true;
            }
        }
        if (button1.isHigh() && !startedApp && !flag) {
            if (!(selected - 1 < 0)) {
                selected--;
                flag = true;
            }
        }

        if(button1.isLow() && button2.isLow() && flag){
            flag = false;
        }

        if ((button2.isHigh() && !startedApp) || takeOver) {
            clear();
            startedApp = true;
            menue.stop();
            if (takeOver) {
                if(apps.get(requester).start(button1, button2, button3, g2d)) {
                    System.out.println("Started: " + apps.get(requester).getName());
                }else{
                    //error.start("Cant start application "+apps.get(requester).getName()+" becuse of "+apps.get(requester).getError());
                }
            }else {
                apps.get(selected).start(button1, button2, button3, g2d);
                System.out.println("Starting: "+apps.get(selected).getClass().getSimpleName());
            }
        }

        if ((home.isHigh() && startedApp) || quit) {
            clear();
            startedApp = false;
            if(quit) {
                apps.get(requester).stop();
            }else {
                apps.get(selected).stop();
            }
            menue.start();
        }
    });

    public Main() throws IOException, InterruptedException {

        synchronized (this){
            this.wait(1000);
        }

        debug = new Debug();

        Context pi4j = Pi4J.newAutoContext();
        Console console = new Console();

        display = new SSD1306(pi4j, console, 1, 0x3C, "off");

        pages = 8;
        buffer = new byte[128*8];

        System.out.println("Initializing display");

        display.setMemoryAddressMode(SSD1306_Defines.COMMAND_SET_MEM_ADDRESS_MODE_HORZ);

        Arrays.fill(buffer, 0, buffer.length, (byte) valueAllOff);
        display.setDisplayStartLine((byte) 0x00);

        display.sendBuffer(buffer, buffer.length);

        System.out.println("Drawing start image onto g2d");
        for(int x = 0; x < getStartIcon().length; x++){
            for(int y = 0; y < getStartIcon()[x].length; y++){
                if(getStartIcon()[x][y]){
                    g2d.fillRect(x,y,1,1);
                }
            }
        }

        System.out.println("Updating display");
        updateDisplay();

        synchronized (this){
            try {
                this.wait(2500);
            }catch (Exception ignored){}
        }

        clear();
        updateDisplay();


        //DigitalInputProvider digitalInputProvider = pi4j.getProvider("linuxfs");

        button1 = pi4j.din().create(DigitalInput.newConfigBuilder(pi4j)
                .id("button")
                .name("button 1")
                .address(GPIOPin.GPIO27)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
                .provider("pigpio-digital-input")
                .build()
        );//gpio.provisionDigitalInputPin(RaspiPin.GPIO_15, PinPullResistance.PULL_DOWN);

        //button1.addListener(BACK);

        button2 = pi4j.din().create(DigitalInput.newConfigBuilder(pi4j)
                .id("button1")
                .name("button 2")
                .address(GPIOPin.GPIO22)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
                .provider("pigpio-digital-input")
                .build()
        );//gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, PinPullResistance.PULL_DOWN);

        //button2.addListener(SELECT);

        button3 = pi4j.din().create(DigitalInput.newConfigBuilder(pi4j)
                .id("button2")
                .name("button 3")
                .address(GPIOPin.GPIO23)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
                .provider("pigpio-digital-input")
                .build()
        );//gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);

        //button3.addListener(FOREWORD);

        home = pi4j.din().create(DigitalInput.newConfigBuilder(pi4j)
                .id("button3")
                .name("button home")
                .address(GPIOPin.GPIO18)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
                .provider("pigpio-digital-input")
                .build()
        );//gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

        //home.addListener(HOME);

        config = new Settings();

        apps.add(new Spotify());
        apps.add(new BrickBrake());


        apps.add(config);
        apps.add(new ShutDown());

        for(int i = 0; i < apps.size(); i++){
            apps.get(i).id = i;
        }

        buttons.start();
        menue.start();
    }

    int speed = 1000/25;

    public static Settings getConfig(){
        return config;
    }

    public boolean[][] getStartIcon(){
        return new boolean[][]{
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,true,false,false,false,false,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,true,false,false,false,false,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,true,false,false,false,true,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,false,true,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,false,false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,true,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,true},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false}
        };
    }

    Timer update = new Timer(speed, a -> updateDisplay());
    private static BufferedImage bufferImg;

    public static void updateDisplay() {
        dataToBuffer(img);
        //Arrays.fill(buffer, 0, buffer.length, (byte) 0x00);
        display.setDisplayStartLine((byte) 0x00);

        display.sendBuffer(buffer, buffer.length);

        debug.setData(img);
        debug.repaint();
    }

    private static void dataToBuffer(BufferedImage img) {

        bufferImg = rotateImage(img, 180);

        for(int x = 0; x < 128; x++){
            for(int y = 0; y < 64; y++){
                if(bufferImg.getRGB(x,y) == Color.BLACK.getRGB()){
                    buffer[x + (y / 8) * getWidth()] &= ~(1 << (y & 7));
                }else{
                    buffer[x + (y / 8) * getWidth()] |= (1 << (y & 7));
                }
            }
        }
    }

    private static BufferedImage rotateImage(BufferedImage buffImage, double angle) {
        double radian = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radian));
        double cos = Math.abs(Math.cos(radian));

        int width = buffImage.getWidth();
        int height = buffImage.getHeight();

        int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
        int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);

        BufferedImage rotatedImage = new BufferedImage(
                nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = rotatedImage.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        graphics.translate((nWidth - width) / 2, (nHeight - height) / 2);
        // rotation around the center point
        graphics.rotate(radian, (double) (width / 2), (double) (height / 2));
        graphics.drawImage(buffImage, 0, 0, null);
        graphics.dispose();

        return rotatedImage;
    }

    private void clear() {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
    }

    public static void requestTakeOver(int id){
        requester = id;
        takeOver = true;
    }
    public static void requestQuit(int id){
        requester = id;
        quit = true;
    }
}
