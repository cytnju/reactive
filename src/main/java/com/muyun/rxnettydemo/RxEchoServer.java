package com.muyun.rxnettydemo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.protocol.tcp.server.ConnectionHandler;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import rx.Observable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author muyun.cyt
 * @version 2018/7/19 9:44 PM
 */
public class EchoServer {

    static int PORT = 8000;
    static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String... args) {
        TcpServer server;
        server= TcpServer.newServer(PORT).
                addChannelHandlerLast("StringDecoder", StringDecoder::new)
                .addChannelHandlerLast("StringEncoder",StringEncoder::new)
                .addChannelHandlerLast("TestHandler",THandler::new)
                .start(new SimpleConnectionHandler());

        server.awaitShutdown();
    }


    private static ExecutorService getExcutorService(){
        return service;
    }


    private static class SimpleConnectionHandler implements ConnectionHandler {

        private AtomicInteger count ;

        public SimpleConnectionHandler() {
            this.count = new AtomicInteger(0);
        }
        @Override
        public Observable<Void> handle(Connection newConnection) {
            //System.out.println("Start a new connection---Total  "+count.incrementAndGet()+"Connections");
            return newConnection.ignoreInput();
        }
    }
    

    /**
     * RxEchoServer$THandler
     *
     * a test handler to invoke for response
     */
    private static class THandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
            System.out.println("Receive request:  "+request);
            Future<String> result = invoke(request);
            ctx.writeAndFlush(result.get(1000,TimeUnit.MILLISECONDS));
        }

        private Future<String> invoke(String msg) {
            return getExcutorService().submit(new RequestProcessTask(msg,new RequestProcessor()));
        }


    }


    /**
     * RxEchoServer$RequestProcessTask
     *
     * a callable return the request processed by the processor
     */

    private static class RequestProcessTask implements Callable {

        private String request;

        private RequestProcessor processor;

        public RequestProcessTask(String request,RequestProcessor processor) {
            this.request = request;
            this.processor = processor;
        }

        @Override
        public String call() {
            return processor.process(request);
        }
    }


    /**
     * RxEchoServer$RequestProcessor
     *
     * RequestProcessor to process the request
     */

    private static class RequestProcessor {

        public String process(String request){
            //do process
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            request = "echo => "+ request;
            return request;
        }

    }
}
