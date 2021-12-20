package ru.ifmo.parallelalgo.bfs;

import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParallelPrimitives {
    private final int blockSize;

    public ParallelPrimitives(int blockSize) {
        this.blockSize = blockSize;
    }

    public void pfor(int l, int r, Consumer<Integer> task) {
        if (r - l < blockSize) {
            for (int i = l; i < r; i++) {
                task.accept(i);
            }
            return;
        }
        int mid = (l + r) / 2;
        forkJoin(() -> pfor(l, mid, task), () -> pfor(mid, r, task));
    }

    public int[] filter(int[] arr, Function<Integer, Boolean> predicate) {
        int[] flags = new int[arr.length];
        pfor(0, arr.length, i -> {
            if (predicate.apply(arr[i])) {
                flags[i] = 1;
            }
        });
        int[] sums = scan(flags);
        int[] res = new int[sums[sums.length - 1]];
        pfor(0, arr.length, i -> {
            if (flags[i] == 1) {
                res[sums[i]] = arr[i];
            }
        });
        return res;
    }

    public int[] scan(int[] arr) {
        // assume block size will handle it
        int[] segTree = new int[arr.length];
        build(0, 0, arr.length, segTree, arr);
        int[] res = new int[arr.length + 1];
        calc(0, 0, arr.length, segTree, arr, 0, res);
        res[arr.length] = arr[arr.length - 1] + res[arr.length - 1];
        return res;
    }

    /**
     * Constructs tree on array, based on blocks
     *
     * @param id   index of vertex, leaves are 2*i+1, 2*i+2
     * @param l    left semi-interval border
     * @param r    right semi-interval border
     * @param tree result tree, root is 0
     * @param arr  source array
     */
    private void build(int id, int l, int r, int[] tree, int[] arr) {
        if (r - l < blockSize) {
            int res = 0;
            for (int i = l; i < r; i++) res += arr[i];
            tree[id] = res;
            return;
        }
        int mid = (l + r) / 2;
        forkJoin(() -> build(2 * id + 1, l, mid, tree, arr), () -> build(2 * id + 2, mid, r, tree, arr));
        tree[id] = tree[2 * id + 1] + tree[2 * id + 2];
    }

    private void calc(int id, int l, int r, int[] tree, int[] arr, int fromLeft, int[] res) {
        if (r - l < blockSize) {
            int cur = fromLeft;
            for (int i = l; i < r; i++) {
                res[i] = cur;
                cur += arr[i];
            }
            return;
        }
        int mid = (l + r) / 2;
        forkJoin(() -> calc(2 * id + 1, l, mid, tree, arr, fromLeft, res),
                () -> calc(2 * id + 2, mid, r, tree, arr, fromLeft + tree[2 * id + 1], res));
    }

    private void forkJoin(Runnable left, Runnable right) {
        RecursiveAction l = new RecursiveAction() {
            @Override
            protected void compute() {
                left.run();
            }
        };
        RecursiveAction r = new RecursiveAction() {
            @Override
            protected void compute() {
                right.run();
            }
        };
        l.fork();
        r.fork();
        l.join();
        r.join();
    }

}
