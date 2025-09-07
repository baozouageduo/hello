package com.tests.campuslostandfoundsystem.service.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tests.campuslostandfoundsystem.dao.PermissionsDAO;
import com.tests.campuslostandfoundsystem.dao.RolesDAO;
import com.tests.campuslostandfoundsystem.dao.UserDAO;
import com.tests.campuslostandfoundsystem.entity.CustomsUserDetail;
import com.tests.campuslostandfoundsystem.entity.enums.exception.UserResultCodes;
import com.tests.campuslostandfoundsystem.entity.permission.Permissions;
import com.tests.campuslostandfoundsystem.entity.roles.Roles;
import com.tests.campuslostandfoundsystem.entity.user.Profiles;
import com.tests.campuslostandfoundsystem.entity.user.Users;
import com.tests.campuslostandfoundsystem.exception.UserException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomsDetailsService implements UserDetailsService {

    private final UserDAO userDAO;
    private final RolesDAO rolesDAO;
    private final PermissionsDAO permissionsDAO;

    @Operation(summary = "通过用户名加载认证所需的用户信息")
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // 1) 查用户（按用户名；MP 的逻辑删除会自动生效）
            Users user = userDAO.selectOne(new QueryWrapper<Users>().eq("username", username));
            if (user == null) {
                throw new UserException(UserResultCodes.USER_NOT_FOUND, "用户不存在");
            }

            // 2) 角色（演示为全量；实际应改为“按用户ID查询关联角色”）
            List<Object> roleObjs = rolesDAO.selectObjs(new QueryWrapper<Roles>().select("role_name"));
            List<String> roles = roleObjs.stream()
                    .map(Object::toString)
                    .collect(Collectors.toCollection(ArrayList::new));

            // 3) 权限（演示为全量；实际应改为“按用户ID查询关联权限”）
            List<Object> permObjs = permissionsDAO.selectObjs(new QueryWrapper<Permissions>().select("permission_name"));
            List<String> permissions = permObjs.stream()
                    .map(Object::toString)
                    .collect(Collectors.toCollection(ArrayList::new));

            // 4) profiles（你已有的 mapper 方法）
            List<Profiles> profiles = userDAO.selectUsersProfilesByUserId(user.getId().toString());

            // 5) 组装 UserDetails（状态位可按表字段映射，这里先默认 true）
            return CustomsUserDetail.builder()
                    .userId(user.getId().toString())
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(roles)
                    .permissions(permissions)
                    .profiles(profiles)
                    .enabled(true)
                    .AccountNonExpired(true)
                    .AccountNonLocked(true)
                    .CredentialsNonExpired(true)
                    .build();

        } catch (UserException ue) {
            throw new InternalAuthenticationServiceException("用户加载失败: " + ue.getMessage(), ue);
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("用户加载失败: " + e.getMessage(), e);
        }
    }
}
