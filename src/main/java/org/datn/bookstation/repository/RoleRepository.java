package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Role;
import org.datn.bookstation.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleName roleName);

    // Lấy danh sách id và tên vai trò (tiếng Việt)
    @Query("SELECT new org.datn.bookstation.dto.response.RoleDropdownResponse(r.id, CAST(r.roleName AS string)) FROM Role r")
    List<org.datn.bookstation.dto.response.RoleDropdownResponse> getRoleDropdown();
}
