package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public List<UserRoleRequest> userMapper(List<User> users) {
        return users.stream()
                .map(u -> new UserRoleRequest(
                        u.getId(),
                        u.getFullName() != null ? u.getFullName().trim() : null,
                        u.getPhoneNumber() != null ? u.getPhoneNumber().trim() : null,
                        u.getRole() != null ? u.getRole().getId() : null))
                .toList();
    }
}
