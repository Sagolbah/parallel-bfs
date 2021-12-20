package ru.ifmo.parallelalgo.bfs.impl;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class SequentialBfsImpl implements BfsImpl {
    @Override
    public int[] bfs(int[][] graph, int start) {
        int[] res = new int[graph.length];
        Set<Integer> vis = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        vis.add(start);
        res[start] = 0;
        queue.add(start);
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            for (int i : graph[cur]) {
                if (!vis.contains(i)) {
                    vis.add(i);
                    res[i] = res[cur] + 1;
                    queue.add(i);
                }
            }
        }
        return res;
    }
}
