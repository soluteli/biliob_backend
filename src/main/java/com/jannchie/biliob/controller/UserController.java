package com.jannchie.biliob.controller;

import com.jannchie.biliob.constant.UserType;
import com.jannchie.biliob.exception.UserAlreadyFavoriteAuthorException;
import com.jannchie.biliob.exception.UserAlreadyFavoriteVideoException;
import com.jannchie.biliob.model.User;
import com.jannchie.biliob.service.UserService;
import com.jannchie.biliob.utils.Message;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.shiro.crypto.hash.Md5Hash;
import javax.validation.Valid;


@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user")
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        User newUser = userService.createUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/favorite/author")
    public ResponseEntity<Message> addFavoriteAuthor(@RequestBody @Valid Long mid) throws UserAlreadyFavoriteAuthorException {
        userService.addFavoriteAuthor(mid);
        return new ResponseEntity<Message>(new Message(201, "添加收藏作者成功"), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/favorite/video")
    public ResponseEntity<Message> addFavoriteVideo(@RequestBody @Valid Long aid) throws UserAlreadyFavoriteVideoException {
        userService.addFavoriteVideo(aid);
        return new ResponseEntity<Message>(new Message(201, "添加收藏视频成功"), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user")
    public ResponseEntity<User> createUser() {
        User user = userService.getUserInfo();
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/no-login")
    public ResponseEntity<Message> noLogin() {
        return new ResponseEntity<Message>(new Message(403, "未登录"), HttpStatus.FORBIDDEN);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/no-rule")
    public ResponseEntity<Message> noRule() {
        return new ResponseEntity<Message>(new Message(403, "未授权"), HttpStatus.FORBIDDEN);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ResponseEntity<Message> login(@RequestBody @Valid User user) {

        String inputName = user.getName();
        String inputPassword = user.getPassword();
        String encodedPassword = new Md5Hash(inputPassword, inputName).toHex();

        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();

        // 在认证提交前准备 token（令牌）
        UsernamePasswordToken token = new UsernamePasswordToken(inputName, encodedPassword);

        // 执行认证登陆
        subject.login(token);

        //根据权限，指定返回数据
        String role = userService.getRole(inputName);
        if (UserType.NORMAL_USER.equals(role)) {
            return new ResponseEntity<Message>(new Message(200, "登录成功"), HttpStatus.OK);
        }
        return new ResponseEntity<Message>(new Message(403, "登录失败"), HttpStatus.FORBIDDEN);
    }
}
