package com.muyun.simpledemo;


import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author muyun.cyt
 * @version 2018/7/11 10:42 AM
 */
public class ReactiveServer {

    private static ServerSocket server;

    private static InetSocketAddress address;

    private static int PORT = 8000;

//    private static ExecutorService es = Executors.newFixedThreadPool(10, new ThreadFactory() {
//        private AtomicInteger count = new AtomicInteger(0);
//        @Override
//        public Thread newThread(Runnable r) {
//            Thread thread = new Thread(r,"ReceiveThread"+count.incrementAndGet());
//            System.out.println("receive thread :"+count.get());
//            return thread;
//        }
//    });

    public static void main(String... args) throws IOException, InterruptedException {

        server = new ServerSocket();
        address = new InetSocketAddress(PORT);
        Observable.just(new ServerSocket())
                .zipWith(Observable.just(new InetSocketAddress(PORT)),ReactiveServer::bind)
                .subscribe(s-> Observable
                        .fromCallable(new SocketCallable(s))
                        .repeat()
                        .subscribeOn(Schedulers.newThread())
                       .subscribe(client->Observable.just(client)
                                  .subscribeOn(Schedulers.io())
                                  .subscribe(ReactiveServer::doReceiveData)));






        Thread.sleep(1000000);

    }






    public static ServerSocket bind(ServerSocket server,SocketAddress address) throws IOException {
        server.bind(address);
        return server;
    }


     static class SocketCallable implements Callable<Socket> {
         private ServerSocket server ;
         public SocketCallable(ServerSocket server) {
            this.server = server;
         }
         @Override
         public Socket call() throws Exception {
             return server.accept();
         }
    }

    public static void doReceiveData(Socket client){
        System.out.println("receive data start!  "+Thread.currentThread().getName());
        try {
            DataInputStream is = new DataInputStream(client.getInputStream());

            byte[] buffer = new byte[100];
            //client.getInputStream().read(buffer);
            while(is.read(buffer)!=-1){
                System.out.println(new String(buffer).trim());
            }
            System.out.println("receive data end!  "+Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startReceiveDataWork(Socket client) throws IOException {
        //es.submit(new ReceiveDataTask(client));
        //Observable.just(client).subscribeOn(Schedulers.from(es)).subscribe(ReactiveServer::doReceiveData);
    }

    public static void receiveDataOnce(Socket client) throws IOException {
        DataInputStream is = new DataInputStream(client.getInputStream());
        byte[] buffer = new byte[100];
        if(is.read(buffer)!=-1){
            System.out.println(new String(buffer).trim());
        }
        is.close();
        client.close();
    }

}
