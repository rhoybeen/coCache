package bupt.wspn.cache.service;

import bupt.wspn.cache.Utils.FilenameConvertor;
import bupt.wspn.cache.Utils.HttpUtils;
import bupt.wspn.cache.Utils.PropertyUtils;
import bupt.wspn.cache.Utils.RequestUtils;
import bupt.wspn.cache.model.NodeType;
import bupt.wspn.cache.model.RequestEntity;
import com.alibaba.fastjson.JSON;
import jdk.nashorn.internal.ir.RuntimeNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.springframework.beans.factory.annotation.Value;
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
public class CacheService {

    @Value("${slave.default_request_number}")
    public int DEFAULT_REQUEST_NUMBER;

    public final Map<String, WebClient> webClientMap = new HashMap<>();

    /**
     * get delays among cache nodes.
     *
     * @return
     */
    public Map<String, Map<String, Integer>> retrieveNetworkDelays() {
        return null;
    }

    /**
     * update system cache by G-S algorithm.
     *
     * @return
     */
    public Map<String, List<String>> updateCache() {
        return null;
    }

    public boolean bindWebClient(String jsonStr) {
        try {
            final WebClient webClient = JSON.parseObject(jsonStr, WebClient.class);
            final String webClientId = webClient.getId();
            log.info("Master bind webClient " + webClientId);
            webClientMap.put(webClientId, webClient);
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
            return true;
        } catch (Exception e) {
            log.info("Master sync failure: " + e.toString());
            return false;
        }
    }

    /**
     * Sync with client node 1.
     *
     * @return
     */
    public boolean syncWithClient1() {
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
        final int resouceAmount = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
        final String ip = "simulator" + id;
        final String masterIp = "localhost";
        final WebClient webClient = new WebClient(ip, id, nodeType, name, parentId, capacity, resouceAmount, masterIp);
        webClient.initCountersAndResources();
        //todo: set up other variants in web client to avoid errors.
        log.info("Put web client id:" + id + "to webClient map");
        webClientMap.put(id, webClient);
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
        for (int i = 0; i < webClientNumber; i++) {
            //Create webClient
            final String webClientPrefix = "cache.client." + i + ".";
            final String id = PropertyUtils.getProperty(webClientPrefix + "id");
            final String ip = PropertyUtils.getProperty(webClientPrefix + "ip");
            final String name = PropertyUtils.getProperty(webClientPrefix + "name");
            final String type = PropertyUtils.getProperty(webClientPrefix + "type");
            final String parentId = PropertyUtils.getProperty(webClientPrefix + "parentId");
            final WebClient webClient = createWebClient(id, name, type, ip, parentId);
            if (Objects.isNull(webClient)) return false;
            this.webClientMap.put(id, webClient);
        }
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
                                     @NonNull final String parentId) {
        if (webClientMap.containsKey(id)) {
            log.info("Webclient with id" + id + "already exist.");
            return null;
        }
        final NodeType nodeType = NodeType.valueOf(type);
        final int capacity = Integer.valueOf(PropertyUtils.getProperty("slave." + type + ".capacity"));
        final int resourceAmount = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
        final String masterIp = PropertyUtils.getProperty("slave.masterIp");
        return new WebClient(ip, id, nodeType, name, parentId, capacity, resourceAmount, masterIp);
    }

    public boolean delWebClient(final String nodeId) {
        log.info("Remove web client by id:" + nodeId);
        webClientMap.remove(nodeId);
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
        final Map<String, Set<String>> resourceMap = webClient.getResourceMap();
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
        }
        return syncWithClient1();
    }

    /**
     * Generate request for specified web client
     *
     * @param nodeId
     * @param lamda
     * @return
     */
    public boolean generateRequest(@NonNull final String nodeId, final double lamda) {
        return generateRequest(nodeId, lamda, DEFAULT_REQUEST_NUMBER);
    }

    /**
     * Generate request for specified web client by a certain request number.
     *
     * @param nodeId
     * @param lamda
     * @param requestNum
     * @return
     */
    public boolean generateRequest(@NonNull final String nodeId, final double lamda, final int requestNum) {
        log.info("Generate requests for webClient " + nodeId + " with parameter:" + lamda);
        final WebClient webClient = webClientMap.get(nodeId);
        if (Objects.isNull(webClient)) {
            log.info("No such webClient " + nodeId);
            return false;
        }
        final Map<String, Integer> counters = webClient.getCounters();
        counters.clear();
        final List<String> requests = RequestUtils.getRequestId(lamda, true);
        log.info(requests.toString());
        for (String requestId : requests) {
            increaseResourceCount(webClient, requestId);
        }
        log.info("After:" + webClient.getCounters().toString());
        return true;
    }

    @PostConstruct
    public void initCacheService() {
    }
}
