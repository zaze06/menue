package me.alien.spotify;

import com.google.gson.JsonObject;
import com.pi4j.io.gpio.digital.DigitalInput;
import me.alien.menu.App;
import me.alien.menu.Main;
import org.json.JSONArray;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.detailed.ServiceUnavailableException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.AddItemToUsersPlaybackQueueRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import static me.alien.menu.Main.updateDisplay;

public class Spotify extends App  /*extends JPanel*/{
    JSONObject config = Main.getConfig().getConfig("spotify");
    String name = "";
    boolean scrol = false;
    String fontname;
    Graphics2D g2d;
    int point = 0;
    String old = "";
    JSONObject item;
    CurrentlyPlaying currentlyPlaying;
    boolean isPi;
    JFrame frame;
    boolean init = false;
    CurrentlyPlayingContext playback;
    int volume;
    String code;
    java.util.Timer timer = new java.util.Timer();
    int valid = 0;
    int creatorScrol = 0;
    boolean loading;
    int que = 0;

    @Override
    public boolean[][] getIcon() {
        return new boolean[][]{
            {false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false},
            {false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false},
            {false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false},
            {false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
            {false,false,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
            {false,true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,false,true,true,true,true,true,true,false},
            {false,true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,true,true,true,true,true,true,true,false},
            {true,true,true,true,true,true,false,false,true,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,false,false,true,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,false,false,true,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,false,false,true,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,true,false,false,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,false,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,false,true,true,true,true,true,true,true},
            {true,true,true,true,true,true,true,false,false,true,true,true,false,false,true,true,false,false,true,true,true,true,true,true},
            {false,true,true,true,true,true,true,true,false,false,true,true,false,false,true,true,true,true,true,true,true,true,true,false},
            {false,true,true,true,true,true,true,true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            {false,false,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
            {false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
            {false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false},
            {false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false},
            {false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false}
        };
    }

    Timer t = new Timer((1000/40), (a) -> {
        if(isPi) {
            update();
        }else{
            //repaint();
        }
    });

    int[][] play = {
            {0,0,0,0,0,0,0,0,0},
            {0,1,1,0,0,0,0,0,0},
            {0,1,1,1,1,0,0,0,0},
            {0,1,1,1,1,1,1,0,0},
            {0,1,1,1,1,1,1,1,0},
            {0,1,1,1,1,1,1,1,0},
            {0,1,1,1,1,1,1,0,0},
            {0,1,1,1,1,0,0,0,0},
            {0,1,1,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0}
    };

    int[][] pause = {
            {0,0,0,0,0,0,0,0,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,1,1,0,0,0,1,1,0},
            {0,0,0,0,0,0,0,0,0}
    };

    public int[][][] getLoadingIcon() {
        return new int[][][]{
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 1, 1, 0, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 1, 0, 0, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 0, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 0, 0, 1, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 0, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 0, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                },
                {
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0},
                        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0}
                }
        };
    }

    int position;
    private AtomicReference<GetUsersCurrentlyPlayingTrackRequest> curentlyPlayingTrack;
    private DigitalInput skipButton;
    private DigitalInput playButton;
    private DigitalInput repeatButton;
    private SpotifyApi spotifyApi;

    public void update(){
        try {
            clear();

            if(!loading) {

                // Displays the artists of the current playing song

                JSONArray array = item.getJSONObject("item").getJSONArray("artists");

                g2d.setFont(new Font(fontname, Font.PLAIN, (isPi ? 8 : 36)));

                ArrayList<String> atristList = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject artis = array.getJSONObject(i);
                    atristList.add(artis.getString("name"));
                }

                for (int i = 0; i < atristList.size(); i++) {
                    g2d.drawString(atristList.get(i), 0, (i + 1) * (isPi ? 8 : 48) + (isPi ? 17 : 114)/* + (atristList.size() > 1 ? creatorScrol : 0)*/);
                }
                if (atristList.size() > 1) {
                    creatorScrol--;
                    if (((atristList.size() - 1) * (isPi ? 8 : 48) + (isPi ? 17 : 114) - creatorScrol) < -25) {
                        creatorScrol = 0;
                    }
                }

                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, (isPi ? 128 : 0/*getWidth()*/), (isPi ? 32 - 17 : 0/*getHeight()*/));
                g2d.setColor(Color.WHITE);

                // displays the current song name

                if (scrol) {
                    g2d.setFont(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60)));

                    String spacing = " ".repeat(3);
                    g2d.drawString(name + spacing, ((currentlyPlaying.getIs_playing()) ? -point : 0), (isPi ? 10 : 50));
                    if (g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name + spacing) - point < (isPi ? 119 : 0 /*getWidth()*/)) {
                        g2d.drawString(name + spacing, ((currentlyPlaying.getIs_playing()) ? g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name + spacing) - point : 0), (isPi ? 10 : 50));
                        //System.out.println(g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi?10:60))).stringWidth(name)-point);
                    }
                    point++;
                    if (point > g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name + spacing)) {
                        point -= g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name + spacing);
                    }

                } else {

                    g2d.setFont(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60)));

                    g2d.drawString(name, 0, (isPi ? 10 : 50));
                }

                g2d.setColor(Color.BLACK);
                g2d.fillRect(119, 0, 9, 20);
                g2d.setColor(Color.WHITE);

                if(currentlyPlaying.getIs_playing()){
                    for(int x = 0; x < 9; x++){
                        for(int y = 0; y < 10; y++){
                            if(pause[y][x] == 1){
                                g2d.fillRect(x+120,y,1,1);
                            }
                        }
                    }
                }else{
                    for(int x = 0; x < 9; x++){
                        for(int y = 0; y < 10; y++){
                            if(play[y][x] == 1){
                                g2d.fillRect(x+120,y,1,1);
                            }
                        }
                    }
                }

                //g2d.setFont(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60)));

                //g2d.drawString(que+"", 120, 10);


                // Displays a progress bar and a percentage of the playing song

                Integer progress_ms = currentlyPlaying.getProgress_ms();
                int totalTime = item.getJSONObject("item").getInt("duration_ms");
                int v = (int) ((progress_ms + 0.0) / totalTime * 100);
                if (!isPi) {
                    g2d.setColor(Color.GREEN);
                }
                g2d.drawRect(0, (isPi ? 11 : 70), (isPi ? 100 : 300), (isPi ? 5 : 30));
                g2d.fillRect(0, (isPi ? 11 : 70), v * (isPi ? 1 : 3), (isPi ? 5 : 30));

                //g2d.drawRect((isPi?105:320), (isPi?17:100), (isPi?10:30), (isPi?50:50));
                //g2d.fillRect((isPi?105:320), (isPi?17:100), (isPi?10:30), -((volume/2)*(isPi?1:10)));

                g2d.setFont(new Font(fontname, Font.PLAIN, (isPi ? 8 : 48)));
                g2d.drawString(v + "%", (isPi ? 103 : 306), (isPi ? 17 : 100));

                if (!isPi) {
                    g2d.setColor(Color.WHITE);
                }

            }else {
                g2d.setFont(new Font(fontname, Font.PLAIN, (isPi ? 15 : 60)));
                g2d.drawString("Loading", (isPi?128/2:0/*getWidth()*//2)-(g2d.getFontMetrics().stringWidth("Loading")/2), 15);
                for(int x = 0; x < 10; x++){
                    for(int y = 0; y < 10; y++){
                        if(getLoadingIcon()[position][y][x] == 1) {
                            g2d.fillRect(x + (128 / 2 - 5), y + 16, 1, 1);
                        }
                    }
                }
                position++;
                if(position >= getLoadingIcon().length){
                    position = 0;
                }
            }

            updateDisplay();
        }catch (Exception e){
            //throw new RuntimeException(e);
            int z = 0;
        }
    }

    boolean[] flags = {false, false, false};

    Timer buttons = new Timer((1000/40), (a) -> {
        try {
            if (skipButton.isHigh() && !flags[0]) {
                flags[0] = true;
                spotifyApi.skipUsersPlaybackToNextTrack().build().execute();

                curentlyPlayingTrack.set(spotifyApi.getUsersCurrentlyPlayingTrack().build());
                currentlyPlaying = curentlyPlayingTrack.get().execute();
                if (currentlyPlaying != null) {
                    name = currentlyPlaying.getItem().getName();
                    this.item = new JSONObject(curentlyPlayingTrack.get().getJson());

                    playback = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
                    if (playback != null) {
                        volume = playback.getDevice().getVolume_percent();
                    }

                    if (!name.equalsIgnoreCase(old)) {
                        System.out.println("new song: " + name + ". width: " + g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name));
                        if (g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name) > (isPi ? 119 : 0/*getWidth()*/)) {
                            scrol = true;
                            //point = 0;
                            //System.out.println("Song name to wide, enabling scrolling title. width: "+g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi?10:60))).stringWidth(name));
                        } else {
                            scrol = false;
                            //point = 0;
                        }
                        old = name;
                    }
                }
            } else if (skipButton.isLow() && flags[0]) {
                flags[0] = false;
            }

            if (playButton.isHigh() && !flags[1]) {
                if (!currentlyPlaying.getIs_playing()) {
                    JsonObject offset = new JsonObject();
                    //offset.addProperty("position", this.item.getJSONObject("item").getInt("track_number"));
                    offset.addProperty("uri", playback.getItem().getUri());

                    StartResumeUsersPlaybackRequest play = spotifyApi.startResumeUsersPlayback()
                            .position_ms(currentlyPlaying.getProgress_ms())
                            .device_id(playback.getDevice().getId())
                            .offset(offset)
                            .context_uri(playback.getContext().getUri())
                            .build();
                    play.execute();
                    curentlyPlayingTrack.set(spotifyApi.getUsersCurrentlyPlayingTrack().build());
                    currentlyPlaying = curentlyPlayingTrack.get().execute();
                    if (currentlyPlaying != null) {
                        name = currentlyPlaying.getItem().getName();
                        this.item = new JSONObject(curentlyPlayingTrack.get().getJson());

                        playback = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
                        if (playback != null) {
                            volume = playback.getDevice().getVolume_percent();
                        }

                        if (!name.equalsIgnoreCase(old)) {
                            System.out.println("new song: " + name + ". width: " + g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name));
                            if (g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name) > (isPi ? 119 : 0/*getWidth()*/)) {
                                scrol = true;
                                //point = 0;
                                //System.out.println("Song name to wide, enabling scrolling title. width: "+g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi?10:60))).stringWidth(name));
                            } else {
                                scrol = false;
                                //point = 0;
                            }
                            old = name;
                        }
                    }
                } else {
                    PauseUsersPlaybackRequest pause = spotifyApi.pauseUsersPlayback()
                            .device_id(playback.getDevice().getId())
                            .build();
                    pause.execute();
                    curentlyPlayingTrack.set(spotifyApi.getUsersCurrentlyPlayingTrack().build());
                    currentlyPlaying = curentlyPlayingTrack.get().execute();
                    if (currentlyPlaying != null) {
                        name = currentlyPlaying.getItem().getName();
                        this.item = new JSONObject(curentlyPlayingTrack.get().getJson());

                        playback = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
                        if (playback != null) {
                            volume = playback.getDevice().getVolume_percent();
                        }

                        if (!name.equalsIgnoreCase(old)) {
                            System.out.println("new song: " + name + ". width: " + g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name));
                            if (g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name) > (isPi ? 119 : 0/*getWidth()*/)) {
                                scrol = true;
                                //point = 0;
                                //System.out.println("Song name to wide, enabling scrolling title. width: "+g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi?10:60))).stringWidth(name));
                            } else {
                                scrol = false;
                                //point = 0;
                            }
                            old = name;
                        }
                    }
                }
                flags[1] = true;
            } else if (playButton.isLow() && flags[0]) {
                flags[1] = false;
            }

            if(repeatButton.isHigh() && !flags[2]){
                AddItemToUsersPlaybackQueueRequest addItemToUsersPlaybackQueueRequest = spotifyApi.addItemToUsersPlaybackQueue(currentlyPlaying.getItem().getUri())
                        .device_id(playback.getDevice().getId())
                        .build();
                addItemToUsersPlaybackQueueRequest.execute();
                flags[2] = true;
            } else if(repeatButton.isLow() && flags[2]){
                flags[2] = false;
            }

        }catch (Exception ignored){}
    });

    public boolean start(DigitalInput button1, DigitalInput button2, DigitalInput button3, Graphics2D g2d) {
        try {
            isPi = true;

            skipButton = button2;
            playButton = button1;
            repeatButton = button3;

            int size = 2;

            size = 1;

            this.g2d = g2d;

            fontname = g2d.getFont().getFontName();


            g2d.setFont(new Font(fontname, Font.PLAIN, 8));
            g2d.drawString("Starting spotify player", 0, 10);
            g2d.drawString("--", 128 - g2d.getFontMetrics().stringWidth("--") / 2, 20);

            updateDisplay();

            final int[] i = {0};
            AtomicReference<String> info = new AtomicReference<>("");
            Timer t1 = new Timer((1000 / 40), (a) -> {

/*            String str = switch (i[0]){
                case 0: return "/";
                case 1: "-"; break;
                case 2: "\\"; break;
                case 3: "|"; break;
                default: "#"; break;
           };*/

                clear();

                g2d.drawString("Starting spotify player", 0, 10);
                //g2d.drawString(str,(onPi?128/2:0/*getWidth()*//2)-(g2d.getFontMetrics().stringWidth(str)/2),(isPi?20:36));
                for (int x = 0; x < 10; x++) {
                    for (int y = 0; y < 10; y++) {
                        if (getLoadingIcon()[i[0]][y][x] == 1) {
                            g2d.fillRect(x + (128 / 2 - 5), 11 + y, 1, 1);
                        }
                    }
                }
                g2d.drawString(info.get(), (128 / 2) - (g2d.getFontMetrics().stringWidth(info.get()) / 2), 30);
                updateDisplay();
                i[0]++;
                if (i[0] >= getLoadingIcon().length) {
                    i[0] = 0;
                }
                //System.out.println("Timer run");
            });
            t1.start();
            //System.out.println("Timer one is "+t1.isRunning());

            spotifyApi = SpotifyApi.builder()
                    .setClientId(SpotifyTokens.CLIENT_ID) // replace CLIENT_ID whit a spotify client id
                    .setClientSecret(SpotifyTokens.CLIENT_SECRET) // replace CLIENT_SECRET whit a spotify client secret
                    //.setAccessToken(object.getString("token"))
                    .setRedirectUri(new URI("http://raspberrypi:8080/"))
                    .build();

            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = null;

            if(!config.has("refresh_token")) {
                InetAddress localhost = InetAddress.getLocalHost();
                System.out.println(localhost.getHostName());

                //ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                //        .build();

                AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                        .state("x4xkmn9pu3j6ukrs8n")
                        .scope("user-read-currently-playing,user-modify-playback-state,user-read-playback-state")
                        //.show_dialog(true)
                        .build();

                URI uri = authorizationCodeUriRequest.execute();

                //System.out.println(uri.toString());

                //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                info.set("Waiting for Spotify code");
                System.out.println("Waiting for Spotify code");

                Server server = new Server(8080);

                System.out.println(uri.toString());

                while (true) {
                    if ((code = server.getCode()) != null) {
                        break;
                    }
                    synchronized (this) {
                        this.wait(500);
                    }
                }

                try {
                    server.serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //code = in.readLine();


                info.set("Got spotify code. Initializing");
                System.out.println("Got Spotify code. Initializing");


                AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                        .build();

                while (true) {
                    try {
                        final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

                        // Set access and refresh token for further "spotifyApi" object usage
                        spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                        spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                        System.out.println("Got access token");
                        break;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        info.set("Failed to generate token. retrying");
                    }
                    synchronized (this) {
                        this.wait(10000);
                    }
                }

                authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                        .build();

                config.put("refresh_token", spotifyApi.getRefreshToken());
                Main.getConfig().setConfig("spotify", config);
            }
            else {
                spotifyApi.setRefreshToken(config.getString("refresh_token"));
                authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                        .build();
                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                // Set access and refresh token for further "spotifyApi" object usage
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            }

            int initialDelay = 3600;
            valid = initialDelay;
            SpotifyApi finalSpotifyApi = spotifyApi;
            //AuthorizationCodeRefreshRequest finalAuthorizationCodeRefreshRequest = authorizationCodeRefreshRequest;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCodeRefresh().build().execute();
                        // Set access and refresh token for further "spotifyApi" object usage
                        finalSpotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

                        //valid = authorizationCodeCredentials.getExpiresIn();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }, initialDelay, valid);

            //g2d.setFont(Font.createFont(0, getClass().getResourceAsStream("/simsum.ttc")));

            int artistsscroll = 0;

            System.out.println("Getting current playing before stating API request loop");

            curentlyPlayingTrack = new AtomicReference<>(spotifyApi.getUsersCurrentlyPlayingTrack().build());
            while (true) {
                try {
                    currentlyPlaying = curentlyPlayingTrack.get().execute();
                    if (currentlyPlaying != null) {
                        name = currentlyPlaying.getItem().getName();
                        this.item = new JSONObject(curentlyPlayingTrack.get().getJson());
                        System.out.println("Got song: "+name);
                        break;
                    } else {
                        //System.out.println("Failed to get current song, waiting for 10 seconds before retrying");
                        info.set("Waiting for song to play");
                        System.out.println(info.get());
                        synchronized (this) {
                            this.wait(10000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    info.set("Failed to get current song, retrying");
                    System.out.println(info.get());
                    synchronized (this) {
                        this.wait(10000);
                    }
                }
            }

            System.out.println("Starting display");

            t1.stop();
            t.start();
            buttons.start();

            System.out.println("Starting API request loop");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        curentlyPlayingTrack.set(spotifyApi.getUsersCurrentlyPlayingTrack().build());
                        currentlyPlaying = curentlyPlayingTrack.get().execute();
                        if (currentlyPlaying != null) {
                            name = currentlyPlaying.getItem().getName();
                            item = new JSONObject(curentlyPlayingTrack.get().getJson());

                            playback = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
                            if (playback != null) {
                                volume = playback.getDevice().getVolume_percent();
                            }

                            if (!name.equalsIgnoreCase(old)) {
                                System.out.println("new song: " + name + ". width: " + g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name));
                                if (g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi ? 10 : 60))).stringWidth(name) > (isPi ? 119 : 0/*getWidth()*/)) {
                                    scrol = true;
                                    //point = 0;
                                    //System.out.println("Song name to wide, enabling scrolling title. width: "+g2d.getFontMetrics(new Font(fontname, Font.PLAIN, (isPi?10:60))).stringWidth(name));
                                } else {
                                    scrol = false;
                                    //point = 0;
                                }
                                old = name;
                            }

                            if (loading) {
                                loading = false;
                            }


                            /*synchronized (this) {
                                if (curentlyPlayingTrack.get().execute().getIs_playing()) {
                                    this.wait(100);
                                } else {
                                    this.wait(600);
                                }
                            }*/
                        } else {
                            //System.out.println("Failed to get current song, waiting for 10 seconds before retrying");
                            loading = true;
                            //synchronized (this) {
                                //this.wait(10000);
                            //}
                        }
                    } catch (ServiceUnavailableException e) {
                        //System.out.println("Failed to get current song, waiting for 10 seconds before retrying");
                        System.out.println(e.getMessage());
                        loading = true;
                        //synchronized (this) {
                            /*try {
                                this.wait(10000);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }*/
                        //}
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }, 0,100);

            //skipButton.setShutdownOptions(true);
            return true;

        }catch (Exception e){
            System.out.println("Error: "+e.getMessage());
            System.out.println("Quiting program");
            return false;
        }
    }

    @Override
    public void stop() {
        //timer.cancel();
        buttons.stop();
        t.stop();
    }

    private void clear() {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, (isPi?128:0/*getWidth()*/), (isPi?128:0/*getHeight()*/));
        g2d.setColor(Color.WHITE);
    }
}
