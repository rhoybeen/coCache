package ca.unx.template.controller;

import ca.unx.template.service.WebClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * This controller defines operations for a slave node.
 */
@Slf4j
@Controller
@RequestMapping(value = "/slave")
public class SlaveController {
    @Autowired
    private WebClient webClient;

    @ResponseBody
    @RequestMapping(value = "/info")
    public String getSlaveIndo(){
        return JSONObject.toJSONString(webClient);
    }
}
