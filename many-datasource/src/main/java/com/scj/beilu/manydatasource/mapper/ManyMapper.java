package com.scj.beilu.manydatasource.mapper;

import com.scj.beilu.manydatasource.model.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ManyMapper {


    List<Student> getStudent();

    int insertStudent(String name);
}
