import com.marigo.model.domain.User;
import com.marigo.model.request.UserLoginRequest;
import com.marigo.model.request.UserRegisterRequest;
import com.marigo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import static com.marigo.constant.UserConstant.ADMIN_ROLE;
import static com.marigo.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 注册
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null){
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    // 用户登录
    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest == null){
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            return null;
        }
        return userService.userLogin(userAccount, userPassword, request);
    }


    // 用户搜索
    @GetMapping("/search")
    public List<User> searchUser(String username, HttpServletRequest request){
        if(!isAdmin(request)){
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
    }

    // 用户删除
    @PostMapping("/delete")
    public boolean deleteUser(@RequestParam Long id, HttpServletRequest request){
        // 管理员权限
        if (!isAdmin(request)) {
            return false;
        }
        return userService.removeById(id);
    }


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
