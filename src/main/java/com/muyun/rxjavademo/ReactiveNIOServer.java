package com.muyun.rxjavademo;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import io.reactivex.internal.operators.flowable.FlowableAll;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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
                .subscribeOn(Schedulers.single())
                .flatMap(it -> Flowable.fromArray(ReactiveNIOServer.handleKey(it)))
                .filter(s -> s!=null&&!s.getStr().equals(""))
                .observeOn(Schedulers.from(dataService))
                .map(ReactiveNIOServer::doDataPorcess1)
                .map(ReactiveNIOServer::doDataPorcess2)
                .map(ReactiveNIOServer::doDataPorcess3)
                .doOnNext(ReactiveNIOServer::registerWriteEvent)
                .subscribe(System.out::println,Throwable::printStackTrace);


        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ReactiveNIOServer#handleKey
     *
     * handle the selectionKey
     * @param Iterator selectionKey
     * @return DataEntity
     */

    public static DataEntity handleKey(Iterator it) throws ExecutionException, InterruptedException, ClosedChannelException {
        if (!it.hasNext()) {return emptyData();}
        SelectionKey key = (SelectionKey)it.next();
        it.remove();
        if (!key.isValid()) {return emptyData();}
        if (key.isAcceptable()) {
            Observable.just(key)
                    .map(selectionKey -> selectionKey.channel())
                    .cast(ServerSocketChannel.class)
                    .map(serverSocketChannel -> serverSocketChannel.accept())
                    .doOnNext(socketChannel -> socketChannel.configureBlocking(false))
                    .subscribe(socketChannel -> socketChannel.register(key.selector(),SelectionKey.OP_READ));
            return emptyData();

        } else if (key.isReadable()) {
            return doReadData(key).get();
        }else if (key.isWritable()){
            doWriteData(key);
        }
        return emptyData();
    }


    /**
     * ReactiveNIOServer#doWriteData
     *
     * do write data
     * @param SelectionKey
     */

    private static void doWriteData(SelectionKey key) throws ClosedChannelException {
        DataEntity data = (DataEntity) key.attachment();
        SocketChannel channal =(SocketChannel) key.channel();
        ByteBuffer buff =  ByteBuffer.allocate(data.getStr().length());
        buff.put(data.getStr().getBytes());
        buff.flip();
        try {
            channal.write(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            channal.register(key.selector(),SelectionKey.OP_READ);
        }

    }

    /**
     * ReactiveNIOServer#registerWriteEvent
     *
     * register write event with data entity
     */

    private static void registerWriteEvent(DataEntity data) throws ClosedChannelException {
        SocketChannel client = (SocketChannel)data.getKey().channel();
        if (client.isConnected())
         client.register(data.getKey().selector(),SelectionKey.OP_WRITE,data);
    }

    /**
     * ReactiveNIOServer#doReadData
     *
     * do read data
     * @param SelectionKey selectionKey
     * @return Future<String>
     */

    public static Future<DataEntity> doReadData(SelectionKey selectionKey) {
        return service.submit(()->{
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (socketChannel.socket().isClosed()) { return emptyData();}
            int count = 0;
                count = socketChannel.read(buffer);
            if (count < 0) {
                socketChannel.close();
                selectionKey.cancel();
                return emptyData();
            }else if (count==0){return emptyData();}
            buffer.flip();
            String data = new String(buffer.array()).trim();
            DataEntity dataEntity = new DataEntity(data,selectionKey);
            selectionKey.attach(dataEntity);
            //System.out.println(data);
            return  dataEntity;
        });

    }

    /**
     * ReactiveNIOServer#doDataPorcess1 ReactiveNIOServer#doDataPorcess1 ReactiveNIOServer#doDataPorcess1
     *
     * do process data
     * @param data {@link DataEntity}
     * @return DataEntity
     */

    public static DataEntity doDataPorcess1(DataEntity data) throws InterruptedException {
        Thread.sleep(20);
        data.setStr(data.getStr()+" =>processor 1");
        return data;
    }

    public static DataEntity doDataPorcess2(DataEntity data) throws InterruptedException {
        Thread.sleep(20);
        data.setStr(data.getStr()+" =>processor 2");
        return data;
    }

    public static DataEntity doDataPorcess3(DataEntity data) throws InterruptedException {
        Thread.sleep(20);
        data.setStr(data.getStr()+" =>processor 3");
        return data;
    }

    /**
     * ReactiveNIOServer#newNioServer
     *
     * init a new ServerSocketChannel and return the selector
     * @return Selector
     */

    private static Selector newNioServer() throws IOException {
        Selector selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        return selector;
    }


    /**
     * ReactiveNIOServer#emptyData
     *
     * default empty data entity implmentation
     */

    private static DataEntity emptyData(){
        return new DataEntity("",null);
    }


    /**
     * ReactiveNIOServer$DataEntity
     * data entity
     */

    static class DataEntity{
        private String str;
        private SelectionKey key;

        public DataEntity(String str,SelectionKey key) {
            this.str = str;
            this.key = key;
        }


        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public SelectionKey getKey(){
            return key;
        }

        @Override
        public String toString(){
            return "Request: "+str;
        }
    }

}
