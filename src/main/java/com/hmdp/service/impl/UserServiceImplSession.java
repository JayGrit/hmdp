package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;


@Service
@ConditionalOnProperty(name="user.service.impl",havingValue = "session")
public class UserServiceImplSession extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if(RegexUtils.isPhoneInvalid(phone))
            return Result.fail("手机号格式不正确");
        String code = RandomUtil.randomNumbers(6);
        System.out.println("验证码:" + code);
        session.setAttribute("code",code);
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        if(RegexUtils.isPhoneInvalid(phone))
            return Result.fail("手机号格式不正确");
        String sessionCode = (String) session.getAttribute("code");

        if( sessionCode==null || !sessionCode.equals(code)){
            return Result.fail("验证码错误");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = this.getOne(queryWrapper);

        if(user == null){
            user = createUser(phone);
        }


        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);

        System.out.println(userDTO);
        UserHolder.saveUser(userDTO);
        session.setAttribute("user",userDTO);
        return Result.ok(userDTO);
    }
}
