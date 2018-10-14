package bupt.wspn.cache.service;

import bupt.wspn.cache.model.Node;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Define cache service operations here
 */
@Service
@Slf4j
public class CacheService {
    public final Map<String,WebClient> webClientMap = new HashMap<>();

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

    public boolean bindWebClient(String jsonStr){
        try {
            final WebClient webClient = JSON.parseObject(jsonStr,WebClient.class);
            final String webClientId = webClient.getId();
            log.info("Master bind webClient " + webClientId);
            webClientMap.put(webClientId,webClient);
            return true;
        }catch (Exception e){
            log.info("Master bind failure: " + e.toString());
            return false;
        }
    }

    public boolean unBindWebClient(String clientId){
        if(webClientMap.containsKey(clientId)){
            webClientMap.remove(clientId);
            return true;
        }else return false;
    }
}
