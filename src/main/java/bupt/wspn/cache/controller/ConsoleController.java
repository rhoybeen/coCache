package bupt.wspn.cache.controller;

import bupt.wspn.cache.Utils.TopoUtils;
import bupt.wspn.cache.model.ResponseEntity;
import bupt.wspn.cache.service.CacheService;
import bupt.wspn.cache.service.WebClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping(value = "/console")
public class ConsoleController {
    @Autowired
    private CacheService cacheService;

    private static final String VIEW = "console";

    @RequestMapping
    public ModelAndView getViewPage(Model model){
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(VIEW);
        return modelAndView;
    }

    /**
     * Get cache service detailed info.
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/info")
    public String getInfo(){
        log.info("Request cache service nodes info.");
        return ResponseEntity.successEntityWithPayload(cacheService).toJSONString();
    }

    /**
     * Bind client to cache service
     * @param params
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bind")
    public String bind(@RequestBody String params, HttpServletRequest request) {
        final String uri = request.getRemoteHost();
        final JSONObject jsonObject = JSONObject.parseObject(params);
        final String webClientStr = jsonObject.getString("params");
        log.info("Bind client " + uri);
        final boolean res = cacheService.bindWebClient(webClientStr);
        return String.valueOf(res);
    }

    /**
     * Sync with web client.
     * @param params
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/sync")
    public String sync(@RequestBody String params, HttpServletRequest request){
        final String uri = request.getRemoteHost();
        final JSONObject jsonObject = JSONObject.parseObject(params);
        final String webClientStr = jsonObject.getString("params");
        log.info("Sync client " + uri);
        final boolean res = cacheService.sync(webClientStr);
        return String.valueOf(res);
    }

    @ResponseBody
    @RequestMapping(value = "/sync/{clientId}")
    public String syncWithClient1(@PathVariable final String clientId){
        log.info("Sync with client 1");
        if(cacheService.syncToWebClient1())
            return ResponseEntity.successEntityWithPayload("Successfully sync with client 1").toJSONString();
        else
            return ResponseEntity.retryableFailEntity("Failed to sync with client 1").toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/unbind")
    public String unbind(@RequestBody String params, HttpServletRequest request){
        final String uri = request.getRemoteHost();
        final String clientId = params;
        log.info("Unbind client " + uri);
        final boolean res = cacheService.unBindWebClient(clientId);
        return String.valueOf(res);
    }

    /**
     * Get all web clients info in cache service.
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/nodes")
    public String retrieveCoCacheNodes(){
        log.info("Retrieve cocache system nodes.");
        final Set<WebClient> nodes = cacheService.retrieveCocacheNodes();
        final ResponseEntity responseEntity = ResponseEntity.successEntityWithPayload(nodes);
        return responseEntity.toJSONString();
    }

    /**
     * Update system delay map.
     * @return
     */
    @ResponseBody
    @RequestMapping( value = "/delays")
    public String updateSystemDelays(){
        log.info("Update system delays.");
        final Map<String, Map<String,Integer>> delayMap = cacheService.retrieveNetworkDelays();
        final ResponseEntity responseEntity = ResponseEntity.successEntityWithPayload(delayMap);
        return responseEntity.toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/cache/update/{strategy}")
    public String updateCache(@PathVariable final String strategy){
        log.info("Update system cache by strategy:" + strategy);
        final Map<String,Object> result = cacheService.updateCache(strategy);
        final ResponseEntity responseEntity = ResponseEntity.successEntityWithPayload(result);
        return responseEntity.toJSONString();
    }

    /**
     * Test function to be removed.
     * @return
     */
    //todo: it should be well returned.
    @ResponseBody
    @RequestMapping( value = "/edge")
    public String test(){
        for(WebClient webClient: cacheService.webClientMap.values()){
            webClient.updateVideoList();
        }
        TopoUtils.getSimuGraphDelays(cacheService.graph,cacheService.delayMap);
        for(int i = 0;i<=15;i++){
            for(int j=0;j<=15;j++){
                System.out.print(String.valueOf((int)cacheService.delayMap[i][j]) + ' ');
            }
            System.out.println();
        }
        return cacheService.delayMap.toString();
    }

}
