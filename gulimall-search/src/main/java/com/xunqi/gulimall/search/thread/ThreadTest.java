package com.xunqi.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-18 11:16
 **/
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        // System.out.println("main......start.....");
        // Thread thread = new Thread01();
        // thread.start();
        // System.out.println("main......end.....");

        // Runable01 runable01 = new Runable01();
        // new Thread(runable01).start();

        // FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        // new Thread(futureTask).start();
        // System.out.println(futureTask.get());

        // service.execute(new Runable01());
        // Future<Integer> submit = service.submit(new Callable01());
        // submit.get();

        System.out.println("main......start.....");
        // CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     System.out.println("运行结果：" + i);
        // }, executor);

        /**
         * 方法完成后的处理
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 0;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, executor).whenComplete((res,exception) -> {
        //     //虽然能得到异常信息，但是没法修改返回数据
        //     System.out.println("异步任务成功完成了...结果是：" + res + "异常是：" + exception);
        // }).exceptionally(throwable -> {
        //     //可以感知异常，同时返回默认值
        //     return 10;
        // });

        /**
         * 方法执行完后端处理
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, executor).handle((result,thr) -> {
        //     if (result != null) {
        //         return result * 2;
        //     }
        //     if (thr != null) {
        //         System.out.println("异步任务成功完成了...结果是：" + result + "异常是：" + thr);
        //         return 0;
        //     }
        //     return 0;
        // });


        /**
         * 线程串行化
         * 1、thenRun：不能获取上一步的执行结果，无返回值
         * 2、thenAcceptAsync：能接受上一步结果，但是无返回值
         * 3、thenApplyAsync：能接受上一步结果，有返回值
         *
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).thenApplyAsync(res -> {
//            System.out.println("任务2启动了..." + res);
//            return "Hello" + res;
//        }, executor);
//        System.out.println("main......end....." + future.get());


        /**
         * 两个都完成的线程处理
         */
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始:" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("任务1结束");
            return i;
        }, executor);

        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始:" + Thread.currentThread().getId());
            System.out.println("任务2结束");
            return "hello";
        }, executor);

//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("任务3开始...");
//        },executor);

//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("结果：" + f1 + f2);
//        }, executor);

        CompletableFuture<String> future03 = future01.thenCombineAsync(future02, (f1, f2) -> {
            System.out.println("结果：" + f1 + f2);
            return f1 + f2;
        }, executor);
        String s = future03.get();
        System.out.println(s);


    }

    private static void threadPool() {

        /**
         * 工作顺序：
         * 1）、线程池创建，准备好core数量的核心线程，准备接受任务。
         * 1.1、core满了，就在讲进来的任务放入阻塞队列中。空闲的core就会自己去阻塞队列获取任务执行
         * 1.2、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         * 1.3、max满了就用RejectedExecutionHandler拒绝任务
         * 1.4、max都执行完成，有很多空闲，在指定的时间keepAliveTime以后，释放max-core这些线程
         *
         *      new LinkedBlockingDeque()：默认是Integer的最大值，内存不够
         *
         * 一个线程池 core7；max20，queue：50，100并发进来怎么分配的；
         * 7个会立即得到执行，50个会进入队列，再开13个进行执行。剩下的30个就使用拒绝策略。
         * 如果不想抛弃还要执行。CallerRunsPolicy
         */
        ExecutorService threadPool = new ThreadPoolExecutor(
                5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        /**
         * Executors.newCachedThreadPool() core是0，所有都可回收
         * Executors.newFixedThreadPool() 固定大小，core=max；都不可回收
         * Executors.newScheduledThreadPool() 定时任务的线程池
         * Executors.newSingleThreadExecutor()  单线程的线程池，后台从队列里面获取任务
         */

        //定时任务的线程池
        ExecutorService service = Executors.newScheduledThreadPool(2);
    }


    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }


    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }


    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }

}
