package com.example.lab6.service;


import com.example.lab6.domain.Prietenie;

import java.util.*;

public class Graph {
    private final Map<Long, Long> parent; // To store the parent of each vertex
    private final Map<Long, Integer> rank;   // To optimize union by rank

    public Graph() {
        parent = new HashMap<>();
        rank = new HashMap<>();
    }

    // Find operation with path compression
    public Long find(Long vertex) {
        if (!parent.get(vertex).equals(vertex)) {
            parent.put(vertex, find(parent.get(vertex))); // Path compression
        }
        return parent.get(vertex);
    }

    // Union operation by rank
    public void union(Long vertex1, Long vertex2) {
        Long root1 = find(vertex1);
        Long root2 = find(vertex2);

        if (!root1.equals(root2)) {
            // Union by rank to keep tree flat
            if (rank.get(root1) > rank.get(root2)) {
                parent.put(root2, root1);
            } else if (rank.get(root1) < rank.get(root2)) {
                parent.put(root1, root2);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank.get(root1) + 1);
            }
        }
    }

    // Initialize the parent and rank for a vertex
    public void addVertex(Long vertex) {
        if (!parent.containsKey(vertex)) {
            parent.put(vertex, vertex);
            rank.put(vertex, 0);
        }
    }

    // Build the adjacency list from edges
    private Map<Long, List<Long>> buildAdjacencyList(Iterable<Prietenie<Long>> edges) {
        Map<Long, List<Long>> adjList = new HashMap<>();
        for (Prietenie<Long> edge : edges) {
            Long vertex1 = edge.getId().getLeft();
            Long vertex2 = edge.getId().getRight();

            // Add edges to the adjacency list
            adjList.computeIfAbsent(vertex1, k -> new ArrayList<>()).add(vertex2);
            adjList.computeIfAbsent(vertex2, k -> new ArrayList<>()).add(vertex1);
        }
        return adjList;
    }

    // Method to count connected components
    public int countConnectedComponents(Iterable<Prietenie<Long>> edges) {
        for (Prietenie<Long> edge : edges) {
            Long vertex1 = edge.getId().getLeft();
            Long vertex2 = edge.getId().getRight();

            // Add the vertices if they haven't been added
            addVertex(vertex1);
            addVertex(vertex2);

            // Union the two vertices
            union(vertex1, vertex2);
        }

        // Find how many unique components there are
        Set<Long> uniqueComponents = new HashSet<>();
        for (Long vertex : parent.keySet()) {
            uniqueComponents.add(find(vertex));
        }

        return uniqueComponents.size();
    }

    // Helper class for BFS result
    static class BFSResult {
        Long farthestNode;
        int distance;
        List<Long> path;

        public BFSResult(Long farthestNode, int distance, List<Long> path) {
            this.farthestNode = farthestNode;
            this.distance = distance;
            this.path = path;
        }
    }

    // BFS method to find the farthest node and its distance from the starting point
    private BFSResult bfs(Long start, Map<Long, List<Long>> adjList) {
        Queue<Long> queue = new LinkedList<>();
        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();

        queue.add(start);
        distance.put(start, 0);
        previous.put(start, null);

        Long farthestNode = start;
        int maxDistance = 0;

        while (!queue.isEmpty()) {
            Long node = queue.poll();
            int currentDistance = distance.get(node);

            if (currentDistance > maxDistance) {
                maxDistance = currentDistance;
                farthestNode = node;
            }

            for (Long neighbor : adjList.get(node)) {
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, currentDistance + 1);
                    previous.put(neighbor, node);
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruct the path from start to the farthest node
        List<Long> path = new ArrayList<>();
        Long step = farthestNode;
        while (step != null) {
            path.add(step);
            step = previous.get(step);
        }
        Collections.reverse(path); // Reverse to get path from start to farthest node

        return new BFSResult(farthestNode, maxDistance, path);
    }

    // Method to calculate the longest path in the graph
    public Map.Entry<List<Long>, Integer> longestPathInNetwork(Iterable<Prietenie<Long>> edges) {
        // Build adjacency list
        Map<Long, List<Long>> adjList = buildAdjacencyList(edges);

        // Keep track of visited nodes and the longest path
        Set<Long> visited = new HashSet<>();
        int longestPath = 0;
        List<Long> longestPathNodes = new ArrayList<>();

        // Traverse all vertices and run BFS for each connected component
        for (Long vertex : adjList.keySet()) {
            if (!visited.contains(vertex)) {
                // BFS-1: Find the farthest node from this vertex
                BFSResult firstBFS = bfs(vertex, adjList);

                // Mark all nodes in this component as visited
                markComponentAsVisited(vertex, adjList, visited);

                // BFS-2: From the farthest node found, find the longest distance in the component
                BFSResult secondBFS = bfs(firstBFS.farthestNode, adjList);

                // The longest path in this component
                if (secondBFS.distance > longestPath) {
                    longestPath = secondBFS.distance;
                    longestPathNodes = secondBFS.path; // Update the longest path nodes
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(longestPathNodes, longestPath);
    }

    // Helper method to mark the entire connected component as visited
    private void markComponentAsVisited(Long start, Map<Long, List<Long>> adjList, Set<Long> visited) {
        Queue<Long> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Long node = queue.poll();
            for (Long neighbor : adjList.get(node)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }
}
