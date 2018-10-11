package bupt.wspn.cache.controller;

import bupt.wspn.cache.service.CacheService;
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
    public String bind(@RequestBody String params,HttpServletRequest request){
        final String uri = request.getRemoteHost();
        log.info("Bind client " + uri);
        System.out.println(params);
        return request.getParameter("params");
    }
}
