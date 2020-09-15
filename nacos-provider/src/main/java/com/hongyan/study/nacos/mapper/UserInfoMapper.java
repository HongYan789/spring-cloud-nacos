package com.hongyan.study.nacos.mapper;

import com.hongyan.study.nacos.bean.UserInfo;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface UserInfoMapper {

    @Select({" select user_id as userId,user_name as userName,account,password from user_info_0 order by user_id  "})
    List<UserInfo> queryAll();
}
