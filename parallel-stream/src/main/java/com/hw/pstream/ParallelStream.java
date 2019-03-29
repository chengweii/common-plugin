package com.hw.pstream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/3/29
 */
public class ParallelStream<T, R> {
    private static final int MAX_CAP = 0x7fff;

    private static Map<String, ForkJoinPool> forkJoinPoolMap;

    static {
        forkJoinPoolMap = new ConcurrentHashMap<String, ForkJoinPool>();
    }

    private String streamKey;

    private List<T> values;

    private List<ParallelActionTask<T, R>> tasks;

    public ParallelStream(String streamKey, List<T> values, int parallelism) {
        this.values = values;
        this.streamKey = streamKey;
        this.tasks = new ArrayList<ParallelActionTask<T, R>>(values.size());
        if (!forkJoinPoolMap.containsKey(streamKey)) {
            ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
            forkJoinPoolMap.put(streamKey, forkJoinPool);
        }
    }

    public ParallelStream(String streamKey, List<T> values) {
        this(streamKey, values, Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()));
    }

    public static <T, R> ParallelStream<T, R> of(String streamKey, List<T> values, int parallelism) {
        return new ParallelStream<T, R>(streamKey, values, parallelism);
    }

    public static <T, R> ParallelStream<T, R> of(String streamKey, List<T> values) {
        return new ParallelStream<T, R>(streamKey, values);
    }

    public List<R> execute(ParallelAction<T, R> parallelAction) {
        ForkJoinPool forkJoinPool = forkJoinPoolMap.get(streamKey);

        values.forEach(value -> {
            ParallelActionTask<T, R> parallelActionTask = new ParallelActionTask<T, R>(parallelAction, value);
            tasks.add(parallelActionTask);
            forkJoinPool.submit(parallelActionTask);
        });

        Collections.reverse(tasks);

        List<R> result = tasks.stream().map(task -> task.join()).collect(Collectors.toList());

        return result;
    }

    public static class ParallelActionTask<T, R> extends RecursiveTask<R> {
        private ParallelAction<T, R> parallelAction;
        private T value;

        ParallelActionTask(ParallelAction<T, R> parallelAction, T value) {
            this.parallelAction = parallelAction;
            this.value = value;
        }

        @Override
        protected R compute() {
            return parallelAction.execute(value);
        }
    }

    @FunctionalInterface
    public interface ParallelAction<T, R> {
        R execute(T param);
    }
}
