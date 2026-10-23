package com.example.easychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.easychat.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（M3）。
 *
 * 继承 MyBatis-Plus 的 BaseMapper<User> 就白嫖了 CRUD：
 * insert / selectById / selectOne / updateById ... 开箱即用，
 * 这是 MyBatis-Plus 相比原生 MyBatis 的核心价值（少写 80% 的样板 SQL）。
 *
 * 复杂查询（M4 的好友联表、M5 的消息分页）再用自定义 XML 或 Lambda 查询包装。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
