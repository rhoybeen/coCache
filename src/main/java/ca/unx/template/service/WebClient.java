package ca.unx.template.service;

import ca.unx.template.Utils.HttpUtils;
import ca.unx.template.model.NodeType;
import ca.unx.template.model.RequestEntity;
import ca.unx.template.model.ResponseEntity;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Define controller client operations here. Each client is a cache server that provides VOD services
 */
@Getter
@Setter
@Slf4j
public class WebClient {

    //Cache
    private static final int DEFAULT_CAPACITY = 30;

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

    @JSONField(serialize = false)
    @Value("${slave.masterIp}")
    private String masterIp;

    //Map indicating resources clicks.
    private final Map<String, Integer> counters = new HashMap<String, Integer>();
    //Map indicating which node resources locate.
    private final Map<String, Set<String>> resourceMap = new HashMap<>();
    //Map indicating delays between itself and the other nodes.
    private final Map<String, Integer> delayMap = new HashMap<String, Integer>();

    /**
     * Bind web client to cache-control(master) server. It's a multi-to-one relationship.
     */
    public String bind() {
        final RequestEntity request = RequestEntity.builder()
                .type("BIND")
                .params(this)
                .build();
        try{
            final String url = "http://" + masterIp + "/cache/bind";
            log.info("WebClient" + this.id + " bind to master server " + url);
            final String responseStr = HttpUtils.sendHttpRequest(url,request);
//            final JSONObject response = JSONObject.parseObject(responseStr);
            return responseStr;
        }catch (Exception e){
            log.warn("WebClient "+ this.id + "fails to bind master server.");
            e.printStackTrace();
            return "error occurs";
        }
    }

}
