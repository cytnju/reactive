package com.muyun.backpressure;

import io.reactivex.*;
import io.reactivex.functions.Consumer;
import org.junit.Test;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


/**
 * @author muyun.cyt
 * @version 2018/7/16 3:06 PM
 */
public class BackPressureDemo {


    @Test
    public void NoBackPressureTest(){
        Consumer<Object> consumer = v -> System.out.println("Result: " + v);
        Flowable<Long> f1 = Flowable.interval(100, TimeUnit.MILLISECONDS);
        Flowable<Long> f2 = Flowable.interval(200, TimeUnit.MILLISECONDS);


        Flowable<Long> f3 = Flowable.interval(300,MILLISECONDS);

        Flowable<Long> f4 = Flowable.zip(f1,f2,f3,(x, y, z) ->x*10000+y*100+z);

        f4.subscribe(consumer,Throwable::printStackTrace);
        try {
            TimeUnit.SECONDS.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void BackPressureBufferTest(){
        Consumer<Object> consumer = v -> System.out.println("Result: " + v);
        Flowable<Long> f1 = Flowable.interval(100, TimeUnit.MILLISECONDS).onBackpressureBuffer();
        Flowable<Long> f2 = Flowable.interval(200, TimeUnit.MILLISECONDS);

        Flowable<Long> f3 = Flowable.zip(f1, f2, (x, y) -> x * 100 + y);

        f3.subscribe(consumer,Throwable::printStackTrace);
        try {
            TimeUnit.SECONDS.sleep(100);
            f3.ignoreElements();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void BackPressureDropTest(){
        Consumer<Object> consumer = v -> System.out.println("Result: " + v);
        Flowable<Long> f1 = Flowable.interval(100, TimeUnit.MILLISECONDS).onBackpressureDrop();
        Flowable<Long> f2 = Flowable.interval(200, TimeUnit.MILLISECONDS);
        Flowable<Long> f3 = Flowable.zip(f1, f2, (x, y) -> x * 100 + y);
        f3.subscribe(consumer,Throwable::printStackTrace);
        try {
            TimeUnit.SECONDS.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void BackPressureLatestTest(){
        Consumer<Object> consumer = v -> System.out.println("Result: " + v);
        final AtomicInteger i = new AtomicInteger(0);
        Flowable<Long> f1 = Flowable.interval(100, TimeUnit.MILLISECONDS).onBackpressureLatest();
              //  .onBackpressureBuffer(1,() -> System.out.println("overflow"+i.incrementAndGet()),BackpressureOverflowStrategy.DROP_OLDEST);
        Flowable<Long> f2 = Flowable.interval(200, TimeUnit.MILLISECONDS);
        Flowable<Long> f3 = Flowable.zip(f1, f2, (x, y) -> x * 100 + y);
        f3.subscribe(consumer,Throwable::printStackTrace);
        try {
            TimeUnit.SECONDS.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
