package org.datn.bookstation.mapper;
import org.datn.bookstation.dto.request.RegisterRequest;
import org.datn.bookstation.dto.response.RegisterResponse;
import org.datn.bookstation.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    User toEntity(RegisterRequest request);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", expression = "java(user.getRole() != null ? user.getRole().getRoleName().name() : null)")
    RegisterResponse toRegisterResponse(User user);
}
