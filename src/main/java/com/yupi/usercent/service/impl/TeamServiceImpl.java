package com.yupi.usercent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercent.common.ErrorCode;
import com.yupi.usercent.exception.BussinessException;
import com.yupi.usercent.model.VO.TeamUserVO;
import com.yupi.usercent.model.VO.UserVO;
import com.yupi.usercent.model.domain.Team;
import com.yupi.usercent.mapper.TeamMapper;
import com.yupi.usercent.model.domain.User;
import com.yupi.usercent.model.domain.UserTeam;
import com.yupi.usercent.model.dto.TeamQuery;
import com.yupi.usercent.model.enums.TeamStatusEnum;
import com.yupi.usercent.model.request.TeamJoinRequest;
import com.yupi.usercent.model.request.TeamQuitRequest;
import com.yupi.usercent.model.request.TeamUpdateRequest;
import com.yupi.usercent.service.TeamService;
import com.yupi.usercent.service.UserService;
import com.yupi.usercent.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author lintai
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-07-12 21:57:12
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1.请求参数是否为空？
        if(team == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BussinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        //3.校验信息
            //1.队伍人数>1且<=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum > 20){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
            //2.队伍标题不为空格且<=20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() > 20){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
            //3.描述<=512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
            //4.status是否公开(int)不传默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if(statusEnum == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
            //5.如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if(TeamStatusEnum.PRIVATE.equals(statusEnum)){
            if(StringUtils.isBlank(password) || password.length() > 32){
                throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
            //6.超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "过期时间设置不正确");
        }
            //7.校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        int count = this.count(queryWrapper);
        if(count >= 5){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4.插入队伍信息到队伍表
        team.setId(null);//设置为空，让数据库自动生成
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        //5.插入用户=>队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(teamId);
        userTeam.setTeamId(0L);
        userTeam.setJoinTime(new Date());

        result = userTeamService.save(userTeam);
        if(!result){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                queryWrapper.eq("id", id);
            }

            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                //name或者description有一个满足条件就能查出来
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            //查询最大人数相等的
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建人来查询
            if(userId!=null && userId > 0){
                queryWrapper.eq("userId", userId);
            }
            //根据状态来查询，只有管理员才能查看加密还有非公开的房间
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(status);
            if(statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            //非管理员，且队伍非公开，抛出异常
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BussinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());

        }
        //不展示已过期队伍
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw->qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        // 关联查询用户信息
        //1、自己写sql
        //查询队伍和创建人信息
        //select * from team t left join user u on t.userid = u.id;
        //查询队伍和已加入队伍成员信息
        //select *
        //from team t
        //          left join user_team ut on t.id = ut.teamId
        //          left join

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            //判断一下
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                //放入创建人信息
                teamUserVO.setCreatUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateById(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        if((oldTeam.getUserId() != loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET) && StringUtils.isBlank(teamUpdateRequest.getPassword())){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "加密房间必须配置密码");
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        //team必须存在且不为空
        if(teamId == null || teamId <= 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询得到team
        Team team = this.getById(teamId);
        if(team == null){
            throw new BussinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //队伍未过期
        Date expireTime = team.getExpireTime();
        if(team.getExpireTime() != null && expireTime.before(new Date())){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        //禁止加入私有的队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isNotBlank(password) || !password.equals(team.getPassword())){
                throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //创建或已加入不超过5个
        long userId = loginUser.getId();
        //只有一个线程能取到锁
        RLock lock = redissonClient.getLock("yupao:join_team");
        try {
            //抢到锁并执行
            while(true){
                //保证所有线程都抢到锁
                if(lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)){
                    System.out.println("getLock:" + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    int haiJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if(haiJoinNum > 5){
                        throw new BussinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
                    }
                    //不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if(hasUserJoinTeam > 0){
                        throw new BussinessException(ErrorCode.NULL_ERROR, "用户已加入该队伍");
                    }

                    //已加入队伍的人数
                    long teamHasJoinNum = this.countTeanUserByTeamId(teamId);
                    if(teamHasJoinNum >= team.getMaxNum()){
                        throw new BussinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecomendUserError", e);
            return false;
        }finally {
            // 只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                System.out.println("unLock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
        //加单机锁，再加分布式锁
//        synchronized (String.valueOf(userId).intern()){}
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验队伍编号
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        //是否为已加入队伍
        if(count == 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        //已加入队伍的人数
        long teamHasJoinNum = this.countTeanUserByTeamId(teamId);
        //队伍只剩一人，解散
        //删除队伍和加入队伍关系
        if(teamHasJoinNum == 1){
            this.removeById(teamId);
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
        }else{
            //还有其他人（至少还有两个人）
            //判断是不是队长
            if(team.getUserId() == userId){
                //把队伍转移给最早加入的用户
                //1、查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                //至少有两条数据
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BussinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新队伍表，更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BussinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
                //更新用户队伍表，移除一条用户关联
                //return userTeamService.remove(queryWrapper);
            }
        }
        //队长或非队长，更新用户队伍表，移除一条用户关联
        return userTeamService.remove(queryWrapper);
    }

    private Team getTeamById(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询队伍是否存在
        Team team = this.getById(teamId);
        if(team == null){
            throw new BussinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        //1校验请求参数
        if(id <= 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //2校验队伍是否存在
        Team team = getTeamById(id);
        Long teamId = team.getId();
        //3校验你是不是队伍的队长
        if(team.getUserId() != loginUser.getId()){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        //4移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        //5删除队伍
        return this.removeById(teamId);
    }

    /**
     * 根据队伍id查询人数
     * @param teamId
     * @return
     */
    private long countTeanUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

}




