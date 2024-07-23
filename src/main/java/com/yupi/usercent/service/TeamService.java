package com.yupi.usercent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercent.model.VO.TeamUserVO;
import com.yupi.usercent.model.domain.Team;
import com.yupi.usercent.model.domain.User;
import com.yupi.usercent.model.dto.TeamQuery;
import com.yupi.usercent.model.request.TeamJoinRequest;
import com.yupi.usercent.model.request.TeamQuitRequest;
import com.yupi.usercent.model.request.TeamUpdateRequest;

import java.util.List;

/**
* @author lintai
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-12 21:57:12
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @return
     */
    boolean updateById(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除解散队伍
     * @param id
     * @return
     */
    boolean deleteTeam(long id, User loginUser);
}
