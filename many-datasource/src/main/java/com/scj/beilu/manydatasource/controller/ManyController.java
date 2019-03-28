package com.scj.beilu.manydatasource.controller;

import com.scj.beilu.manydatasource.commons.TargetDataSource;
import com.scj.beilu.manydatasource.model.Student;
import com.scj.beilu.manydatasource.service.ManyService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author shen
 * @Date 2019/3/27 15:54
 **/
@Controller
public class ManyController {

    @Autowired
    ManyService service;


    @RequestMapping("getStudent")
    @ResponseBody
    @TargetDataSource(name = "slave")
    public List<Student> getStudent(){
        return service.getStudent();
    }

    @RequestMapping("insertStudent")
    @ResponseBody
    public Map<String,Object> insertStudent(@RequestParam("studentName")String name){
        Map<String,Object> map = new HashMap<>();
        int result = service.insertStudent(name);
        if (result==1){
            map.put("msg","插入成功");
        }else {
            map.put("msg","插入失败");
        }
        return map;
    }
}
