package com.muyun.rxjavademo;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author muyun.cyt
 * @version 2018/7/11 11:16 AM
 */
public class ReactiveClient {
    private static final int PORT = 8000;

    private static final String HOST = "localhost";

    private static final String DATA = "hello";

    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String... args) throws InterruptedException {

        Flowable.range(1,1000)
                .flatMap(integer -> Flowable.just(new Socket()))
                .map(socket -> connect(socket,new InetSocketAddress(HOST,PORT)))
                .subscribe(client -> Observable.just(DATA)
                                .repeatWhen(t->t.delay(10,TimeUnit.MILLISECONDS))
                                .map(data ->doSendData(client,data+count.incrementAndGet()))
                                .map(data->"client send: "+data)
                                .subscribe(System.out::println));


        Thread.sleep(1000000);

    }


    /**
     * ReactiveClient#connect
     *
     * bind the address to server
     * @param Socket client
     * @param SocketAddress address
     */

    public static Socket connect(Socket client,SocketAddress address) throws IOException {
        client.connect(address);
        return client;
    }

    /**
     * ReactiveClient#doSendData
     *
     * do send data
     * @param Socket client
     * @param String data
     */

    public static String doSendData(Socket client,String data) throws IOException {
        client.getOutputStream().write(data.getBytes());
        client.getOutputStream().flush();
        return data;
    }

//    public static void sendData(Socket client) throws IOException {
//        while(true){
//            client.getOutputStream().write("hello".getBytes());
//            client.getOutputStream().flush();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void sendDataOnce(Socket client) throws IOException {
//            client.getOutputStream().write("hello".getBytes());
//            client.getOutputStream().flush();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            client.getOutputStream().close();
//            client.close();
//    }

}
