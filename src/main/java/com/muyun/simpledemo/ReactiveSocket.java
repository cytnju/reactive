package com.muyun.simpledemo;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author muyun.cyt
 * @version 2018/7/10 8:56 PM
 */
public class ReactiveSocket {

    private static final int PORT = 8000;

    private static final String HOST = "localhost";

    private static final String DATA = "Hello";
    public static void main(String... args) throws IOException, InterruptedException {
        //开启一个服务端接收数据并打印
        Flowable.just(new ServerSocket())
                .zipWith(Flowable.just(new InetSocketAddress(PORT)),(s,add)->bind(add,s))
                .subscribeOn(Schedulers.newThread())
                .map(server -> server.accept())
                .subscribe(ReactiveSocket::receiveData);


        //开启一个客户端发送数据
        Flowable.just(new Socket())
                .zipWith(Flowable.just(new InetSocketAddress(HOST,PORT)),(s,add)->connect(add,s))
                .subscribe(client -> Flowable.just(DATA)
                                            .repeatWhen(t->t.delay(1,TimeUnit.SECONDS))
                                            .map(data ->sendData(client,data))
                                            .map(data->"client send: "+data)
                                            .subscribe(System.out::println));

        Thread.sleep(100000);
//                    .subscribe(ReactiveSocket::sendData);
//            client.map(Flowable.just(DATA).repeat(),(c,data)->c.getOutputStream().write(data.getBytes()))

    }

    public static void receiveData(Socket client) throws IOException {
        DataInputStream is = new DataInputStream(client.getInputStream());
        while(true){
            byte[] buffer = new byte[100];
            if(is.read(buffer)!=-1){
                System.out.println("server receive: "+new String(buffer).trim());
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

    public static String sendData(Socket client,String data) throws IOException {
        client.getOutputStream().write(data.getBytes());
        client.getOutputStream().flush();
        return data;
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
