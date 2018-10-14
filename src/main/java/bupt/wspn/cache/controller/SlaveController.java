package bupt.wspn.cache.controller;

import bupt.wspn.cache.model.ResponseEntity;
import bupt.wspn.cache.service.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
    public String getSlaveIndo(){
        log.info("Get webClient  "+ webClient.getId() + " info.");
        return webClient.toString();
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
        final WebClient res = webClient.getResources();
        final ResponseEntity responseEntity = ResponseEntity.successEntityWithPayload(res);
        return responseEntity.toJSONString();
    }
}
