package com.muyun.simpledemo;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author muyun.cyt
 * @version 2018/7/10 8:56 PM
 */
public class ReactiveSocket {

    private static final int PORT = 8000;

    private static final String HOST = "localhost";

    public static void main(String... args) {
        try {
            Observable.just(new ServerSocket())
                    .zipWith(Observable
                            .just(new InetSocketAddress(PORT)),(s,add)->bind(add,s))
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(ReactiveSocket::receiveData);

            Thread.sleep(1000);

            Observable.just(new Socket())
                    .zipWith(Observable
                            .just(new InetSocketAddress(HOST,PORT)),(s,add)->connect(add,s))
                    .subscribe(ReactiveSocket::sendData);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void receiveData(ServerSocket server) throws IOException {
        Socket client = server.accept();
        DataInputStream is = new DataInputStream(client.getInputStream());
        while(true){
            byte[] buffer = new byte[100];
            if(is.read(buffer)!=-1){
                System.out.println(new String(buffer).trim());
            }
        }
    }

    public static void sendData(Socket client) throws IOException {
        while(true){
            client.getOutputStream().write("hello".getBytes());
            client.getOutputStream().flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static ServerSocket bind(SocketAddress address,ServerSocket server) throws IOException {
        server.bind(address);
        return server;
    }

    public static Socket connect(SocketAddress address,Socket client) throws IOException {
        client.connect(address);
        return client;
    }
}
