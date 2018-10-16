package bupt.wspn.cache.service;

import bupt.wspn.cache.Utils.PropertyUtil;
import bupt.wspn.cache.Utils.RequestUtils;
import bupt.wspn.cache.model.Node;
import bupt.wspn.cache.model.NodeType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
    public int DEFAULT_REQUEST_NUMBER ;

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

    public WebClient simuWebClient(final String parentStr) {
        final String parentId = parentStr.equals("0") ? null : parentStr;
        if (Objects.nonNull(parentId) && !webClientMap.containsKey(parentId)) {
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
        final int capacity = Integer.valueOf(PropertyUtil.getProperty("slave." + nodeType + ".capacity"));
        final int resouceAmount = Integer.valueOf(PropertyUtil.getProperty("slave.resourceAmount"));
        final String ip = "simulator" + id;
        final String masterIp = "localhost";
        final WebClient webClient = new WebClient(ip, id, nodeType, name, parentId, capacity, resouceAmount, masterIp, new ArrayList<>());
        log.info("Put web client id:" + id + "to webClient map");
        webClientMap.put(id, webClient);
        return webClient;
    }

    public boolean delWebClient(final String nodeId) {
        webClientMap.remove(nodeId);
        return true;
    }

    public boolean simuRequest(@NonNull final String nodeId, @NonNull final String videoId) {
        final WebClient webClient = webClientMap.get(nodeId);
        if (Objects.isNull(webClient)) {
            log.info("Client " + nodeId + "does not exist.");
            return false;
        }
        return increaseResourceCount(webClient,videoId);
    }

    public boolean increaseResourceCount(@NonNull final WebClient webClient, @NonNull final String videoId){
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

    public boolean generateRequest(@NonNull final String nodeId, @NonNull final double lamda){
        return generateRequest(nodeId,lamda,DEFAULT_REQUEST_NUMBER);
    }

    public boolean generateRequest(@NonNull final String nodeId, @NonNull final double lamda, @NonNull final int requestNum) {
        log.info("Generate requests for webClient "+nodeId+" with parameter:" + lamda);
        final WebClient webClient = webClientMap.get(nodeId);
        if(Objects.isNull(webClient)){
            log.info("No such webClient");
            return false;
        }
        final Map<String,Integer> counters = webClient.getCounters();
        counters.clear();
        for(int i=0;i<requestNum;i++){
            final String videoId = RequestUtils.getRequestId(lamda);
            increaseResourceCount(webClient,videoId);
        }
        return true;
    }

    @PostConstruct
    public void initCacheService(){
        log.info(Integer.toString(DEFAULT_REQUEST_NUMBER));
    }
}
