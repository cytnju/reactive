package com.muyun.simpledemo;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author muyun.cyt
 * @version 2018/7/10 9:43 PM
 */
public class ReactiveNIO {

    private static int PORT = 8000;

    private static AtomicInteger v = new AtomicInteger(0);

    private static final ExecutorService service = Executors.newFixedThreadPool(16);

    public static void main(String... args) throws IOException {

        Flowable.just(newNioServer())
                .repeat()
                .filter(sel ->sel.selectNow()>=0 )
                .doOnNext(sel -> Flowable.just(sel.selectedKeys().iterator())
                        .filter(selectionKeyIterator -> selectionKeyIterator!=null)
                        .doOnNext(ReactiveNIO::handleKey)
                        //.subscribeOn(Schedulers.newThread())
                        .subscribe())
                .subscribe();


        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void handleKey(Iterator it) throws IOException {
        if (!it.hasNext()) return;
        SelectionKey key = (SelectionKey)it.next();
        it.remove();
        if (!key.isValid()) return;
        if (key.isAcceptable()) {
            Observable.just(key).map(selectionKey -> selectionKey.channel())
                    .cast(ServerSocketChannel.class)
                    .map(serverSocketChannel -> serverSocketChannel.accept())
                    .doOnNext(socketChannel -> socketChannel.configureBlocking(false))
                    .subscribe(socketChannel -> socketChannel.register(key.selector(),SelectionKey.OP_READ));

        } else if (key.isReadable()) {
            Observable.just(key)
                    .subscribe(ReactiveNIO::doReadData);
        }

    }


    public static void doReadData(SelectionKey selectionKey) {
        service.submit(()->{
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (socketChannel.socket().isClosed()) return;
            int count = 0;
            try {
                count = socketChannel.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (count < 0) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                selectionKey.cancel();
                return ;
            }else if (count==0){return;}
            buffer.flip();
            System.out.println(new String(buffer.array()).trim());
        });

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
