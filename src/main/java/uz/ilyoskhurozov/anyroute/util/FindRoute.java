package uz.ilyoskhurozov.anyroute.util;

import uz.ilyoskhurozov.anyroute.component.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FindRoute {

    public static List<String> withDijkstra(LinkedHashMap<String, LinkedHashMap<String, Integer>> table, String begin, String end) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparing(Node::getDistance));
        HashMap<String, Node> nodeMap = new HashMap<>();

        Node cur = new Node(begin, null, 0);

        do {
            Node curNode = cur;
            table.get(cur.getName()).forEach((name, dis) -> {
                if (dis != null && !nodeMap.containsKey(name)) {
                    Node node = queue.stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);

                    int disToCheck = curNode.getDistance() + dis;
                    if (node == null) {
                        node = new Node(name, curNode.getName(), disToCheck);
                        queue.add(node);
                    } else {
                        if (node.getDistance() > disToCheck) {
                            node.setDistance(disToCheck);
                            node.setPrevious(curNode.getName());
                        }
                    }
                }
            });
            nodeMap.put(cur.getName(), cur);
            cur = queue.poll();
        } while (cur != null && !cur.getName().equals(end));

        if (cur == null) {
            return null;
        } else {
            LinkedList<String> route = new LinkedList<>();
            route.addFirst(end);

            while (cur.getPrevious() != null) {
                route.addFirst(cur.getPrevious());
                cur = nodeMap.get(cur.getPrevious());
            }

            return route;
        }
    }

    public static List<String> withFloyd(LinkedHashMap<String, LinkedHashMap<String, Integer>> table, String begin, String end) {
        Set<String> routers = table.keySet();
        //initialize prevTable
        LinkedHashMap<String, LinkedHashMap<String, String>> prevTable = new LinkedHashMap<>();

        routers.forEach(r1 -> {
            LinkedHashMap<String, String> row = new LinkedHashMap<>();
            prevTable.put(r1, row);
            routers.forEach(r2 -> {
                if (!r1.equals(r2)) {
                    row.put(r2, r2);
                }
            });
        });

        //find routes
        routers.forEach(
                i -> routers.forEach(
                        col -> routers.stream().takeWhile(row -> !row.equals(col)).forEach(
                                row -> {
                                    Integer d1 = table.get(row).get(i);
                                    Integer d2 = table.get(i).get(col);
                                    if (d1 != null && d2 != null) {
                                        Integer d = d1 + d2;
                                        Integer cell = table.get(row).get(col);
                                        if (cell == null || cell > d) {
                                            table.get(row).put(col, d);
                                            table.get(col).put(row, d);

                                            prevTable.get(row).put(col, i);
                                            prevTable.get(col).put(row, i);
                                        }
                                    }
                                }
                        )
                )
        );

        //check shortcuts
        AtomicBoolean hasChange = new AtomicBoolean();
        do {
            hasChange.set(false);
            routers.forEach(
                    row -> routers.stream().takeWhile(col -> !col.equals(row)).forEach(
                            col -> {
                                String prev = prevTable.get(row).get(col);
                                String prePrev = prevTable.get(row).get(prev);
                                String reverse = prevTable.get(col).get(row);

                                if (!prev.equals(prePrev) && !prev.equals(reverse)) {
                                    hasChange.set(true);
                                    prevTable.get(row).put(col, prePrev);
                                    prevTable.get(col).put(row, prePrev);
                                }
                            }
                    )
            );
        } while (hasChange.get());

        //assemble route
        LinkedList<String> route = new LinkedList<>();
        route.add(end);
        String prev = prevTable.get(begin).get(end), prePrev;

        while (!(prePrev = prevTable.get(begin).get(prev)).equals(prev)) {
            route.addFirst(prev);
            prev = prePrev;
        }
        route.addFirst(prev);
        route.addFirst(begin);

        return route;
    }

}
