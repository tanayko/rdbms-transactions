package com.lseg.acadia.skills.rdbmstx.mappers;

import com.lseg.acadia.skills.rdbmstx.models.People;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PeopleMapper {
    List<People> selectAll();
    People selectById(long id);
    void insert(People people);
    void update(People people);
    void delete(People people);
}
