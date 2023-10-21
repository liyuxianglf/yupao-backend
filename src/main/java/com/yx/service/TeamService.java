package com.yx.service;

import com.yx.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yx.model.domain.User;
import com.yx.model.requestDto.*;
import com.yx.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍服务
 *
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return  队伍Id
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     * @param teamQuery
     * @param request
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request);

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);
}
