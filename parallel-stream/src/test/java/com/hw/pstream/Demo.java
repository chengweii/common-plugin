package com.hw.pstream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/3/29
 */
public class Demo {
    public static void main(String[] args) {
        List<Item> list = getTestData();

        System.out.println(streamTest(list));

        System.out.println(parallelStreamTest(list));

        concurrentTest(10);
    }

    private static List<Item> getTestData() {
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Item item1 = new Item();
            item1.value1 = 1;
            item1.value2 = 4;
            list.add(item1);
        }
        return list;
    }

    public static void concurrentTest(int scheduleCount) {
        long start = System.currentTimeMillis();
        System.out.println("concurrentTest begin:" + start);

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < scheduleCount; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Item> list = getTestData();
                        System.out.println(parallelStreamTest(list));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });
        }

        System.out.println("添加完成");

        executorService.shutdown();

        System.out.println("concurrentTest end:" + (System.currentTimeMillis() - start));
    }

    private static List<Result> streamTest(List<Item> list) {
        long start = System.currentTimeMillis();

        List<Result> finalResult = new ArrayList<>(list.size());
        for (Item item : list) {
            Result result = new Result();
            result.value = item.sum();
            finalResult.add(result);
        }

        System.out.println("streamTest:" + (System.currentTimeMillis() - start));

        return finalResult;
    }

    private static List<Result> parallelStreamTest(List<Item> list) {
        long start = System.currentTimeMillis();
        System.out.println("parallelStreamTest begin:" + start);
        int threadCount = Runtime.getRuntime().availableProcessors() * 32;

        List<Result> finalResult = ParallelStream.<Item, Result>of("action1", list, threadCount).execute((item) -> {
            Result result = new Result();
            result.value = item.sum();
            return result;
        });

        System.out.println("parallelStreamTest end:" + (System.currentTimeMillis() - start));
        System.out.println("");

        return finalResult;
    }

    public static class Item {
        public int value1;
        public int value2;

        public int sum() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return value1 + value2;
        }
    }

    public static class Result {
        public int value;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Result{");
            sb.append("value=").append(value);
            sb.append('}');
            return sb.toString();
        }
    }
}
