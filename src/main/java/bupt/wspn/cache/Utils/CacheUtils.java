package bupt.wspn.cache.Utils;

import bupt.wspn.cache.model.SortableClientEntity;
import bupt.wspn.cache.model.SortableVideoEntity;
import bupt.wspn.cache.model.Video;
import bupt.wspn.cache.service.CacheService;
import bupt.wspn.cache.service.WebClient;
import com.google.common.graph.MutableValueGraph;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
public class CacheUtils {

    public static final String CACHE_STRATEGY_GS_ALGORITHM = "GS";

    public static final String CACHE_STRATEGY_RANDOM_COOPERATE = "RAN_CO";

    //every content may keep more than one copies in the cooperating system
    public static final String CACHE_STRATEGY_POPULARITY_NON_COOPERATE = "POP_NON_CO";

    //every content only keep at most one copy in the system
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
            case CACHE_STRATEGY_RANDOM_COOPERATE:
                return randomCooperateStrategy(cacheService);
            case CACHE_STRATEGY_POPULARITY_NON_COOPERATE:
                return popularityNonCooperateStrategy(cacheService);
            case CACHE_STRATEGY_POPULARITY_COOPERATE:
                return popularityCooperateStrategy(cacheService);
            default:
                return new HashMap<String, SortableClientEntity>();
        }
    }

    //todo: make sure whether need to call updateVideoList() first.
    //todo: the preference list sort order from high to low score.
    public static Map<String, SortableClientEntity> GSAlgorithm(final CacheService cacheService) {
        log.info("Start to update cache by GS strategy.");
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
        log.info("Generating preference list for every client.");
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
        log.info("Generating preference list for every content");
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
                final SortableClientEntity tmpClient = new SortableClientEntity(
                        currentId,
                        current.getCapacity(),
                        preferenceScore,
                        null,
                        null
                );
                videoEntity.getPreferenceMap().put(currentId, tmpClient);
                videoEntity.getPreferenceList().add(tmpClient);
            }
            Collections.sort(videoEntity.getPreferenceList());
        }

