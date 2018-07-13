package com.muyun.simpledemo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author muyun.cyt
 * @version 2018/7/12 11:52 AM
 */
public class SocketDemo {

    public static void main(String... args) throws IOException {
        ServerSocket server = new ServerSocket(8000);
        Socket client = server.accept();
        BufferedInputStream bin = new BufferedInputStream(client.getInputStream());
        byte[] buf = new byte[100];
        while(bin.read(buf)!=-1){
            System.out.println(new String(buf).trim());

        }



    }
}
