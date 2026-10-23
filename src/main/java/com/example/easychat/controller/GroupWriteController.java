package com.example.easychat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.easychat.common.Result;
import com.example.easychat.model.Group;
import com.example.easychat.model.GroupMember;
import com.example.easychat.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 群组修改类 Controller（M6b）：改群信息 + 群模糊查询。
 */
@RestController
public class GroupWriteController {

    private final GroupService groupService;

    public GroupWriteController(GroupService groupService) {
        this.groupService = groupService;
    }

    /** POST /group/update 修改群信息（仅群成员，对齐原版权限语义） */
    @PostMapping("/group/update")
    public Result<Group> updateGroup(@RequestParam String groupId,
                                     @RequestParam(required = false) String groupName,
                                     HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (!groupService.isMember(groupId, userId)) {
            return Result.error(com.example.easychat.common.ResultCode.NOT_MEMBER);
        }
        Group group = groupService.getById(groupId);
        if (groupName != null && !groupName.trim().isEmpty()) {
            group.setGroupName(groupName);
            groupService.updateGroup(group);
        }
        return Result.success(group);
    }

    /**
     * GET /fuzzy/query/groups 群模糊查询。
     * 【对拍看点】原版这个接口是坏的（BUG-001：Mapper SQL 列名 groupName≠group_name，
     * 我们测试原版时抓到）。复刻版开启了 map-underscore-to-camel-case 且 SQL 用列名，
     * 同一个功能在这里正常工作——复刻不是照搬 bug，是修复后兼容。
     */
    @GetMapping("/fuzzy/query/groups")
    public Result<List<Group>> fuzzyQueryGroups(@RequestParam String groupName) {
        List<Group> groups = groupService.list(new LambdaQueryWrapper<Group>()
                .like(Group::getGroupName, groupName));
        return Result.success(groups);
    }
}
