package com.muyun.rxnettydemo;



import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author muyun.cyt
 * @version 2018/7/19 9:45 PM
 */
public class EchoClient {

    public static void main(String... args) {
        TcpClient.newClient("localhost",8000)
                .createConnectionRequest()
                .repeatWhen(observable -> observable.delay(1,TimeUnit.SECONDS))
                .flatMap(connection ->
                        connection.writeString(Observable.just("Hello World!"))
                                .cast(ByteBuf.class)
                                .concatWith(connection.getInput())
                )
                .map(bb -> bb.toString(Charset.defaultCharset()))
                .map(s -> "Get Response: "+s)
                .toBlocking()
                .forEach(System.out::println);
    }
}
