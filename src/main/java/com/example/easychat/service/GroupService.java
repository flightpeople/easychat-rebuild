package com.example.easychat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.easychat.mapper.GroupMapper;
import com.example.easychat.mapper.GroupMemberMapper;
import com.example.easychat.model.Group;
import com.example.easychat.model.GroupMember;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 群组服务（M5a）：建群、拉人、成员管理。
 */
@Service
public class GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;

    public GroupService(GroupMapper groupMapper, GroupMemberMapper groupMemberMapper) {
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    /** 建群：群主自动入群 */
    public Group createGroup(String groupName, String ownerId) {
        String groupId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
        Group group = new Group();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        group.setMainUserId(ownerId);
        groupMapper.insert(group);
        groupMemberMapper.insert(new GroupMember(groupId, ownerId));
        System.out.println("[GroupService] 建群: groupId=" + groupId + ", 群主=" + ownerId);
        return group;
    }

    public Group getById(String groupId) {
        return groupMapper.selectById(groupId);
    }

    /** 改群名/头像（updateById 的选择更新） */
    public void update(String groupId, String groupName, String avatar) {
        Group g = new Group();
        g.setGroupId(groupId);
        g.setGroupName(groupName);
        g.setAvatar(avatar);
        groupMapper.updateById(g);
    }

    /** 批量查群信息（REST /groups 用） */
    public List<Group> listByIds(List<String> groupIds) {
        return groupMapper.selectBatchIds(groupIds);
    }

    /** 更新群信息（M6b） */
    public void updateGroup(Group group) {
        groupMapper.updateById(group);
    }

    /** 群模糊查询（M6b，修复原版 BUG-001 的功能） */
    public List<Group> list(LambdaQueryWrapper<Group> wrapper) {
        return groupMapper.selectList(wrapper);
    }

    public boolean isMember(String groupId, String userId) {
        return groupMemberMapper.exists(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId));
    }

    public void addMember(String groupId, String userId) {
        if (!isMember(groupId, userId)) {
            groupMemberMapper.insert(new GroupMember(groupId, userId));
        }
    }

    public List<String> memberIds(String groupId) {
        return groupMemberMapper.selectMemberIds(groupId);
    }
}
