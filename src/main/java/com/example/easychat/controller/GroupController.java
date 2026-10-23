package com.example.easychat.controller;

import com.example.easychat.common.Result;
import com.example.easychat.model.Group;
import com.example.easychat.model.GroupMember;
import com.example.easychat.mapper.GroupMemberMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.easychat.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 群组查询 Controller（M6a）——只读。
 *
 * 写操作在 GroupWriteController（M6b：/group/update、/fuzzy/query/groups）。
 *
 * /groups 实现说明：原版是查“我加入的群”。这里先查我的 groupId 集合，
 * 再批量查群信息（生产环境应一条 JOIN 搞定，分步是为了分层清晰）。
 */
@RestController
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberMapper groupMemberMapper;

    public GroupController(GroupService groupService, GroupMemberMapper groupMemberMapper) {
        this.groupService = groupService;
        this.groupMemberMapper = groupMemberMapper;
    }

    /** GET /groups 我的群列表 */
    @GetMapping("/groups")
    public Result<List<Group>> groups(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<String> groupIds = groupMemberMapper.selectList(new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getUserId, userId))
                .stream().map(GroupMember::getGroupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            return Result.success(List.of());
        }
        return Result.success(groupService.listByIds(groupIds));
    }

    /** GET /group/{groupId} 群信息 */
    @GetMapping("/group/{groupId}")
    public Result<Group> group(@PathVariable String groupId) {
        return Result.success(groupService.getById(groupId));
    }

    /** GET /query/member/{groupId} 群成员 userId 列表（原版源码是 GET，以源码为准） */
    @GetMapping("/query/member/{groupId}")
    public Result<List<String>> members(@PathVariable String groupId) {
        return Result.success(groupService.memberIds(groupId));
    }
}
