package ca.unx.template.service;

import groovy.util.logging.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Define controller client operations here. Each client is a cache server that provides VOD services
 */
@Slf4j
@Getter
@Setter
public class WebClient {

    private static final int DEFAULT_CAPACITY = 30;

    @Value("${slave.ip}")
    private String ip;

//    private final String id;
//    private final NodeType nodeType;
//    private final String name;
//    private final String parentId;
//    private final int capacity;

    //Map indicating resources clicks.
    private final Map<String,Integer> counters = new HashMap<String,Integer>();
    //Map indicating which node resources locate.
    private final Map<String,Set<String>> resourceMap = new HashMap<>();
    //Map indicating delays between itself and the other nodes.
    private final Map<String,Integer> delayMap = new HashMap<String, Integer>();

//    public WebClient(String id, String ip,String name,NodeType nodeType,String parentId){
//        this.id = id;
//        this.ip = ip;
//        this.name = name;
//        this.nodeType = nodeType;
//        this.parentId = parentId;
//        this.capacity = DEFAULT_CAPACITY;
//    }
}
