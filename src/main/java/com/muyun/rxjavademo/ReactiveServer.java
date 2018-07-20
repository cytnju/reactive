package com.muyun.rxjavademo;


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


/**
 * @author muyun.cyt
 * @version 2018/7/11 10:42 AM
 */
public class ReactiveServer {

    private static ServerSocket server;

    private static InetSocketAddress address;

    private static int PORT = 8000;

    static ExecutorService service = Executors.newFixedThreadPool(100);

    public static void main(String... args) throws IOException, InterruptedException {
        Flowable.just(new ServerSocket())
                .zipWith(Flowable.just(new InetSocketAddress(PORT)),ReactiveServer::bind)
                .subscribeOn(Schedulers.computation())
                .flatMap(serverSocket -> Flowable.fromCallable(new SocketCallable(serverSocket)).repeatUntil(()->serverSocket.isClosed()))
                .subscribe(client->Observable.just(client)
                        .observeOn(Schedulers.io())
                        .subscribe(ReactiveServer::doReceiveData));


        Thread.sleep(1000000);

    }


    /**
     * ReactiveServer#bind
     *
     * bind the address to server
     * @param ServerSocket
     * @param SocketAddress
     */

    public static ServerSocket bind(ServerSocket server,SocketAddress address) throws IOException {
        server.bind(address,100);
        return server;
    }


    /**
     * ReactiveServer$SocketCallable
     * return a client that the ServerSocket accept
     * @param ServerSocket
     * @return Socket
     */

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

    /**
     * ReactiveServer#doReceiveData
     *
     * do receive data
     * @param Socket client
     */

    public static void doReceiveData(Socket client) throws IOException {
        DataInputStream is = new DataInputStream(client.getInputStream());

        byte[] buffer = new byte[100];
        while(is.read(buffer)!=-1){
            System.out.println("server receive: "+new String(buffer).trim());
        }
    }

}
