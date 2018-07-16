package com.muyun.simpledemo;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
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

        Observable.range(1,100)
                .subscribeOn(Schedulers.computation())
                .map(i->Observable.just(new Socket())
                        .zipWith(Observable.just(new InetSocketAddress(HOST,PORT)),ReactiveClient::connect)
                        .subscribeOn(Schedulers.io())
                        .subscribe(client -> Observable.just(DATA)
                                .repeatWhen(t->t.delay(1,TimeUnit.SECONDS))
                                .map(data ->doSendData(client,data+count.incrementAndGet()))
                                .map(data->"client send: "+data)
                                .subscribe(System.out::println)))
                .subscribe();
//                        .subscribe(c->Observable.just(c.getOutputStream())
//                                .repeatWhen(o->o.delay(1,TimeUnit.SECONDS))
//                                .doOnNext(out->out.write((DATA+count.incrementAndGet()).getBytes()))
//                                .doOnNext(o -> System.out.println("client send :"+DATA+count.get()))
//                                .subscribe(OutputStream::flush)))
//                .subscribe();

        Thread.sleep(1000000);

    }

    public static Socket connect(Socket client,SocketAddress address) throws IOException {
        client.connect(address);
        return client;
    }

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
