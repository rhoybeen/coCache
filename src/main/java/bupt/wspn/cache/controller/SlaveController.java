package bupt.wspn.cache.controller;

import bupt.wspn.cache.model.ResponseEntity;
import bupt.wspn.cache.service.WebClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * This controller defines operations for a slave node.
 */
@Slf4j
@Controller
@RequestMapping(value = "/slave")
public class SlaveController {
    final private static String VIEW = "resource";

    @Autowired
    private WebClient webClient;

    @ResponseBody
    @RequestMapping(value = "/info")
    public String getSlaveInfo(){
        log.info("Get webClient "+ webClient.getId() + " info.");
        return ResponseEntity.successEntityWithPayload(webClient).toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/bind")
    public String bind(){
        log.info("Bind webClient  "+ webClient.getId() + " to master server");
        return webClient.bind();
    }

    @ResponseBody
    @RequestMapping(value = "/unbind")
    public String unbind(){
        log.info("Unbind webClient  "+ webClient.getId() + " from master server");
        return webClient.unbind();
    }

    @RequestMapping("/resource")
    public String getResourcePage(){
        return VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "/resource/list")
    public String getResources(){
        log.info("Request webClient resources");
        final WebClient res = webClient.retrieveDataResources();
        final ResponseEntity responseEntity = ResponseEntity.successEntityWithPayload(res);
        return responseEntity.toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/sync")
    public String syncFromCacheServer(@RequestBody String params, HttpServletRequest request) {
        final JSONObject jsonObject = JSONObject.parseObject(params);
        final String webClientStr = jsonObject.getString("params");
        log.info("Sync from master cache server");
        final boolean res = webClient.sync(webClientStr);
        return String.valueOf(res);
    }
}
