package com.example.easychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.easychat.model.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
