package ca.unx.template.service;

import ca.unx.template.model.Node;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Define cache service operations here
 */
@Service
@Slf4j
public class CacheService {
    public final List<Node> nodeList = new ArrayList<>();

    /**
     * get delays among cache nodes.
     * @return
     */
    public Map<String,Map<String,Integer>> getNetworkDelays(){
        return null;
    }

    /**
     * update system cache by G-S algorithm.
     * @return
     */
    public Map<String,List<String>> updateCache(){
        return null;
    }

}
