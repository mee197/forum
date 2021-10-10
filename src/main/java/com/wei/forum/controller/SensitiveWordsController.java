package com.wei.forum.controller;

import com.wei.forum.service.SensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description Created by weilei on 16:14 2019/4/20
 **/
@Controller
public class SensitiveWordsController {
    @Autowired
    SensitiveService sensitiveService;

    @RequestMapping(path = {"/SensitiveWords"},method = {RequestMethod.POST,RequestMethod.GET})
    public String SensitiveWords(Model model){
        model.addAttribute("SensitiveWords",sensitiveService.getSensitiveWords());
        return "SensitiveWords";
    }

    @RequestMapping(path = {"/addSensitiveWords"},method = {RequestMethod.POST})
    public String addSensitiveWords(Model model,
                                    @RequestParam("words") String word){
        SensitiveService.addSensitiveWords(word);
        model.addAttribute("SensitiveWords",sensitiveService.getSensitiveWords());
        return "SensitiveWords";
    }

    @RequestMapping(path = {"/delSensitiveWords"},method = {RequestMethod.POST})
    public String delSensitiveWords(Model model,
                                    @RequestParam("words") String word){
        SensitiveService.delSensitiveWords(word);
        model.addAttribute("SensitiveWords",sensitiveService.getSensitiveWords());
        return "SensitiveWords";
    }


}
