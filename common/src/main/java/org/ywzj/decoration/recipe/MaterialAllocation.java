package org.ywzj.decoration.recipe;

import java.util.Arrays;

public final class MaterialAllocation {

    private MaterialAllocation() {}

    public static int[] plan(int[] available, int[] required, boolean[][] matches) {
        int materialStart = 1 + available.length;
        int sink = materialStart + required.length;
        int[][] capacity = new int[sink + 1][sink + 1];
        int total = 0;
        for (int slot = 0; slot < available.length; slot++) {
            capacity[0][slot + 1] = available[slot];
            for (int material = 0; material < required.length; material++) {
                if (matches[slot][material]) {
                    capacity[slot + 1][materialStart + material] = available[slot];
                }
            }
        }
        for (int material = 0; material < required.length; material++) {
            capacity[materialStart + material][sink] = required[material];
            total += required[material];
        }
        int flow = 0;
        int[] parent = new int[sink + 1];
        int[] queue = new int[sink + 1];
        while (flow < total) {
            Arrays.fill(parent, -1);
            parent[0] = 0;
            int head = 0;
            int tail = 1;
            queue[0] = 0;
            while (head < tail && parent[sink] == -1) {
                int from = queue[head++];
                for (int to = 1; to <= sink; to++) {
                    if (parent[to] == -1 && capacity[from][to] > 0) {
                        parent[to] = from;
                        queue[tail++] = to;
                    }
                }
            }
            if (parent[sink] == -1) {
                return null;
            }
            int amount = total - flow;
            for (int to = sink; to != 0; to = parent[to]) {
                amount = Math.min(amount, capacity[parent[to]][to]);
            }
            for (int to = sink; to != 0; to = parent[to]) {
                capacity[parent[to]][to] -= amount;
                capacity[to][parent[to]] += amount;
            }
            flow += amount;
        }
        int[] consumed = new int[available.length];
        for (int slot = 0; slot < consumed.length; slot++) {
            consumed[slot] = available[slot] - capacity[0][slot + 1];
        }
        return consumed;
    }
}
