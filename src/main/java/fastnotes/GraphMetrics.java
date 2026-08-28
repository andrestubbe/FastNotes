package fastnotes;

import java.util.*;

/**
 * Vault Knowledge Graph topological and connectivity metrics (In/Out-degrees, PageRank centrality,
 * orphan nodes, ghost links, connected components).
 */
public final class GraphMetrics {
    private final int totalNodes;
    private final int totalEdges;
    private final int connectedComponents;
    private final double graphDensity;
    private final List<String> orphanNotes;
    private final Set<String> ghostNotes;
    private final Map<String, Integer> inDegrees;
    private final Map<String, Integer> outDegrees;
    private final Map<String, Double> pageRanks;

    public GraphMetrics(int totalNodes, int totalEdges, int connectedComponents, double graphDensity,
                        List<String> orphanNotes, Set<String> ghostNotes,
                        Map<String, Integer> inDegrees, Map<String, Integer> outDegrees,
                        Map<String, Double> pageRanks) {
        this.totalNodes = totalNodes;
        this.totalEdges = totalEdges;
        this.connectedComponents = connectedComponents;
        this.graphDensity = graphDensity;
        this.orphanNotes = orphanNotes != null ? Collections.unmodifiableList(new ArrayList<>(orphanNotes)) : Collections.emptyList();
        this.ghostNotes = ghostNotes != null ? Collections.unmodifiableSet(new LinkedHashSet<>(ghostNotes)) : Collections.emptySet();
        this.inDegrees = inDegrees != null ? Collections.unmodifiableMap(new LinkedHashMap<>(inDegrees)) : Collections.emptyMap();
        this.outDegrees = outDegrees != null ? Collections.unmodifiableMap(new LinkedHashMap<>(outDegrees)) : Collections.emptyMap();
        this.pageRanks = pageRanks != null ? Collections.unmodifiableMap(new LinkedHashMap<>(pageRanks)) : Collections.emptyMap();
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getTotalEdges() {
        return totalEdges;
    }

    public int getConnectedComponents() {
        return connectedComponents;
    }

    public double getGraphDensity() {
        return graphDensity;
    }

    public List<String> getOrphanNotes() {
        return orphanNotes;
    }

    public Set<String> getGhostNotes() {
        return ghostNotes;
    }

    public Map<String, Integer> getInDegrees() {
        return inDegrees;
    }

    public Map<String, Integer> getOutDegrees() {
        return outDegrees;
    }

    public Map<String, Double> getPageRanks() {
        return pageRanks;
    }

    public int getInDegree(String noteName) {
        return inDegrees.getOrDefault(noteName, 0);
    }

    public int getOutDegree(String noteName) {
        return outDegrees.getOrDefault(noteName, 0);
    }

    public double getPageRank(String noteName) {
        return pageRanks.getOrDefault(noteName, 0.0);
    }

    @Override
    public String toString() {
        return "GraphMetrics{" +
                "nodes=" + totalNodes +
                ", edges=" + totalEdges +
                ", components=" + connectedComponents +
                ", density=" + String.format(Locale.US, "%.4f", graphDensity) +
                ", orphans=" + orphanNotes.size() +
                ", ghostNotes=" + ghostNotes.size() +
                '}';
    }
}
