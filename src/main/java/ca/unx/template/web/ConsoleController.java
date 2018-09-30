package ca.unx.template.web;

import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping(value = "/console")
public class ConsoleController {
    private static final String VIEW = "console";

    @RequestMapping
    public ModelAndView getViewPage(Model model){
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(VIEW);
        return modelAndView;
    }
}
