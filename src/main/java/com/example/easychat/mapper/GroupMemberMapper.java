package com.example.easychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.easychat.model.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    /** 查群成员 userId 列表（群聊广播用：给每个成员推一份） */
    @Select("SELECT user_id FROM sp_user_group WHERE group_id = #{groupId}")
    List<String> selectMemberIds(@Param("groupId") String groupId);
}
