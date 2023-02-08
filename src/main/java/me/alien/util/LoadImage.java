package me.alien.util;

import java.io.DataInputStream;
import java.io.FileInputStream;

public class LoadImage {
    public static boolean[][] getImageFromResources(String image){
        try {
            DataInputStream in = new DataInputStream(LoadImage.class.getResourceAsStream("/" + image));
            int width = in.read();
            int height = in.read();
            boolean[][] img = new boolean[width][height];
            int x = 0;
            int y = 0;
            for (int i = 0; i < width * height; i++) {
                img[x][y] = in.read() == 1;
                x++;
                if(x > width-1){
                    y++;
                    x=0;
                }
            }
            return img;
        }catch (Exception e){
            return new boolean[][]{
                    {true, false, true},
                    {false, true, false},
                    {true, false, true},
            };
        }
    }
}
