package com.example.mps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mps.common.ResponseEntity;
import com.example.mps.common.ResponseCode;
import com.example.mps.exception.BusinessException;
import com.example.mps.pojo.domain.UserDo;
import com.example.mps.pojo.dto.PageDto;
import com.example.mps.pojo.dto.UserDto;
import com.example.mps.pojo.vo.UserVo;
import com.example.mps.pojo.dto.UserLoginDto;
import com.example.mps.pojo.dto.UserRegisterDto;
import com.example.mps.service.UserService;
import com.example.mps.utils.CommonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.mps.constant.UserConstant.*;

/**
 * @author pengYuJun
 */
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public ResponseEntity<Long> userRegister(@RequestBody UserRegisterDto userRegisterDto, HttpServletRequest request) {
        if (userRegisterDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "参数为空");
        }
        String loginName = userRegisterDto.getLoginName();
        String loginPwd = userRegisterDto.getLoginPwd();
        String checkPwd = userRegisterDto.getCheckPwd();
        String email = userRegisterDto.getEmail();
        String captcha = userRegisterDto.getCaptcha();
        if (CommonUtils.isAnyBlank(loginName, loginPwd, checkPwd, email, captcha)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "参数为空");
        }
        long result = userService.userRegister(loginName, loginPwd, checkPwd, email,  captcha, request);
        return ResponseEntity.success(result);
    }

    @PostMapping("/login")
    public ResponseEntity<UserVo> userLogin(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
        if (userLoginDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "参数为空");
        }
        String loginName = userLoginDto.getLoginName();
        String loginPwd = userLoginDto.getLoginPwd();
        if (CommonUtils.isAnyBlank(loginName, loginPwd)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "参数为空");
        }
        UserVo user = userService.userLogin(loginName, loginPwd, request);
        return ResponseEntity.success(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "会话无效");
        }
        int result = userService.userLogout(request);
        return ResponseEntity.success(result);
    }

    @GetMapping("/current")
    public ResponseEntity<UserVo> getCurrentUser(HttpServletRequest request) {
        UserVo currentUser = userService.getCurrentUser(request);
        return ResponseEntity.success(currentUser);
    }

    @GetMapping("/search")
    public ResponseEntity<IPage<UserVo>> searchUsers(UserDto userDto, PageDto pageDto, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "无权限访问");
        }
        IPage<UserVo> userPage = userService.searchUser(userDto, pageDto);
        return ResponseEntity.success(userPage);
    }

    @GetMapping("/search/tags")
    public ResponseEntity<List<UserVo>> searchUsersByTags(@RequestParam(required = false) List<String> tagList) {
        List<UserVo> userVos = userService.searchUsersByTags(tagList);
        return ResponseEntity.success(userVos);
    }

    @GetMapping("/recommend")
    public ResponseEntity<Page<UserVo>> recommendUsers(PageDto pageDto, HttpServletRequest request) {
        UserVo currentUser = userService.getCurrentUser(request);
        String redisKey = String.format("mps:user:recommend:%s",currentUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读取
        Page<UserVo> userPage = (Page<UserVo>) valueOperations.get(redisKey);
        if (userPage != null){
            return ResponseEntity.success(userPage);
        }
        //无缓存，查数据库
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        Page<UserDo> page = userService.page(new Page<>(pageDto.getCurrent(), pageDto.getPageSize()), queryWrapper);

        List<UserDo> doList = page.getRecords();
        List<UserVo> voList = doList.stream().map(UserVo::new).toList();

        Page<UserVo> pageVo = new Page<>();
        pageVo.setRecords(voList);
        pageVo.setTotal(page.getTotal());
        pageVo.setSize( page.getSize());
        pageVo.setCurrent(page.getCurrent());
        //写缓存,30s过期
        try {
            valueOperations.set(redisKey, pageVo,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis set key error", e);
        }
        return ResponseEntity.success(pageVo);
    }

    @PostMapping("/delete")
    public ResponseEntity<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "无权限访问");
        }
        if (id <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "删除请求id值无效");
        }
        boolean result = userService.removeById(id);
        if (result) {
            return ResponseEntity.success(true);
        }else {
            return ResponseEntity.failed("删除失败");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Boolean> updateUser(@RequestBody UserDo userDo, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "无权限访问");
        }
        if (userDo.getId() == null || userDo.getId() <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "更新请求id值无效");
        }
        boolean result = userService.updateById(userDo);
        if (result) {
            return ResponseEntity.success(true);
        }else {
            return ResponseEntity.failed("更新失败");
        }
    }

    @PostMapping("update/self")
    public  ResponseEntity<Boolean> updateUserSelf(@RequestBody UserDo userDo, HttpServletRequest request) {
        //
        if (userDo == null || userDo.getId() == null || CommonUtils.areAllOtherFieldsNull(userDo, "id")) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "更新请求参数无效");
        }
        UserVo currentUser = userService.getCurrentUser(request);
        if (!currentUser.getId().equals(userDo.getId())) {
            throw new BusinessException(ResponseCode.FORBIDDEN);
        }
        boolean result = userService.updateById(userDo);
        if (result) {
            return ResponseEntity.success(true);
        }else {
            return ResponseEntity.failed("更新失败");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Boolean> addUser(@RequestBody UserDo userDo, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "无权限访问");
        }
        boolean result = userService.addUser(userDo);
        if (result) {
            return ResponseEntity.success(true);
        }else {
            return ResponseEntity.failed("新增失败");
        }
    }

    @GetMapping("mailCaptcha")
    public ResponseEntity<Boolean> getMailCaptcha(String email, HttpServletRequest request) {
        String captcha = userService.getMailCaptcha(email, request);
        if (StringUtils.hasText(captcha)){
            request.getSession().setAttribute("captcha", captcha);
            return ResponseEntity.success(true);
        }else {
            return ResponseEntity.failed("获取验证码失败");
        }
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public ResponseEntity<List<UserVo>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        UserVo user = userService.getCurrentUser(request);
        return ResponseEntity.success(userService.matchUsers(num, user));
    }

}
