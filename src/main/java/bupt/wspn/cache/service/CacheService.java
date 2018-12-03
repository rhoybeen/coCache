package bupt.wspn.cache.service;

import bupt.wspn.cache.Utils.*;
import bupt.wspn.cache.model.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.graph.MutableValueGraph;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Define cache service operations here
 */
@Getter
@Setter
@Service
@Slf4j
@Order(2)
public class CacheService {

    @Value("${slave.default_request_number}")
    public int DEFAULT_REQUEST_NUMBER;

    @Value("${slave.resourceAmount}")
    public int RESOURCE_AMOUNT;

    @Value("${cache.delay.BASE_DELAY}")
    public int BASE_SERVICE_DELAY;

    @Value("${cache.delay.MISS_DELAY}")
    public int MISS_DELAY;

    public int MAX_CLIENT_NUM = 15;

    public final Map<String, WebClient> webClientMap = new HashMap<>();

    public transient MutableValueGraph<WebClient, Edge> graph;

    /**
     * It is not reasonable to assume that all webClient id is number which can be converted into an Integer type.
     * But here, just simplify the problem.
     */
    public double[][] delayMap = new double[MAX_CLIENT_NUM + 1][MAX_CLIENT_NUM + 1];

    /**
     * get delays among cache nodes.
     * and returned modified delay to the front-end
     *
     * @return
     */
    public Map<String, Map<String, Double>> retrieveNetworkDelays() {
        TopoUtils.getSimuGraphDelays(graph, delayMap);
        final Map<String, Map<String, Double>> result = new HashMap<>();
        for (WebClient webClient : webClientMap.values()) {
            final String id = webClient.getId();
            final Map<String, Double> clientMap = new HashMap<>();
            for (WebClient webClient1 : webClientMap.values()) {
                final String id1 = webClient1.getId();
                double delay = delayMap[Integer.valueOf(id)][Integer.valueOf(id1)] + BASE_SERVICE_DELAY;
                clientMap.put(id1, delay);
            }
            result.put(id, clientMap);
        }
        return result;
    }

    /**
     * update system cache by G-S algorithm.
     * And return the expected avg service delay.
     *
     * @return
     */
    public Map<String, Object> updateCache(final String strategy) {
        log.info("Update system cache now.");
        final Map<String, Object> res = new HashMap<>();
        //Sync requests from every nodes
        //Only sync from web client 1. because the other web clients are simulated in local host.
        final WebClient webClient = getWebClientInfo(webClientMap.get("1").getIp());
        webClientMap.put("1", webClient);
        TopoUtils.createGraphFromMap(webClientMap);

        //update network delay map
        TopoUtils.getSimuGraphDelays(graph, delayMap);

        //allocate content to corresponding node
        Map<String, SortableClientEntity> clientEntityMap = CacheUtils.updateCache(strategy, this);
        //notify web client to update their cache
        Map<String, List<String>> resourceMap = CacheUtils.generateResourceMap(this, clientEntityMap);
        log.info("Resource map : " + resourceMap.toString());
        for (WebClient client : webClientMap.values()) {
            final Map<String, List<String>> map = new HashMap<>();
            for (Map.Entry entry : resourceMap.entrySet()) {
                final String key = (String) entry.getKey();
                final List<String> locations = (ArrayList<String>) entry.getValue();
                final List<String> copyTo = new ArrayList<>();
                for (String item : locations) {
                    copyTo.add(item);
                }
                map.put(key, copyTo);
            }
            client.setResourceMap(map);
        }
        //Calculate expected average service delay.
        final Map<String, Double> expectedDelayMap = CacheUtils.calculateExpectedStrategyDelay(this);
        //Clean up history requests for all clients.
        //cleanUpClientHistory();
        //Sync new resource map to client 1.
        syncToWebClient1();
        res.put("expectedDelay", expectedDelayMap);
        res.put("webClients", webClientMap.values());
        res.put("resourceMap", resourceMap);
        res.put("delayMap", delayMap);
        return res;
    }

