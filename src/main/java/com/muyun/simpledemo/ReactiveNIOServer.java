package com.muyun.simpledemo;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import io.reactivex.internal.operators.flowable.FlowableAll;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author muyun.cyt
 * @version 2018/7/10 9:43 PM
 */
public class ReactiveNIOServer {

    private static int PORT = 8000;

    private static AtomicInteger v = new AtomicInteger(0);

    private static final ExecutorService service = Executors.newFixedThreadPool(16);

    private static final ExecutorService dataService = Executors.newFixedThreadPool(20);

    public static void main(String... args) throws IOException {

        Flowable.just(newNioServer())
                .repeat()
                .filter(sel ->sel.selectNow()>=0 )
                .flatMap(selector -> FlowableAll.just(selector.selectedKeys()))
                .flatMap(selectionKeys -> Flowable.just(selectionKeys.iterator()))
                .subscribeOn(Schedulers.newThread())
                .flatMap(it -> Flowable.fromArray(ReactiveNIOServer.handleKey(it)))
                .filter(s -> s!=null&&!"".equals(s))
                .observeOn(Schedulers.from(dataService))
                .map(ReactiveNIOServer::doDataPorcess1)
                .map(ReactiveNIOServer::doDataPorcess2)
                .map(ReactiveNIOServer::doDataPorcess3)
                .subscribe(System.out::println,throwable -> {});

        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static String handleKey(Iterator it) throws ExecutionException, InterruptedException {
        if (!it.hasNext()) {return "";}
        SelectionKey key = (SelectionKey)it.next();
        it.remove();
        if (!key.isValid()) {return "";}
        if (key.isAcceptable()) {
            Observable.just(key)
                    .map(selectionKey -> selectionKey.channel())
                    .cast(ServerSocketChannel.class)
                    .map(serverSocketChannel -> serverSocketChannel.accept())
                    .doOnNext(socketChannel -> socketChannel.configureBlocking(false))
                    .subscribe(socketChannel -> socketChannel.register(key.selector(),SelectionKey.OP_READ));
            return "";

        } else if (key.isReadable()) {
            return doReadData(key).get();
        }
        return "";
    }


    public static Future<String> doReadData(SelectionKey selectionKey) {
        return service.submit(()->{
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (socketChannel.socket().isClosed()) { return null;}
            int count = 0;
                count = socketChannel.read(buffer);
            if (count < 0) {
                socketChannel.close();
                selectionKey.cancel();
                return null;
            }else if (count==0){return null;}
            buffer.flip();
            String data = new String(buffer.array()).trim();
            //System.out.println(data);
            return  data;
        });

    }

    public static String doDataPorcess1(String data){
        return data+" =>processor 1";
    }

    public static String doDataPorcess2(String data){
        return data+" =>processor 2";
    }

    public static String doDataPorcess3(String data){
        return data+" =>processor 3";
    }



    private static Selector newNioServer() throws IOException {
        Selector selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        return selector;
    }

}
