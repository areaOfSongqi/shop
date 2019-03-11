package com.leyou.item.web;


import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.List;

@RestController
@RequestMapping("category")
@MapperScan("com.leyou.item.mapper")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据pid查询商品分类 rest风格
     * @param pid
     * @return
     */

    @GetMapping("/list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid") Long pid)
    {

        //return ResponseEntity.status(HttpStatus.OK).body(null);
        return ResponseEntity.ok(categoryService.queryCategoryByPid(pid));
    }


    /**
     * 根据多个id查询商品分类，用于ly-search调用
     * @param ids
     * @return
     */
    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids)
    {
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }


}