    public Map<String, Object> evaluateCacheStrategies() {
        log.info("Evaluate cache strategies now.");
        final Map<String, Object> res = new HashMap<>();
        //Sync requests from every nodes
        //Only sync from web client 1. because the other web clients are simulated in local host.
        final WebClient webClient = getWebClientInfo(webClientMap.get("1").getIp());
        webClientMap.put("1", webClient);
        TopoUtils.createGraphFromMap(webClientMap);
        //update network delay map
        TopoUtils.getSimuGraphDelays(graph, delayMap);
        //Calculate expected average service delay.
        final Map<String, Double> expectedDelayMap = CacheUtils.calculateExpectedStrategyDelay(this);
        res.put("expectedDelay", expectedDelayMap);
        return res;
    }

    public boolean bindWebClient(String jsonStr) {
        try {
            final WebClient webClient = JSON.parseObject(jsonStr, WebClient.class);
            final String webClientId = webClient.getId();
            log.info("Master bind webClient " + webClientId);
            webClientMap.put(webClientId, webClient);
            graph = TopoUtils.createGraphFromMap(webClientMap);
            return true;
        } catch (Exception e) {
            log.info("Master bind failure: " + e.toString());
            return false;
        }
    }

    public boolean sync(String jsonStr) {
        try {
            final WebClient webClient = JSON.parseObject(jsonStr, WebClient.class);
            final String webClientId = webClient.getId();
            log.info("Master sync webClient " + webClientId);
            webClientMap.put(webClientId, webClient);
            graph = TopoUtils.createGraphFromMap(webClientMap);
            return true;
        } catch (Exception e) {
            log.info("Master sync failure: " + e.toString());
            return false;
        }
    }

