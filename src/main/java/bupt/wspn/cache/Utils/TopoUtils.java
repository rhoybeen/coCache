package bupt.wspn.cache.Utils;

import bupt.wspn.cache.model.Edge;
import bupt.wspn.cache.model.EdgeType;
import bupt.wspn.cache.model.NodeType;
import bupt.wspn.cache.service.WebClient;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

@Slf4j
public class TopoUtils {
    public static double REGIONAL_EDGE_DELAY = Double.valueOf(PropertyUtils.getProperty("cache.delay.MBS_TO_REGIONAL"));
    public static double MBS_EDGE_DELAY = Double.valueOf(PropertyUtils.getProperty("cache.delay.SBS_TO_MBS"));
    public static double BASE_DELAY = Double.valueOf(PropertyUtils.getProperty("cache.delay.BASE_DELAY"));

    public static MutableValueGraph<WebClient, Edge> createGraphFromMap(@NonNull Map<String, WebClient> webClientMap) {
        MutableValueGraph<WebClient, Edge> graph = ValueGraphBuilder.undirected().allowsSelfLoops(false).build();
        for (WebClient webclient : webClientMap.values()) {
            graph.addNode(webclient);
            if (!StringUtils.isEmpty(webclient.getParentId())) {
                final WebClient parent = webClientMap.get(webclient.getParentId());
                if (Objects.nonNull(parent)) {
                    graph.addNode(parent);
                    EdgeType edgeType = parent.getNodeType() == NodeType.REGIONAL_MEC ? EdgeType.MBS_REGIONAL : EdgeType.SBS_MBS;
                    double weight = parent.getNodeType() == NodeType.REGIONAL_MEC ? REGIONAL_EDGE_DELAY : MBS_EDGE_DELAY;
                    log.info("Add edge " + webclient.getId() + "  " + parent.getId());
                    graph.putEdgeValue(webclient, parent, new Edge(weight, edgeType));
                }
            }
        }
        return graph;
    }

    /**
     * Get network delays between two web clients.
     * -1 as default return.
     *
     * @param graph
     * @param client1
     * @param client2
     * @return
     */
    public static double getDelay(@NonNull final MutableValueGraph graph,
                                  @NonNull final WebClient client1,
                                  @NonNull final WebClient client2) {
        if (!graph.nodes().contains(client1) || !graph.nodes().contains(client2)) return -1;
        Stack<WebClient> path = new Stack<>();
        getPathCore(graph, client1, client2, path);
        if (path.empty()) {
            log.info("No path found between " + client1.getId() + " and " + client2.getId());
            return -1;
        } else {
            log.info("Path between " + client1.getId() + " and " + client2.getId() + " :" + path.toString());
            double pathDelay = 0;
            WebClient cur = path.pop();
            while (!path.empty()) {
                Edge edge = (Edge) graph.edgeValue(cur, path.peek()).get();
                double delay = getSimuEdgeDelay(edge);
                pathDelay += delay;
                cur = path.pop();
            }
            return pathDelay;
        }
    }

    public static void getPathCore(MutableValueGraph graph,
                                   WebClient current,
                                   WebClient target,
                                   Stack<WebClient> stack) {
        WebClient peek = stack.peek();
        stack.push(current);
        if (current == target) return;
        Set<WebClient> adjacentNodes = graph.adjacentNodes(current);
        adjacentNodes.remove(peek);
        for (WebClient node : adjacentNodes) {
            getPathCore(graph, node, target, stack);
        }
        stack.pop();
    }

    public static double getSimuEdgeDelay(@NonNull Edge edge) {
        final EdgeType edgeType = edge.getType();
        return getSimuDelay((int) (edgeType == EdgeType.MBS_REGIONAL ? REGIONAL_EDGE_DELAY : MBS_EDGE_DELAY));
    }

    public static double getSimuDelay(int pivot) {
        NormalDistribution normalDistribution = new NormalDistribution(pivot, 1);
        return normalDistribution.sample();
    }

    /**
     * Simulate delays among nodes in cooperating network.
     *
     * @param graph
     * @param delayMap
     */
    public static void getSimuGraphDelays(@NonNull final MutableValueGraph graph, double[][] delayMap) {
        Set<WebClient> webClients = graph.nodes();
        for (WebClient webClient : webClients) {
            //剪枝，只计算SBS节点间的时延
            //if (webClient.getNodeType() != NodeType.SBS_MEC) continue;
            Set<WebClient> adjacentNodes = graph.adjacentNodes(webClient);
            for (WebClient adjacentNode : adjacentNodes) {
                traversalGraph(graph, delayMap, webClient, webClient, adjacentNode, 0);
            }
        }
    }

    public static void traversalGraph(@NonNull MutableValueGraph graph,
                                      double[][] delayMap,
                                      @NonNull WebClient start,
                                      @NonNull WebClient pre,
                                      @NonNull WebClient cur,
                                      double delay) {
//        log.info("Calc edge between" + start.getId() + "  " + cur.getId());
        Edge edge = (Edge) graph.edgeValue(pre, cur).get();
        double edgeDelay = getSimuEdgeDelay(edge);
        delay += edgeDelay;
        int startId = Integer.valueOf(start.getId());
        int curId = Integer.valueOf(cur.getId());
        delayMap[startId][curId] = delay;
        Set<WebClient> webClients = graph.adjacentNodes(cur);
        if (Objects.nonNull(webClients)) {
//            log.info("Removing " + pre.getId());
//            log.info(String.valueOf(webClients.size()));
//            log.info(String.valueOf(Objects.isNull(pre)));
            for (WebClient webClient : webClients) {
                if (webClient.getId() == pre.getId()) continue;
                traversalGraph(graph, delayMap, start, cur, webClient, delay);
            }
        }
    }

    /**
     * Use NetStateUtils to get delays between webClient 1 and the other nodes in network.
     *
     * @param delayMap
     */
    public static void pingFromWebClient1(double[][] delayMap, @NonNull MutableValueGraph graph) throws Exception {
        Set<WebClient> webClients = graph.nodes();
        for (WebClient webClient : webClients) {
            if (webClient.getId().equals("1")) continue;
            String ip = webClient.getIp();
            if (!StringUtils.isEmpty(ip)) {
                double delay = NetStateUtils.ping(ip);
                int id = Integer.valueOf(webClient.getId());
                delayMap[1][id] = delay;
                log.info("Ping from webClient 1 to " + webClient.getId() + " delay is:" + String.valueOf(delay));
            }
        }
    }
}
