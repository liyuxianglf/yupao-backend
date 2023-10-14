package com.yx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yx.domain.UserTeam;
import com.yx.service.UserTeamService;
import com.yx.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author lige
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-10-07 19:10:32
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




