package bupt.wspn.cache.service;

import bupt.wspn.cache.Utils.FilenameConvertor;
import bupt.wspn.cache.Utils.HttpUtils;
import bupt.wspn.cache.model.NodeType;
import bupt.wspn.cache.model.RequestEntity;
import bupt.wspn.cache.model.Video;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Define controller client operations here. Each client is a cache server that provides VOD services
 */
@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class WebClient {

    @NonNull
    @Value("${slave.ip}")
    private String ip;

    @NonNull
    @Value("${slave.id}")
    private String id;

    @NonNull
    @Value("${slave.type}")
    private NodeType nodeType;

    @NonNull
    @Value("${slave.name}")
    private String name;

    @NonNull
    @Value("${slave.parentId}")
    private String parentId;

    @NonNull
    @Value("${slave.SBS_MEC.capacity}")
    private int capacity;

    @NonNull
    @Value("${slave.resourceAmount}")
    private int resourceAmount;

    @NonNull
//    @JSONField(serialize = false)
    @Value("${slave.masterIp}")
    private String masterIp;

//    @JSONField(serialize = false)
    @Value("${slave.remoteServerIp}")
    private String remoteServerIp;

    public transient int pivot;

    //Map indicating resources clicks.
    private Map<String, Integer> counters = new HashMap<String, Integer>();
    //Map indicating which node resources locate.
    private Map<String, List<String>> resourceMap = new HashMap<>();
    //Map indicating delays between itself and the other nodes.
    private Map<String, Integer> delayMap = new HashMap<String, Integer>();
    //provide sorted video list.
    private List<Video> resources = new ArrayList<Video>();

    public WebClient(final String ip,
                     final String id,
                     final NodeType nodeType,
                     final String name,
                     final String parentId,
                     final int capacity,
                     final int resourceAmount,
                     final String masterIp) {
        this.ip = ip;
        this.id = id;
        this.nodeType = nodeType;
        this.name = name;
        this.parentId = parentId;
        this.capacity = capacity;
        this.resourceAmount = resourceAmount;
        this.masterIp = masterIp;
        initCountersAndResources(true,true);
    }

    /**
     * Init counters and resourceMap
     */
    public void initCountersAndResources(boolean initCounters, boolean initResources) {
        final int resourceSize = this.resourceAmount;
        for (int i = 1; i <= resourceSize; i++) {
            final String fileNameNum = FilenameConvertor.toStringName(i);
            if(initCounters) this.counters.put(fileNameNum, 0);
            if(initResources) this.resourceMap.put(fileNameNum, new ArrayList<>());
        }
    }

    /**
     * Bind web client to cache-control(master) server. It's a multi-to-one relationship.
     */
    public String bind() {
        final RequestEntity request = RequestEntity.builder()
                .type("BIND")
                .params(this)
                .build();
        try {
            final String url = "http://" + masterIp + "/console/bind";
            log.info("WebClient" + this.id + " bind to master server " + url);
            final String responseStr = HttpUtils.sendHttpRequest(url, request);
            return responseStr;
        } catch (Exception e) {
            log.warn("WebClient " + this.id + "fails to bind master server.");
            e.printStackTrace();
            return "error occurs";
        }
    }

    public String unbind() {
        final RequestEntity request = RequestEntity.builder().type("UNBIND").params(this.id).build();
        try {
            final String url = "http://" + masterIp + "/console/unbind";
            log.info("WebClient" + this.id + " unbind from master server " + url);
            final String responseStr = HttpUtils.sendHttpRequest(url, request);
            return responseStr;
        } catch (Exception e) {
            log.warn("WebClient " + this.id + "fails to unbind from master server.");
            e.printStackTrace();
            return "error occurs";
        }
    }

    public WebClient retrieveDataResources() {
        updateVideoList();
        return this;
    }

    /**
     * sort video by popularity.
     */
    public void updateVideoList() {
        final List<Video> newList = new ArrayList<Video>();
        for (String key : counters.keySet()) {
            final Video video = Video.builder()
                    .name(key)
                    .clickNum(counters.get(key))
                    .build();
            newList.add(video);
        }
        Collections.sort(newList);
        resources = newList;
    }

    /**
     * Sync from master cache server.
     *
     * @param webStr
     * @return
     */
    public boolean sync(@NonNull final String webStr) {
        final WebClient webClient = JSON.parseObject(webStr, WebClient.class);
        final String webClientId = webClient.getId();
        if (!webClientId.equals(this.id)) return false;
        this.resources = webClient.resources;
        this.resourceMap = webClient.resourceMap;
        this.counters = webClient.counters;
        this.delayMap = webClient.delayMap;
        log.info("Sync from master cache server " + webStr);
        return true;
    }

    public boolean syncWithCacheServer() {
        final RequestEntity request = RequestEntity.builder().type("SYNC").params(this.id).build();
        try {
            final String url = "http://" + masterIp + "/console/sync";
            log.info("WebClient" + this.id + " unbind from master server " + url);
            final String responseStr = HttpUtils.sendHttpRequest(url, request);
            return true;
        } catch (Exception e) {
            log.warn("WebClient " + this.id + "fails to unbind from master server.");
            return false;
        }
    }

    public Map<String,Object> handleVideoRequest(@NonNull final String videoName){
        final List<String> locations = resourceMap.get(videoName);
        if(Objects.isNull(locations)) return null;
        final Map<String,Object> result = new HashMap<>();
        final int countBefore = counters.get(videoName);
        counters.put(videoName,countBefore+1);
        result.put("locations",locations);
        return result;
    }

    /**
     * Init function for webClient.
     */
    @PostConstruct
    public void initWebClient() {
        log.info("WebClient init method.");
        final int resourceSize = resourceAmount;
        this.pivot = 1;
        for (int i = 1; i <= resourceSize; i++) {
            final String fileNameNum = FilenameConvertor.toStringName(i);
            counters.put(fileNameNum, i);
            resourceMap.put(fileNameNum, new ArrayList<>());
        }
    }
}
