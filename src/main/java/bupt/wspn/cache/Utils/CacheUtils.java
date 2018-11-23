package bupt.wspn.cache.Utils;

import bupt.wspn.cache.model.SortableClientEntity;
import bupt.wspn.cache.model.SortableVideoEntity;
import bupt.wspn.cache.model.Video;
import bupt.wspn.cache.service.CacheService;
import bupt.wspn.cache.service.WebClient;
import com.google.common.graph.MutableValueGraph;
import org.springframework.util.StringUtils;
import sun.rmi.transport.ObjectTable;

import java.util.*;

public class CacheUtils {

    public static final String CACHE_STRATEGY_GS_ALGORITHM = "GS";

    public static final String CACHE_STRATEGY_RANDOM_COOPERATE = "RAN_CO";

    public static final String CACHE_STRATEGY_POPULARITY_NON_COOPERATE = "POP_NON_CO";

    public static final String CACHE_STRATEGY_POPULARITY_COOPERATE = "POP_CO";

    /**
     * update system cache by G-S algorithm.
     * And return the expected avg service delay.
     *
     * @return
     */
    public static Map<String, SortableClientEntity> updateCache(String method, CacheService cacheService) {
        switch (method) {
            case CACHE_STRATEGY_GS_ALGORITHM:
                return GSAlgorithm(cacheService);
            default:
                return new HashMap<String, SortableClientEntity>();
        }
    }

    //todo: make sure whether need to call updateVideoList() first.
    //todo: the preference list sort order from high to low score.
    public static Map<String, SortableClientEntity> GSAlgorithm(final CacheService cacheService) {
        final MutableValueGraph graph = cacheService.getGraph();
        final double[][] delayMap = cacheService.getDelayMap();
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final Set<WebClient> webClients = graph.nodes();
        Map<String, SortableClientEntity> clientEntityMap = new HashMap<>();
        Map<String, SortableVideoEntity> videoEntityMap = new HashMap<>();
        for (WebClient webClient : webClients) {
            clientEntityMap.put(webClient.getId(), new SortableClientEntity(
                    webClient.getId(),
                    webClient.getCapacity(),
                    new Double(0),
                    new HashMap<>(),
                    new ArrayList<>()
            ));
        }
        int resourceNum = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
        for (int i = 1; i <= resourceNum; i++) {
            String filename = FilenameConvertor.toStringName(i);
            videoEntityMap.put(filename, new SortableVideoEntity(filename,
                    new Double(0),
                    false,
                    new HashMap<>(),
                    new ArrayList<>()));
        }
        //For each client, generate its preference list
        for (SortableClientEntity clientEntity : clientEntityMap.values()) {
            String clientId = clientEntity.getClientId();
            final WebClient webClient = webClientMap.get(clientId);
            webClient.updateVideoList();
            //The current client preference list is already in order now.
            final List<Video> videoList = webClient.getResources();
            for (Video video : videoList) {
                clientEntity.getPreferenceMap().put(video.name, new SortableVideoEntity(
                        video.name,
                        new Double(video.getClickNum()),
                        false,
                        null,
                        null
                ));
            }
        }
        //For each content, generate its preference list
        //In general, the allocation algorithm should take all nodes into consideration.
        for (SortableVideoEntity videoEntity : videoEntityMap.values()) {
            final String contentName = videoEntity.getName();
            for (WebClient current : webClientMap.values()) {
                double preferenceScore = 0;
                final String currentId = current.getId();
                for (WebClient otherClient : webClientMap.values()) {
                    final String otherId = otherClient.getId();
                    if (currentId.equals(otherId)) continue;
                    preferenceScore += otherClient.getCounters().get(contentName)
                            * delayMap[Integer.valueOf(currentId)][Integer.valueOf(otherId)];
                }
                videoEntity.getPreferenceMap().put(currentId, new SortableClientEntity(
                        currentId,
                        current.getCapacity(),
                        preferenceScore,
                        null,
                        null
                ));
            }
//            Collections.sort(videoEntity.getPreferenceList());
        }

        while (!isAllocationFinished(clientEntityMap, cacheService)) {
            for (SortableVideoEntity videoEntity : videoEntityMap.values()) {
                if (videoEntity.isAccepted()) continue;
                final List<SortableClientEntity> videoPreferenceList = videoEntity.getPreferenceList();
                if (!videoPreferenceList.isEmpty()) {
                    final String mostPreferredClientId = videoPreferenceList.remove(0).getClientId();
                    final SortableClientEntity mostPreferredClient = clientEntityMap.get(mostPreferredClientId);
                    final String rejectedId = mostPreferredClient.tryAccept(videoEntity);
                    videoEntity.setAccepted(true);
                    if (!StringUtils.isEmpty(rejectedId)) {
                        videoEntityMap.get(rejectedId).setAccepted(false);
                    }
                }
            }
        }
        return clientEntityMap;
    }

