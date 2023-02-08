package me.alien.menu;

import com.pi4j.io.gpio.digital.DigitalInput;

import java.awt.*;

public abstract class App {
    /**
     * contains the default icon used if the app doesn't return its own icon
     */
    private static final boolean[][] defaultIcon = {
            {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
            {false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            {false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
            {false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,true,false},
            {false,true,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false},
            {false,true,false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,true,false},
            {false,true,false,false,false,false,true,false,false,false,false,false,false,true,false,false,false,false,true,false},
            {false,true,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,true,false},
            {false,true,false,false,false,false,false,false,true,false,false,true,false,false,false,false,false,false,true,false},
            {false,true,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,true,false},
            {false,true,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,true,false},
            {false,true,false,false,false,false,false,false,true,false,false,true,false,false,false,false,false,false,true,false},
            {false,true,false,false,false,false,false,true,false,false,false,false,true,false,false,false,false,false,true,false},
            {false,true,false,false,false,false,true,false,false,false,false,false,false,true,false,false,false,false,true,false},
            {false,true,false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,true,false},
            {false,true,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false},
            {false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,true,false},
            {false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
            {false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false}
    };

    /**
     * Always returns the default icon <br>
     * This is only used by the main program if the application's {@link #getIcon()} returns too big or too small icon
     * @return {@link #defaultIcon}
     */
    public static boolean[][] getDefaultIcon(){
        return defaultIcon;
    }

    /**
     * returns the icon used for the app select menu of the main program. this icon should not be bigger than 40x40 and not smaller then 10x10
     * @return a boolean array in two dimensions that contains the icon described by true or false values
     */
    public boolean[][] getIcon(){
        return defaultIcon;
    }

    /**
     * Used to start the application, this should start all necessary proses the application need. but it should not contain a while true loop that doesn't quit
     * @param button1 the first button used by the main program
     * @param button2 the second button used by the main program
     * @param button3 the third button used by the main program
     * @param g2d the {@link Graphics2D} interface used to display data onto the screen
     * @return a {@link Boolean} describing <br> true: the application started successfully <br> false: the application failed to start and requires quiting
     */
    public abstract boolean start(DigitalInput button1, DigitalInput button2, DigitalInput button3, Graphics2D g2d);

    /**
     * Used to safely exit an application after this method is called the application should not put more data onto the {@link Graphics2D} provided in the {@link #start(DigitalInput, DigitalInput, DigitalInput, Graphics2D)} method
     */
    public abstract void stop();

    /**
     * The name of the application
     * @return {@link String} containing the name of the application
     */
    public String getName(){
        return this.getClass().getSimpleName();
    }

    /**
     * A method to give the error if the application cant be started.
     * @return {@link String} containing the reason for the crash or failed start
     */
    public String getError(){
        return "Unknown error";
    }

    /**
     * Never modify this value on your own, read but never write to it! it's assigned by the main program
     */
    public int id = 0;
}
