package me.alien.spotify;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class Server extends Thread{

    ServerSocket serverSocket;
    String code = null;
    public Server(int port) {
        try{
            serverSocket = new ServerSocket(port);
            this.start();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String input = "";
        try {
            //SSLSocket socket;
            Socket socket = serverSocket.accept();

            BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
            String fileRequested = null;

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(socket.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(socket.getOutputStream());

            // get first line of the request from the client
            input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileRequested = parse.nextToken();

            /*System.out.println(input);
            System.out.println("\n");
            System.out.println(method);
            System.out.println("\n");*/
            //System.out.println(fileRequested);
            code = fileRequested.substring(fileRequested.indexOf("=") + 1, fileRequested.lastIndexOf("&"));
            //System.out.println(code);

            socket.close();


        } catch (IOException e) {
            System.out.println(input);
            throw new RuntimeException(e);
        }
    }

    public String getCode() {
        return code;
    }
}
