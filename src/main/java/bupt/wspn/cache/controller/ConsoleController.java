package bupt.wspn.cache.controller;

import bupt.wspn.cache.Utils.PropertyUtil;
import bupt.wspn.cache.model.ResponseEntity;
import bupt.wspn.cache.service.CacheService;
import bupt.wspn.cache.service.WebClient;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.chart.ValueAxis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;
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

    @ResponseBody
    @RequestMapping(value = "/info")
    public String getInfo(){
        log.info("Request cache service nodes info.");
        return ResponseEntity.successEntityWithPayload(cacheService).toJSONString();
    }

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

    @ResponseBody
    @RequestMapping(value = "/unbind")
    public String unbind(@RequestBody String params, HttpServletRequest request){
        final String uri = request.getRemoteHost();
        final String clientId = params;
        log.info("Unbind client " + uri);
        final boolean res = cacheService.unBindWebClient(clientId);
        return String.valueOf(res);
    }

    @ResponseBody
    @RequestMapping(value = "/nodes")
    public String retrieveCoCacheNodes(){
        log.info("Retrieve cocache system nodes.");
        final Set<WebClient> nodes = cacheService.retrieveCocacheNodes();
        final ResponseEntity responseEntity = ResponseEntity.successEntityWithPayload(nodes);
        return responseEntity.toJSONString();
    }

}
