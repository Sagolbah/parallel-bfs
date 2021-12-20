package ru.ifmo.parallelalgo.bfs.impl;

import ru.ifmo.parallelalgo.bfs.ParallelPrimitives;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ParallelBfsImpl implements BfsImpl {
    private ParallelPrimitives primitives;

    public ParallelBfsImpl(ParallelPrimitives primitives) {
        this.primitives = primitives;
    }

    @Override
    public int[] bfs(int[][] graph, int start) {
        AtomicReference<int[]> frontier = new AtomicReference<>(new int[1]);  // effective final workaround :(
        frontier.get()[0] = start;
        AtomicInteger[] result = new AtomicInteger[graph.length];
        primitives.pfor(0, graph.length, x -> result[x] = new AtomicInteger(-1));
        result[start].set(0);
        while (frontier.get().length != 0) {
            int[] degree = new int[frontier.get().length];
            primitives.pfor(0, degree.length, x -> {
                degree[x] = graph[frontier.get()[x]].length;
            });
            int[] trueDeg = primitives.scan(degree);
            int[] newFrontier = new int[trueDeg[trueDeg.length - 1]];
            primitives.pfor(0, newFrontier.length, i -> newFrontier[i] = -1);
            primitives.pfor(0, frontier.get().length, i -> {
                int curVertex = frontier.get()[i];
                if (curVertex == -1) return;
                int myDistance = result[curVertex].get();
                int myNextStart = trueDeg[i];
                for (int v : graph[curVertex]) {
                    if (result[v].compareAndSet(-1, myDistance + 1)) {
                        newFrontier[myNextStart++] = v;
                    }
                }
            });
            int[] resultedFrontier = primitives.filter(newFrontier, x -> x != -1);
            frontier.set(resultedFrontier);
        }
        int[] finalRes = new int[graph.length];
        for (int i = 0; i < result.length; i++) finalRes[i] = result[i].get();
        return finalRes;
    }
}
