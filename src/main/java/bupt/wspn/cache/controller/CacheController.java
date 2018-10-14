package bupt.wspn.cache.controller;

import bupt.wspn.cache.service.CacheService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping(value = "/cache")
public class CacheController {
    @Autowired
    private CacheService cacheService;

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
}
