package me.alien.spotify;

import java.io.*;

public class Loader {
    public static String loadFile(String s) {
        return loadFile(s, "");
    }

    public static String loadFile(String s, String newLine){
        return loadFile(s, newLine, null);
    }

    public static String loadFile(String s, String newLine, String backupFile){
        try {
            File file = new File(s);
            if(!file.exists()){
                file.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                BufferedReader in = new BufferedReader(new FileReader(backupFile));
                String tmp;
                while((tmp = in.readLine())!=null){
                    out.write(out+"\n");
                }
            }
            BufferedReader in = new BufferedReader(new FileReader(file));
            StringBuilder data = new StringBuilder();
            String tmp = "";
            while((tmp=in.readLine())!=null){
                data.append(tmp);
                data.append(newLine);
            }
            return data.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}