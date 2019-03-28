package com.scj.beilu.manydatasource.service;

import com.scj.beilu.manydatasource.commons.TargetDataSource;
import com.scj.beilu.manydatasource.mapper.ManyMapper;
import com.scj.beilu.manydatasource.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description TODO
 * @Author shen
 * @Date 2019/3/27 15:54
 **/
@Service
public class ManyService {

    @Autowired
    ManyMapper manyMapper;

    @TargetDataSource
    public List<Student> getStudent() {
        return manyMapper.getStudent();
    }

    @TargetDataSource(name = "slave")
    public int insertStudent(String name) {
        return manyMapper.insertStudent(name);
    }
}
