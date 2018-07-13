package com.muyun.simpledemo;


import io.reactivex.Observable;

import java.util.Arrays;

/**
 * @author muyun.cyt
 * @version 2018/7/10 8:06 PM
 */
public class ReactiveSort {
    public static void main(String... args) {
        Integer[] a = {1,5,3,7,9,2,8,6,10,4};

        //Observable.just(a).subscribe(b->Arrays.sort(a));
        //Observable.fromArray(a).toSortedList().subscribe(System.out::println);
        Observable.fromArray(a).doOnSubscribe(b->Arrays.sort(a)).subscribe(it->System.out.print(it+" "));

    }
    
}
