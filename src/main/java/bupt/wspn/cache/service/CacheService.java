package bupt.wspn.cache.service;

import bupt.wspn.cache.Utils.PropertyUtil;
import bupt.wspn.cache.model.Node;
import bupt.wspn.cache.model.NodeType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public Map<String,Map<String,Integer>> retrieveNetworkDelays(){
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

    public Set<WebClient> retrieveCocacheNodes(){
        final Set<WebClient> nodes = new HashSet<>(webClientMap.values());
        return nodes;
    }

    public WebClient simuWebClient(final String parentStr){
        final String parentId = parentStr.equals("0") ? null : parentStr;
        if(Objects.nonNull(parentId) && !webClientMap.containsKey(parentId)){
            log.info("Create simu webClient failed. " + parentId + "does not exist.");
            return null;
        }
        final WebClient parent = webClientMap.get(parentId);
        String id = null;
        for(int i=1;i<100;i++){
            log.info(String.valueOf(i));
            if(webClientMap.containsKey(String.valueOf(i))){
                log.info("Contains key "+ i);
                continue;
            }else {
                id = String.valueOf(i);
                break;
            }
        }
        if(Objects.isNull(id)) return null;
        final String name = 'S' + id;
        final NodeType nodeType;
        //Construct node type
        if(Objects.isNull(parent)){
            nodeType = NodeType.REGIONAL_MEC;
        }else{
            nodeType = (parent.getNodeType() == NodeType.REGIONAL_MEC) ? NodeType.MBS_MEC : NodeType.SBS_MEC;
        }
        final int capacity = Integer.valueOf(PropertyUtil.getProperty("slave." + nodeType + ".capacity"));
        final int resouceAmount = Integer.valueOf(PropertyUtil.getProperty("slave.resourceAmount"));
        final String ip = "simulator" + id;
        final String masterIp = "localhost";
        final WebClient webClient = new WebClient(ip,id,nodeType,name,parentId,capacity,resouceAmount,masterIp,new ArrayList<>());
        log.info("Put web client id:" + id + "to webClient map");
        webClientMap.put(id,webClient);
        return webClient;
    }

    public boolean delWebClient(final String nodeId){
        webClientMap.remove(nodeId);
        return true;
    }
}
