package com.example.mps.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mps.common.ResponseCode;
import com.example.mps.pojo.domain.UserDo;
import com.example.mps.pojo.dto.UserDto;
import com.example.mps.utils.AlgorithmUtils;
import com.example.mps.utils.MailUtils;
import com.example.mps.exception.BusinessException;
import com.example.mps.pojo.dto.PageDto;
import com.example.mps.pojo.vo.UserVo;
import com.example.mps.service.UserService;
import com.example.mps.mapper.UserMapper;
import com.example.mps.utils.CommonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.mps.constant.UserConstant.*;

/**
* @author pengYuJun
* @description 针对表【tb_user(用户表)】的数据库操作Service实现
* @createDate 2025-03-21 10:LOGIN_NAME_MIN_LEN6:02
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo>
        implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private MailUtils mailUtils;

    @Override
    public long userRegister(String loginName, String loginPwd, String checkPwd, String email, String captcha, HttpServletRequest request) {
        // 1. 校验
        if (CommonUtils.isAnyBlank(loginName, loginPwd, checkPwd)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "参数为空");
        }
        if (loginName.length() < LOGIN_NAME_MIN_LEN){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户账号过短");
        }
        if (loginPwd.length() < LOGIN_PWD_MIN_LEN){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALID_LOGIN_NAME_PATTERN).matcher(loginName);
        if (matcher.find()){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
        // 密码与校验密码相同
        if (!loginPwd.equals(checkPwd)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "密码与校验密码相同");
        }
        // 校验验证码
        String captchaOg = (String) request.getSession().getAttribute("captcha");
        if (!StringUtils.hasText(captcha)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "验证码已失效，请重新获取");
        }
        if (!captcha.equals(captchaOg)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "验证码不匹配，请重新输入");
        }
        request.getSession().removeAttribute("captcha");
        // 账户不能重复
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDo::getLoginName, loginName);
        long count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账户不能重复");
        }
        // 邮箱不能重复
        queryWrapper.clear();
        queryWrapper.eq(UserDo::getEmail, email);
        count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "邮箱不能重复");
        }
        // 2. 加密
        String md5Pwd = DigestUtils.md5DigestAsHex((PASSWORD_SALT + loginPwd).getBytes());
        // 3. 插入数据
        UserDo userDo = new UserDo();
        userDo.setLoginName(loginName);
        userDo.setLoginPwd(md5Pwd);
        userDo.setEmail(email);
        boolean saveResult = this.save(userDo);
        if (!saveResult){
            throw new BusinessException(ResponseCode.ERROR, "注册失败");
        }
        return userDo.getId();
    }

    @Override
    public UserVo userLogin(String loginName, String loginPwd, HttpServletRequest request) {
        // 1. 校验
        if (CommonUtils.isAnyBlank(loginName, loginPwd)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "参数为空");
        }
        if (loginName.length() < LOGIN_NAME_MIN_LEN){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户账号过短");
        }
        if (loginPwd.length() < LOGIN_PWD_MIN_LEN){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALID_LOGIN_NAME_PATTERN).matcher(loginName);
        if (matcher.find()){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
        // 2. 加密
        String md5Pwd = DigestUtils.md5DigestAsHex((PASSWORD_SALT + loginPwd).getBytes());
        // 查询用户信息
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDo::getLoginName, loginName);
        queryWrapper.eq(UserDo::getLoginPwd, md5Pwd);
        UserDo userDo = this.getOne(queryWrapper);
        if (userDo == null){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "登录失败，账户或密码错误");
        }
        // 3.用户脱敏
        UserVo userVo = new UserVo(userDo);
        // LOGIN_NAME_MIN_LEN. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, userVo);
        return userVo;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public IPage<UserVo> searchUser(UserDto userDto, PageDto pageDto) {
        IPage<UserVo> userPage = new Page<>(pageDto.getCurrent(), pageDto.getPageSize());
        // 筛选
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(userDto.getUserName()), UserDo::getUserName, userDto.getUserName());
        queryWrapper.like(StringUtils.hasText(userDto.getLoginName()), UserDo::getLoginName, userDto.getLoginName());
        queryWrapper.eq(userDto.getGender() != null, UserDo::getGender, userDto.getGender());
        queryWrapper.like(StringUtils.hasText(userDto.getPhone()), UserDo::getPhone, userDto.getPhone());
        queryWrapper.like(StringUtils.hasText(userDto.getEmail()), UserDo::getEmail, userDto.getEmail());
        queryWrapper.eq(userDto.getStatus() != null, UserDo::getStatus, userDto.getStatus());
        queryWrapper.eq(userDto.getUserRole() != null, UserDo::getUserRole, userDto.getUserRole());
        // 逻辑删除不要查出来
        queryWrapper.eq(UserDo::getDeleted, 0);
        //
        userMapper.selectUserVoPage(userPage, queryWrapper);
        //若当前页码大于总页面数
        if (pageDto.getCurrent() > userPage.getPages()){
            userPage = new Page<>(userPage.getPages(), pageDto.getPageSize());
            userMapper.selectUserVoPage(userPage, queryWrapper);
        }
        return userPage;
    }

    @Override
    public String getMailCaptcha(String email, HttpServletRequest request) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        // 随机生成6为随机数
        String captcha = (Math.random() + "").substring(2, LOGIN_PWD_MIN_LEN);
        boolean result = mailUtils.sendCaptchaMail(captcha, email);
        if (result) {
            return captcha;
        }else {
            return null;
        }
    }

    @Override
    public boolean addUser(UserDo userDo) {
        // 1. 校验
        if (CommonUtils.isAnyBlank(userDo.getLoginName(), userDo.getLoginPwd())) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账户和密码不能为空");
        }
        if (userDo.getLoginName().length() < LOGIN_NAME_MIN_LEN){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userDo.getLoginPwd().length() < LOGIN_PWD_MIN_LEN){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        Matcher matcher = Pattern.compile(VALID_LOGIN_NAME_PATTERN).matcher(userDo.getLoginName());
        if (matcher.find()){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
        // 账户不能重复
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDo::getLoginName, userDo.getLoginName());
        long count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账户不能重复");
        }
        // 2. 加密
        String md5Pwd = DigestUtils.md5DigestAsHex((PASSWORD_SALT + userDo.getLoginPwd()).getBytes());
        userDo.setLoginPwd(md5Pwd);
        // 3.插入
        return this.save(userDo);
    }

    @Override
    public List<UserVo> searchUsersByTags(List<String> tagList){
        if (tagList == null || tagList.isEmpty()){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagList){
            queryWrapper = queryWrapper.like(UserDo::getTags, tagName);
        }
        List<UserDo> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(UserVo::new).toList();
    }

    @Override
    public UserVo getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVo currentUser = (UserVo) userObj;
        // 再查询，用户数据是否有效
        // todo: 校验用户是否合法
        try{
            Long userId = currentUser.getId();
            UserDo userDo = this.getById(userId);
            return new UserVo(userDo);
        }catch (Exception e){
            throw new BusinessException(ResponseCode.UNAUTHORIZED);
        }
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //
        UserVo user = (UserVo) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<UserVo> matchUsers(long num, UserVo loginUser) {
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(UserDo::getId, UserDo::getTags);
        queryWrapper.isNotNull(UserDo::getTags);
        List<UserDo> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        List<String> tagList = JSONUtil.toList(tags, String.class);
        // 用户列表的下表 => 相似度
        Map<Long, Long> distanceMap = new HashMap<>();
        for (UserDo user : userList) {
            List<String> userTagList = JSONUtil.toList(user.getTags(), String.class);
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            distanceMap.put(user.getId(), distance);
        }
        // 排序
        Map<Long, Long> sortedMap = distanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        (m1, m2) -> {});
        //下面这个是打印前num个的id和分数
        List<Long> idList =  new ArrayList<>((int) num);
        // 先排一次序到数据库查询
        sortedMap.forEach((k, v) -> {
            if (idList.size() > num){
                return;
            }
            idList.add(k);
        });
        //
        queryWrapper.clear();
        queryWrapper.in(UserDo::getId, idList);
        List<UserDo> userDoList = this.list(queryWrapper);
        List<UserVo> userVoList = userDoList.stream().map(UserVo::new).toList();
        //再排序
        List<UserVo> resultList = new ArrayList<>(idList.size());
        sortedMap.forEach((k, v) -> {
            userVoList.stream().filter(item -> item.getId().equals(k)).findFirst().ifPresent(resultList::add);
        });
        return resultList;
    }
}




