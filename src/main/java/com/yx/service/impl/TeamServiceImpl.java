package com.yx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yx.common.ErrorCode;
import com.yx.model.domain.Team;
import com.yx.model.domain.User;
import com.yx.model.domain.UserTeam;
import com.yx.exception.BusinessException;
import com.yx.model.enums.TeamStatusEnum;
import com.yx.model.requestDto.*;
import com.yx.model.vo.TeamUserVO;
import com.yx.model.vo.UserVO;
import com.yx.service.TeamService;
import com.yx.mapper.TeamMapper;
import com.yx.service.UserService;
import com.yx.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 队伍服务实现类
 *
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Resource
    UserTeamService userTeamService;

    @Resource
    UserService userService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return  队伍Id
     */
    @Override
    @Transactional
    public Long addTeam(Team team, User loginUser) {
        //判断请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //检验队伍人数：1-20
        Integer maxNum = team.getMaxNum();
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //队伍标题<=20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题过长");
        }
        //队伍描述<=512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //判断队伍状态是否正确，默认为0
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态参数有误");
        }
        //如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //队伍超时时间大于当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间>当前时间");
        }
        //校验一个用户最多只能创建5个队伍
        //todo 有bug，可能同时创建多个队伍
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(Team::getUserId, loginUser.getId());
        long count = this.count(lambdaQueryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户创建的队伍数量大于5");
        }
        //检验完毕，可以插入队伍信息到队伍表
        team.setUserId(loginUser.getId());
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        //在用户队伍关系表中插入数据
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUser.getId());
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 查询队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long loginUserId = userService.getLoginUser(request).getId();

        //添加查询条件
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper();
        List<Long> idList = teamQuery.getIdList();
        if (!CollectionUtils.isEmpty(idList)) {
            lambdaQueryWrapper.in(Team::getId, idList);
        }
        String searchText = teamQuery.getSearchText();
        //在名字和描述中对搜索内容进行模糊匹配
        if (StringUtils.isNotBlank(searchText)) {
            lambdaQueryWrapper.like(Team::getName, searchText).or(lam -> lam.like(Team::getDescription, searchText));
        }
        //判断队伍状态
        Integer status = teamQuery.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            statusEnum = TeamStatusEnum.PUBLIC;
        }
        lambdaQueryWrapper.eq(Team::getStatus, statusEnum.getValue());
        //根据创建人来查询
        Long createUserId = teamQuery.getUserId();
        if (createUserId != null && createUserId > 0) {
            lambdaQueryWrapper.eq(Team::getUserId, createUserId);
        }
        //不展示已经过期的队伍
        lambdaQueryWrapper.gt(Team::getExpireTime, new Date());
        List<Team> teamList = this.list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<Long> teamIdList = teamList.stream().map(Team::getId).collect(Collectors.toList());

        //查询当前用户所参加的队伍,用于关联队伍的hasJoinTeam参数
        LambdaQueryWrapper<UserTeam> userTeamQuery = new LambdaQueryWrapper<UserTeam>();
        userTeamQuery.eq(UserTeam::getUserId, loginUserId).in(UserTeam::getTeamId, teamIdList).select(UserTeam::getTeamId);
        List<Long> loginUserTeamIds = userTeamService.list(userTeamQuery).stream().map(UserTeam::getTeamId).collect(Collectors.toList());

        // 查询每个队伍的人数
        LambdaQueryWrapper<UserTeam> teamCountQuery = new LambdaQueryWrapper<>();
        teamCountQuery.in(UserTeam::getTeamId, teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(teamCountQuery);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                // 关联队伍创建人的用户信息
                teamUserVO.setCreateUser(userVO);
            }

            boolean hasJoin = false;
            if (loginUserTeamIds.contains(team.getId())) {
                hasJoin = true;
            }
            //关联当前队伍是否有当前用户
            teamUserVO.setHasJoin(hasJoin);
            //关联每个队伍含有的的人数
            teamUserVO.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //权限验证，只有管理员和队伍的创建者可以修改
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword()) || teamUpdateRequest.getPassword().length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不符合要求");
            }
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);
        boolean b = this.updateById(team);
        return b;
    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = this.getById(teamId);
        //判断加入的队伍是否过期
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍已过期");
        }
        //判断是否是私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        long userId = loginUser.getId();
        RLock lock = redissonClient.getLock("yupao:team:joinTeam");

        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    //每个人最多参加5个队伍
                    LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper();
                    wrapper.eq(UserTeam::getUserId,userId);
                    long hasJoinNum = userTeamService.count(wrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");

                    }
                    //不能重复入队
                    wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(UserTeam::getUserId,userId).eq(UserTeam::getTeamId,teamId);
                    long hasUserJoinTeam = userTeamService.count(wrapper);
                    if (hasUserJoinTeam>0){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
                    }
                    //队伍是否已满
                    long teamUserNums = this.countTeamUserByTeamId(teamId);
                    if (teamUserNums>=team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
                    }
                    UserTeam userTeam = new UserTeam();
                    userTeam.setTeamId(teamId);
                    userTeam.setUserId(userId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("The lock 'yupao:team:joinTeam' had a error ",e);
            return false;
        } finally {
            //释放锁,只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unLock"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    @Transactional
    public boolean deleteTeam(long id, User loginUser) {
        //检验队伍是否存在
        Team team = getTeamById(id);
        //检验是不是队伍的队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //移除所有加入队伍的关联信息
        boolean remove = userTeamService.remove(new LambdaQueryWrapper<UserTeam>().eq(UserTeam::getTeamId, id));
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        //删除队伍
        return this.removeById(id);
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }


    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        //检验用户是否有加入队伍以及是否存在这个队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        //查看队伍的人数
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 队伍还剩至少两人
            // 是队长
            if (team.getUserId() == userId) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(queryWrapper);
    }
}