    /**
     * Get webClient info from a specified ip address.
     * Return a re-constructed web client.
     *
     * @param ip
     * @return
     */
    public WebClient getWebClientInfo(@NonNull String ip) {
        //    if (StringUtils.isEmpty(ip) || !IPAddressUtil.isIPv4LiteralAddress(ip)) return null;
        final RequestEntity request = RequestEntity.builder()
                .type("UPLOAD")
                .build();
        try {
            final String url = "http://" + ip + "/slave/info";
            log.info("Send request to " + ip + " to report its details");
            final String response = HttpUtils.sendHttpRequest(url, request);
            final JSONObject jsonObject = JSONObject.parseObject(response);
            final Boolean isSuccess = jsonObject.getBoolean("isSuccess");
            if (isSuccess) {
                return JSON.parseObject(jsonObject.getString("payload"), WebClient.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Failed to send request to client to sync its details:" + ip);
        }
        return null;
    }

    /**
     * Sync to client node 1.
     *
     * @return
     */
    public boolean syncToWebClient1() {
        final WebClient webClient = webClientMap.get("1");
        final RequestEntity request = RequestEntity.builder()
                .type("SYNC")
                .params(webClient)
                .build();
        try {
            final String clientIp = webClient.getIp();
            final String url = "http://" + clientIp + "/slave/sync";
            log.info("Cache Server sync with client 1 " + url);
            final String responseStr = HttpUtils.sendHttpRequest(url, request);
            return true;
        } catch (Exception e) {
            log.warn("Cache Server failed to sync with client 1.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean unBindWebClient(String clientId) {
        if (webClientMap.containsKey(clientId)) {
            webClientMap.remove(clientId);
            graph = TopoUtils.createGraphFromMap(webClientMap);
            return true;
        } else return false;
    }

    public Set<WebClient> retrieveCocacheNodes() {
        final Set<WebClient> nodes = new HashSet<>(webClientMap.values());
        return nodes;
    }

    /**
     * Simulate a slave client.
     *
     * @param parentId
     * @return
     */
    public WebClient simuWebClient(final String parentId) {
        if (!parentId.equals("0") && !webClientMap.containsKey(parentId)) {
            log.info("Create simu webClient failed. " + parentId + "does not exist.");
            return null;
        }
        final WebClient parent = webClientMap.get(parentId);
        String id = null;
        for (int i = 1; i < 100; i++) {
            log.info(String.valueOf(i));
            if (webClientMap.containsKey(String.valueOf(i))) {
                log.info("Contains key " + i);
                continue;
            } else {
                id = String.valueOf(i);
                break;
            }
        }
        if (Objects.isNull(id)) return null;
        final String name = 'S' + id;
        final NodeType nodeType;
        //Construct node type
        if (Objects.isNull(parent)) {
            nodeType = NodeType.REGIONAL_MEC;
        } else {
            nodeType = (parent.getNodeType() == NodeType.REGIONAL_MEC) ? NodeType.MBS_MEC : NodeType.SBS_MEC;
        }
        final int capacity = Integer.valueOf(PropertyUtils.getProperty("slave." + nodeType + ".capacity"));
        final String ip = "simulator" + id;
        final String masterIp = "localhost";
        final WebClient webClient = new WebClient(ip, id, nodeType, name, parentId, capacity, RESOURCE_AMOUNT, masterIp);
        webClient.initCountersAndResources(true, true);
        //todo: set up other variants in web client to avoid errors.
        log.info("Put web client id:" + id + "to webClient map");
        webClientMap.put(id, webClient);
        graph = TopoUtils.createGraphFromMap(webClientMap);
        return webClient;
    }

    /**
     * WebClient can be configured in slave.properties.
     * This method will overrode the previously created webClients which own the same id.
     *
     * @return
     */
    public boolean createWebClientFromConfiguration() {
        final int webClientNumber = Integer.valueOf(PropertyUtils.getProperty("cache.client.number"));
        for (int i = 1; i <= webClientNumber; i++) {
            //Create webClient
            final String webClientPrefix = "cache.client." + i + ".";
            final String id = PropertyUtils.getProperty(webClientPrefix + "id");
            log.info(webClientPrefix + "id");
            final String ip = PropertyUtils.getProperty(webClientPrefix + "ip");
            final String name = PropertyUtils.getProperty(webClientPrefix + "name");
            final String type = PropertyUtils.getProperty(webClientPrefix + "type");
            final String parentId = PropertyUtils.getProperty(webClientPrefix + "parentId");
            final int pivot = Integer.valueOf(PropertyUtils.getProperty(webClientPrefix + "pivot"));
            log.info("Create webClient from property file with id" + id + " ip" + ip + " name" + name + " type" + type + " parentId" + parentId);
            final WebClient webClient = createWebClient(id, name, type, ip, parentId, pivot);
            if (Objects.isNull(webClient)) return false;
            this.webClientMap.put(id, webClient);
        }
        graph = TopoUtils.createGraphFromMap(webClientMap);
        return true;
    }

    /**
     * Create new web client.
     *
     * @param id
     * @param name
     * @param type
     * @param ip
     * @param parentId
     * @return
     */
    public WebClient createWebClient(@NonNull final String id,
                                     @NonNull final String name,
                                     @NonNull final String type,
                                     @NonNull final String ip,
                                     @NonNull final String parentId,
                                     final int pivot) {
        if (webClientMap.containsKey(id)) {
            log.info("Webclient with id" + id + "already exist.");
            return null;
        }
        final NodeType nodeType = NodeType.valueOf(type);
        final int capacity = Integer.valueOf(PropertyUtils.getProperty("slave." + type + ".capacity"));
        final int resourceAmount = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
        final String masterIp = PropertyUtils.getProperty("slave.masterIp");
        final WebClient webClient = new WebClient(ip, id, nodeType, name, parentId, capacity, resourceAmount, masterIp);
        webClient.setPivot(pivot);
        return webClient;
    }

    public boolean delWebClient(final String nodeId) {
        log.info("Remove web client by id:" + nodeId);
        webClientMap.remove(nodeId);
        graph = TopoUtils.createGraphFromMap(webClientMap);
        return true;
    }

    /**
     * Manually request video from a specified client.
     *
     * @param nodeId
     * @param videoId
     * @return
     */
    public boolean simuRequest(@NonNull final String nodeId, @NonNull final String videoId) {
        final WebClient webClient = webClientMap.get(nodeId);
        if (Objects.isNull(webClient)) {
            log.info("Client " + nodeId + "does not exist.");
            return false;
        }
        return increaseResourceCount(webClient, videoId);
    }

    /**
     * Request a video source and increase its counter.
     *
     * @param webClient
     * @param videoId
     * @return
     */
    public boolean increaseResourceCount(@NonNull final WebClient webClient, @NonNull final String videoId) {
        final Map<String, List<String>> resourceMap = webClient.getResourceMap();
        if (!resourceMap.containsKey(videoId)) {
            log.info("Video " + videoId + " does not exist.");
            return false;
        }
        final Map<String, Integer> counts = webClient.getCounters();
        final Integer count = Optional.ofNullable(counts.get(videoId)).orElse(0);
        counts.put(videoId, count + 1);
        return true;
    }

    /**
     * Generate requests for all clients.
     * Modify it if you want different clients have different lamda arg.
     * It supposes that only client 1 is a real client.
     *
     * @param lamda
     * @return
     */
    public boolean generateRequest(final double lamda) {
        log.info("Generate requests for all webClients.");
        for (final WebClient webClient : webClientMap.values()) {
            generateRequest(webClient.getId(), lamda);
            int count = 0;
            for (int i : webClient.getCounters().values()) {
                count += i;
            }
            log.info("Client " + webClient.getId() + " current request number " + count);
        }
        return syncToWebClient1();
    }

    public boolean generateRequestByClientType(final String nodeType, final double lamda) {
        log.info("Generate requests for edge web clients.");
        NodeType type;
        try {
            type = NodeType.valueOf(nodeType);
        } catch (IllegalArgumentException e) {
            log.info(nodeType + "is not supported node type");
            return false;
        }
        for (final WebClient webClient : webClientMap.values()) {
            if (webClient.getNodeType() != type) continue;
            generateRequest(webClient.getId(), lamda);
            int count = 0;
            for (int i : webClient.getCounters().values()) {
                count += i;
            }
            log.info("Client " + webClient.getId() + " current request number " + count);
        }
        return syncToWebClient1();
    }

    /**
     * Generate request for specified web client
     *
     * @param nodeId
     * @param lamda
     * @return
     */
    public boolean generateRequest(@NonNull final String nodeId, final double lamda) {
        log.info("Generate requests for webClient " + nodeId + " with parameter:" + lamda);
        final WebClient webClient = webClientMap.get(nodeId);
        if (Objects.isNull(webClient)) {
            log.info("No such webClient " + nodeId);
            return false;
        }
        //    webClient.initCountersAndResources(true,false);
        final List<String> requests = RequestUtils.getRequestId(lamda, webClient.getPivot());
        log.info("Generate request for node id: " + nodeId + " with number " + requests.size());
        for (String requestId : requests) {
            increaseResourceCount(webClient, requestId);
        }
        //log.info("After:" + webClient.getCounters().toString());
        return true;
    }

    public boolean cleanUpClientHistory() {
        for (WebClient webClient : webClientMap.values()) {
            webClient.initCountersAndResources(true, false);
        }
        return true;
    }

    public boolean resetSystemCache() {
        for (WebClient webClient : webClientMap.values()) {
            webClient.initCountersAndResources(false, true);
        }
        return true;
    }

    @PostConstruct
    public void initCacheService() {
        createWebClientFromConfiguration();
        //     generateRequest(0.60);
    }
}
