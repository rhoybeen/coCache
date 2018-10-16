package bupt.wspn.cache.controller;

import bupt.wspn.cache.model.ResponseEntity;
import bupt.wspn.cache.service.CacheService;
import bupt.wspn.cache.service.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Slf4j
@Controller
@RequestMapping(value = "/simu")
public class SimulatorController {
    @Autowired
    private CacheService cacheService;

    @ResponseBody
    @RequestMapping(value = "/client/add/{parentId}")
    public String simuWebClient(@PathVariable final String parentId) {
        log.info("Create child node for parent id:" + parentId);
        final WebClient webClient = cacheService.simuWebClient(parentId);
        if (Objects.isNull(webClient)) {
            final ResponseEntity responseEntity = ResponseEntity
                    .retryableFailEntity("Failed to create node with parentID" + parentId);
            return responseEntity.toJSONString();
        } else {
            final ResponseEntity responseEntity = ResponseEntity
                    .successEntityWithPayload(webClient);
            return responseEntity.toJSONString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/client/del/{nodeId}")
    public String removeWebClientById(@PathVariable String nodeId) {
        log.info("Delete node by id:" + nodeId);
        cacheService.delWebClient(nodeId);
        return ResponseEntity.successEntityWithPayload("Successfully deleted node by id " + nodeId).toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/request/{nodeId}/{videoId}")
    public String requestVideo(@PathVariable final String nodeId, @PathVariable final String videoId) {
        log.info("Request video:" + videoId + " from cdn node " + nodeId);
        return cacheService.simuRequest(nodeId,videoId) ?
                ResponseEntity.successEntityWithPayload("Success.").toJSONString() :
                ResponseEntity.retryableFailEntity("Request failed.").toJSONString();
    }
}