    /**
     * Update cache by random strategy.
     * @param cacheService
     * @return
     */
    public static Map<String, SortableClientEntity> RandomCooperateStrategy(final CacheService cacheService){
        final MutableValueGraph graph = cacheService.getGraph();
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final Set<WebClient> webClients = graph.nodes();
        final Map<String,SortableClientEntity> clientEntityMap = new HashMap<>();

        int totalResourceSize = 0;
        for (WebClient webClient : webClients) {
            clientEntityMap.put(webClient.getId(), new SortableClientEntity(
                    webClient.getId(),
                    webClient.getCapacity(),
                    new Double(0),
                    new HashMap<>(),
                    new ArrayList<>()
            ));
            totalResourceSize += webClient.getCapacity();
        }
        int resourceNum = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
        Random random = new Random();
        Set<Integer> resultSet = new HashSet<>();
        while(resultSet.size()<resourceNum){
            resultSet.add(random.nextInt(cacheService.getRESOURCE_AMOUNT())+1);
        }
        final Iterator iterator = clientEntityMap.values().iterator();
        SortableClientEntity currentClientEntity = null;

        for(int content : resultSet){
            if(currentClientEntity==null && iterator.hasNext()){
                currentClientEntity = (SortableClientEntity) iterator.next();
            }
            final List acceptList = currentClientEntity.getAcceptList();
            if(acceptList.size() >= currentClientEntity.getCapacity()){
                currentClientEntity = (SortableClientEntity) iterator.next();
                if(Objects.isNull(currentClientEntity)) break;
            }
            currentClientEntity.getAcceptList().add(new SortableVideoEntity(
                    FilenameConvertor.toStringName(content),
                    new Double(0),
                    true,
                    null,
                    null
            ));
        }
        return clientEntityMap;
    }

    /**
     * When all clients have reached their capacity limits or all content has been cached
     * The allocating process is finished.
     *
     * @param clients
     * @return
     */
    public static boolean isAllocationFinished(Map<String, SortableClientEntity> clients,
                                               CacheService cacheService) {
        boolean result = false;
        int accepted = 0;
        for (SortableClientEntity sortableClientEntity : clients.values()) {
            result &= sortableClientEntity.isFull();
            accepted += sortableClientEntity.getAcceptList().size();
        }
        return result || (accepted >= cacheService.RESOURCE_AMOUNT);
    }

    public static double calculateExpectedDelay(CacheService cacheService) {
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final Map<String, List<String>> resourceMap = webClientMap.get("1").getResourceMap();
        final double[][] delayMap = cacheService.delayMap;
        double serviceDelay = 0.0;
        long count = 0;
        for (WebClient webClient : webClientMap.values()) {
            final Map<String, Integer> counters = webClient.getCounters();
            for (String contentName : counters.keySet()) {
                final int requestNum = counters.get(contentName);
                count += requestNum;
                final List<String> locationSet = resourceMap.get(contentName);
                if (Objects.isNull(locationSet) || locationSet.isEmpty()) {
                    serviceDelay += (cacheService.MISS_DELAY * count);
                } else {
                    serviceDelay += (delayMap[Integer.valueOf(webClient.getId())][Integer.valueOf(locationSet.get(0))]
                            + cacheService.BASE_SERVICE_DELAY) * count;
                }
            }
        }
        return serviceDelay / count;
    }
}
