package bupt.wspn.cache.service;

import bupt.wspn.cache.Utils.HttpUtils;
import bupt.wspn.cache.model.NodeType;
import bupt.wspn.cache.model.RequestEntity;
import bupt.wspn.cache.model.Video;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
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
public class WebClient {

    @Value("${slave.ip}")
    private String ip;

    @Value("${slave.id}")
    private String id;

    @Value("${slave.type}")
    private NodeType nodeType;

    @Value("${slave.name}")
    private String name;

    @Value("${slave.parentId}")
    private String parentId;

    @Value("${slave.capacity}")
    private int capacity;

    @Value("${slave.resourceAmount}")
    private int resourceAmount;

    @JSONField(serialize = false)
    @Value("${slave.masterIp}")
    private String masterIp;

    //Map indicating resources clicks.
    private final Map<String, Integer> counters = new HashMap<String, Integer>();
    //Map indicating which node resources locate.
    private final Map<String, Set<String>> resourceMap = new HashMap<>();
    //Map indicating delays between itself and the other nodes.
    private final Map<String, Integer> delayMap = new HashMap<String, Integer>();

    //provide sorted video list.
    private List<Video> resources = new ArrayList<Video>();

    /**
     * Bind web client to cache-control(master) server. It's a multi-to-one relationship.
     */
    public String bind() {
        final RequestEntity request = RequestEntity.builder()
                .type("BIND")
                .params(this)
                .build();
        try {
            final String url = "http://" + masterIp + "/cache/bind";
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
            final String url = "http://" + masterIp + "/cache/unbind";
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
     * It is a test init function to set up webClient.
     */
    @PostConstruct
    public void tmpInit() {
        log.info("WebClient init method.");
        final int resourceSize = resourceAmount;
        for (int i = 1; i <= resourceSize; i++) {
            final String fileNameNum = String.format("%03d", i);
            counters.put(fileNameNum,i);
            resourceMap.put(fileNameNum,new HashSet<>());
        }
    }
}
