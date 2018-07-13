package com.muyun.simpledemo;

import io.reactivex.Flowable;


/**
 * @author muyun.cyt
 * @version 2018/7/10 7:45 PM
 */
public class ReactiveHelloWorld {

    public static void main(String... args) {
        Flowable.just("Hello World")
                .map(it->it.split(""))
                .flatMap(Flowable::fromArray)
                .zipWith(Flowable.range(1,100),(str,count)->(str+count))
                .zipWith(Flowable.just("Hello World")
                        .map(str->(new StringBuffer(str).reverse().toString()))
                        .map(it->it.split(""))
                        .flatMap(Flowable::fromArray),(a, b)->(a+b))
                .subscribe(System.out::println);

    }
}
