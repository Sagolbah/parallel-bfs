package ru.ifmo.parallelalgo.bfs;

import ru.ifmo.parallelalgo.bfs.impl.BfsImpl;
import ru.ifmo.parallelalgo.bfs.impl.ParallelBfsImpl;
import ru.ifmo.parallelalgo.bfs.impl.SequentialBfsImpl;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int size = 300;
        ParallelPrimitives primitives = new ParallelPrimitives(1024);
        int[][] graph = makeGraph(size);
        BfsImpl seqImpl = new SequentialBfsImpl();
        BfsImpl parImpl = new ParallelBfsImpl(primitives);
        long seq1 = System.currentTimeMillis();
        int[] seqRes = seqImpl.bfs(graph, 0);
        long seq2 = System.currentTimeMillis();
        System.out.println("Sequential: " + (seq2 - seq1));
        long par1 = System.currentTimeMillis();
        int[] parRes = parImpl.bfs(graph, 0);
        long par2 = System.currentTimeMillis();
        System.out.println("Parallel: " + (par2 - par1));
        System.out.println("Checking results...");
        for (int i = 0; i < parRes.length; i++) {
            if (parRes[i] != seqRes[i]) {
                System.err.println("FAILED");
                return;
            }
        }
        System.out.printf("Ratio: %.3f", (double) (seq2 - seq1) / (par2 - par1));
    }


    private static int[][] makeGraph(int size) {
        int[][] graph = new int[size * size * size][];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    graph[i * size * size + j * size + k] = neighbours(i, j, k, size);
                }
            }
        }
        return graph;
    }

    private static int[] neighbours(int i, int j, int k, int size) {
        List<Integer> res = new ArrayList<>();
        if (i > 0) {
            res.add((i - 1) * size * size + j * size + k);
        }
        if (j > 0) {
            res.add(i * size * size + (j - 1) * size + k);
        }
        if (k > 0) {
            res.add(i * size * size + j * size + k - 1);
        }
        if (i < size - 1) {
            res.add((i + 1) * size * size + j * size + k);
        }
        if (j < size - 1) {
            res.add(i * size * size + (j + 1) * size + k);
        }
        if (k < size - 1) {
            res.add(i * size * size + j * size + k + 1);
        }
        int[] arr = new int[res.size()];
        for (int t = 0; t < res.size(); t++) arr[t] = res.get(t);
        return arr;
    }

}

