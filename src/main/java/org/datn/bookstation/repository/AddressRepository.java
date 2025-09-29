package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Integer>, JpaSpecificationExecutor<Address> {
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.status = 1 ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserIdOrderByIsDefaultDesc(@Param("userId") Integer userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.status = 1 ORDER BY a.updatedAt DESC")
    Optional<Address> findDefaultByUserId(@Param("userId") Integer userId);
}
