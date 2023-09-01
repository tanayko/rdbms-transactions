package com.lseg.acadia.skills.rdbmstx.mappers;

import com.lseg.acadia.skills.rdbmstx.models.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrdersMapper {
    List<Orders> selectAll();
    Orders selectById(long id);
    void insert(Orders orders);
    void update(Orders orders);
    void delete(Orders orders);
}
