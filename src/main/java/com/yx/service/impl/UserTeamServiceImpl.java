package com.yx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yx.model.domain.UserTeam;
import com.yx.service.UserTeamService;
import com.yx.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* 用户-队伍服务实现类
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




