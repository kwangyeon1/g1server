package org.yht.exps.express.mapper.sms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.sms.user.User;

@Mapper
public interface UserMapper {
    User getUser(String userId);
    User getUserByLoginId(String userLoginId);
}