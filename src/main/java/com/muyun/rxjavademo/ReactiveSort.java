package com.muyun.rxjavademo;


import io.reactivex.Observable;

import java.util.Arrays;

/**
 * @author muyun.cyt
 * @version 2018/7/10 8:06 PM
 */
public class ReactiveSort {
    public static void main(String... args) {
        int[] a = {1,5,3,7,9,2,8,6,10,4};
        QucikSort(a);
        for (int b:
             a) {
            System.out.println(b);
        }

        Integer[] s = {2,4,3,6,7,2};

        Observable.fromArray(s).doOnNext(b->Arrays.sort(s)).subscribe(it->System.out.print(it+" "));

    }



    static void QucikSort(int[] arr){
        quickSort(arr,0,arr.length);
    }

    static void quickSort(int[] arr,int begin,int end){
        if(begin<end){
            int index = partition(begin,end,arr);
            quickSort(arr,begin,index);
            quickSort(arr,index+1,end);
        }

    }

    static int partition(int begin,int end,int[] array){
        int pivot = array[begin];
        int cur = begin+1;
        int small = begin;
        while(cur<end){
            if(array[cur]<pivot){
                if(small!=cur) {
                    swap(++small, cur, array);
                }
            }
            cur++;
        }
        swap(begin,small,array);
        return small;
    }

    static void swap(int x,int y,int[] a){
        int t = a[x];
        a[x] = a[y];
        a[y] =t;
    }

}
