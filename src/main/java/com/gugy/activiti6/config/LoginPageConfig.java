package com.gugy.activiti6.config;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class LoginPageConfig {

    @RequestMapping("/")
    public RedirectView loginPage() {
        return new RedirectView("views/model-list.html");
    }


    /**
     * @description: TODO activity的model页面
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 16:36
     */
    @GetMapping("editor")
    public String modeler(){
        return "/modeler";
    }
}
