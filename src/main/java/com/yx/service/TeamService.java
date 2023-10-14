package com.yx.service;

import com.yx.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yx.domain.User;
import com.yx.model.requestDto.*;
import com.yx.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lige
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-10-07 19:10:32
*/
public interface TeamService extends IService<Team> {

    Long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);
}