//        for (SortableClientEntity sortableClientEntity : clientEntityMap.values()) {
//            log.info(sortableClientEntity.toString());
//        }
//
//        for (SortableVideoEntity sortableVideoEntity : videoEntityMap.values()) {
//            log.info(sortableVideoEntity.toString());
//        }

        log.info("Allocating content.");
        while (!isAllocationFinished(clientEntityMap, cacheService)) {
            for (SortableVideoEntity videoEntity : videoEntityMap.values()) {
                if (videoEntity.isAccepted()) continue;
                final List<SortableClientEntity> videoPreferenceList = videoEntity.getPreferenceList();
                //log.info(videoPreferenceList.size() + " ");
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
        for (SortableClientEntity sortableClientEntity : clientEntityMap.values()) {
            log.info(sortableClientEntity.getAcceptListString());
        }
        log.info("Allocation finished.");
        return clientEntityMap;
    }

    /**
     * Update cache by random strategy.
     *
     * @param cacheService
     * @return
     */
    public static Map<String, SortableClientEntity> randomCooperateStrategy(final CacheService cacheService) {
        log.info("Start to update cache by randomCooperateStrategy.");
        final MutableValueGraph graph = cacheService.getGraph();
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final Set<WebClient> webClients = graph.nodes();
        final Map<String, SortableClientEntity> clientEntityMap = new HashMap<>();

        int resourceNum = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
        Set<Integer> tmpSet = new HashSet<>();
        Random random = new Random();
        for (WebClient webClient : webClients) {
            final SortableClientEntity currentClient = new SortableClientEntity(
                    webClient.getId(),
                    webClient.getCapacity(),
                    new Double(0),
                    new HashMap<>(),
                    new ArrayList<>()
            );
            clientEntityMap.put(webClient.getId(), currentClient);
            final List<SortableVideoEntity> acceptList = currentClient.getAcceptList();
            final int capacity = currentClient.getCapacity();
            while (acceptList.size() < capacity && tmpSet.size() < resourceNum) {
                int candidate = random.nextInt(resourceNum) + 1;
                while (tmpSet.contains(candidate)) candidate = random.nextInt(resourceNum) + 1;
                acceptList.add(new SortableVideoEntity(
                        FilenameConvertor.toStringName(candidate),
                        new Double(0),
                        true,
                        null,
                        null
                ));
                tmpSet.add(candidate);
            }
        }
        log.info("Finish updating cache by randomCooperateStrategy.");
        return clientEntityMap;
    }

    public static Map<String, SortableClientEntity> popularityNonCooperateStrategy(final CacheService cacheService) {
        log.info("Start to update cache by popularityNonCooperateStrategy.");
        final MutableValueGraph graph = cacheService.getGraph();
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final Set<WebClient> webClients = graph.nodes();
        final Map<String, SortableClientEntity> clientEntityMap = new HashMap<>();

        for (WebClient webClient : webClientMap.values()) {
            final SortableClientEntity currentClient = new SortableClientEntity(
                    webClient.getId(),
                    webClient.getCapacity(),
                    new Double(0),
                    new HashMap<>(),
                    new ArrayList<>()
            );
            final List<SortableVideoEntity> acceptList = currentClient.getAcceptList();
            final int capacity = webClient.getCapacity();
            webClient.updateVideoList();
            final List<Video> sortedList = webClient.getResources();
            for (int i = 0; i < capacity; i++) {
                final Video currentVideo = sortedList.get(i);
                acceptList.add(new SortableVideoEntity(
                        currentVideo.getName(),
                        new Double(currentVideo.getClickNum()),
                        false,
                        null,
                        null
                ));
            }
            clientEntityMap.put(webClient.getId(), currentClient);
        }
        log.info("Finish updating cache by popularityNonCooperateStrategy.");
        return clientEntityMap;
    }

    public static Map<String, SortableClientEntity> popularityCooperateStrategy(final CacheService cacheService) {
        log.info("Start to update cache by popularityCooperateStrategy.");
        final MutableValueGraph graph = cacheService.getGraph();
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final Map<String, SortableClientEntity> clientEntityMap = new HashMap<>();

        final Set<String> tmpSet = new HashSet<>();
        for (final WebClient webClient : webClientMap.values()) {
            webClient.updateVideoList();
            final SortableClientEntity currentClientEntity = new SortableClientEntity(
                    webClient.getId(),
                    webClient.getCapacity(),
                    new Double(0),
                    new HashMap<>(),
                    new ArrayList<>()
            );
            final List<Video> sortedList = webClient.getResources();
            final List<SortableVideoEntity> acceptList = currentClientEntity.getAcceptList();
            final int capacity = webClient.getCapacity();
            int startIndex = 0;
            log.info("sortedList size" + sortedList.size() + "capacity:" + capacity);
            while (acceptList.size() < capacity && startIndex < sortedList.size()) {
                Video currentVideo = sortedList.get(startIndex);
                while (tmpSet.contains(currentVideo.getName()) && startIndex < sortedList.size()) {
                    startIndex++;
                    currentVideo = sortedList.get(startIndex);
                }
                acceptList.add(new SortableVideoEntity(
                        currentVideo.getName(),
                        new Double(currentVideo.getClickNum()),
                        true,
                        null,
                        null
                ));
                startIndex++;
                tmpSet.add(currentVideo.getName());
            }
            clientEntityMap.put(webClient.getId(), currentClientEntity);
        }
        for (SortableClientEntity sortableClientEntity : clientEntityMap.values()) {
            log.info(sortableClientEntity.toString());
        }

        log.info("Finish updating cache by popularityCooperateStrategy.");
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
        boolean result = true;
        int accepted = 0;
        for (SortableClientEntity sortableClientEntity : clients.values()) {
            result &= sortableClientEntity.isFull();
            accepted += sortableClientEntity.getAcceptList().size();
        }
        //    log.info(String.valueOf(result) + "  " + String.valueOf(accepted));
        return result || (accepted >= cacheService.RESOURCE_AMOUNT);
    }

    public static Map<String, Double> calculateExpectedStrategyDelay(final CacheService cacheService) {
        final Map<String, Double> expectedDelayMap = new HashMap<>();

        final Map<String, List<String>> resourceMap_gs = generateResourceMap(cacheService, GSAlgorithm(cacheService));
        final double delay_ds = calculateExpectedDelay(cacheService, resourceMap_gs);
        expectedDelayMap.put(CACHE_STRATEGY_GS_ALGORITHM, delay_ds);

        final Map<String, List<String>> resourceMap_random = generateResourceMap(cacheService, randomCooperateStrategy(cacheService));
        final double delay_random = calculateExpectedDelay(cacheService, resourceMap_random);
        expectedDelayMap.put(CACHE_STRATEGY_RANDOM_COOPERATE, delay_random);

        final Map<String, List<String>> resourceMap_pop_non = generateResourceMap(cacheService, popularityNonCooperateStrategy(cacheService));
        final double delay_pop_non = calculateExpectedDelay(cacheService, resourceMap_pop_non);
        expectedDelayMap.put(CACHE_STRATEGY_POPULARITY_NON_COOPERATE, delay_pop_non);

        final Map<String, List<String>> resourceMap_pop_co = generateResourceMap(cacheService, popularityCooperateStrategy(cacheService));
        final double delay_pop_co = calculateExpectedDelay(cacheService, resourceMap_pop_co);
        expectedDelayMap.put(CACHE_STRATEGY_POPULARITY_COOPERATE, delay_pop_co);

        return expectedDelayMap;
    }

    public static Map<String, List<String>> generateResourceMap(CacheService cacheService, Map<String, SortableClientEntity> clientEntityMap) {
        final Map<String, List<String>> resourceMap = new HashMap<>();
        for (int i = 1; i <= cacheService.RESOURCE_AMOUNT; i++) {
            resourceMap.put(FilenameConvertor.toStringName(i), new ArrayList<>());
        }
        for (SortableClientEntity clientEntity : clientEntityMap.values()) {
            final String id = clientEntity.getClientId();
            for (SortableVideoEntity videoEntity : clientEntity.getAcceptList()) {
                List<String> locations = resourceMap.get(videoEntity.getName());
                locations.add(id);
            }
        }
        return resourceMap;
    }

    public static double calculateExpectedDelay(CacheService cacheService, Map<String, List<String>> resourceMap) {
        final Map<String, WebClient> webClientMap = cacheService.getWebClientMap();
        final double[][] delayMap = cacheService.delayMap;
        double serviceDelay = 0.0;
        long count = 0;
        long missCount = 0;
        for (WebClient webClient : webClientMap.values()) {
            final Map<String, Integer> counters = webClient.getCounters();
            for (String contentName : counters.keySet()) {
                final int requestNum = counters.get(contentName);
                count += requestNum;
                final List<String> locationSet = resourceMap.get(contentName);
                if (Objects.isNull(locationSet) || locationSet.isEmpty()) {
                    serviceDelay += (cacheService.MISS_DELAY * requestNum);
                    missCount += requestNum;
                } else {
                    int currentIndex = Integer.valueOf(webClient.getId());
                    int cachedIndex = Integer.valueOf(locationSet.get(0));
                    if (locationSet.size() > 1) {
                        for (String s : locationSet) {
                            if (Integer.valueOf(s) == currentIndex) cachedIndex = currentIndex;
                        }
                    }
                    serviceDelay += (delayMap[currentIndex][cachedIndex]
                            + cacheService.BASE_SERVICE_DELAY) * requestNum;
                }
            }
        }
        log.info("Total service delay:" + serviceDelay + " total/miss:" + count + "/" + missCount);
        return serviceDelay / count;
    }
}
