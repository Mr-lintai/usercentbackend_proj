package com.yupi.usercent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercent.model.domain.UserTeam;
import com.yupi.usercent.mapper.UserTeamMapper;
import com.yupi.usercent.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author lintai
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-07-12 22:53:58
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




