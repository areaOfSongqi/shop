package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model)
    {
        //准备数据模型
        Map<String, Object> attributes=pageService.loadModel(spuId);
        model.addAllAttributes(attributes);



        return "item";
    }
}
