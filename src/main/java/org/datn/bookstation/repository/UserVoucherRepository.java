package org.datn.bookstation.repository;

import org.datn.bookstation.entity.UserVoucher;
import org.datn.bookstation.dto.response.voucherUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import org.datn.bookstation.dto.response.UserForVoucher;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    @Query("SELECT new org.datn.bookstation.dto.response.voucherUserResponse(" +
           "uv.voucher.id, uv.voucher.code, uv.voucher.name, uv.voucher.description, uv.voucher.voucherCategory, " +
           "uv.voucher.discountPercentage, uv.voucher.discountAmount, uv.voucher.startTime, uv.voucher.endTime, " +
           "uv.voucher.minOrderValue, uv.voucher.maxDiscountValue, uv.voucher.usageLimit, uv.usedCount, " +
           "uv.voucher.usageLimitPerUser, uv.voucher.status) " +
           "FROM UserVoucher uv WHERE uv.user.id = :userId")
    List<voucherUserResponse> findVouchersByUserId(Integer userId);

    @Query("SELECT new org.datn.bookstation.dto.response.voucherUserResponse(" +
       "uv.voucher.id, uv.voucher.code, uv.voucher.name, uv.voucher.description, uv.voucher.voucherCategory, " +
       "uv.voucher.discountPercentage, uv.voucher.discountAmount, uv.voucher.startTime, uv.voucher.endTime, " +
       "uv.voucher.minOrderValue, uv.voucher.maxDiscountValue, uv.voucher.usageLimit, uv.usedCount, " +
       "uv.voucher.usageLimitPerUser, uv.voucher.status) " +
       "FROM UserVoucher uv WHERE uv.voucher.code = :vouchercode")
List<voucherUserResponse> findVouchersByVoucherId(String vouchercode);

@Query("SELECT new org.datn.bookstation.dto.response.voucherUserResponse(" +
"uv.voucher.id, uv.voucher.code, uv.voucher.name, uv.voucher.description, uv.voucher.voucherCategory, " +
"uv.voucher.discountPercentage, uv.voucher.discountAmount, uv.voucher.startTime, uv.voucher.endTime, " +
"uv.voucher.minOrderValue, uv.voucher.maxDiscountValue, uv.voucher.usageLimit, uv.usedCount, " +
"uv.voucher.usageLimitPerUser, uv.voucher.status) " +
"FROM UserVoucher uv WHERE uv.voucher.code = :vouchercode AND uv.user.id = :userId")
List<voucherUserResponse> findVouchersByVoucherUserId(String vouchercode, Integer userId);

    // List<UserVoucher> findByUserId(Integer userId);

    Optional<UserVoucher> findByUserIdAndVoucherId(Integer userId, Integer voucherId);



List<UserVoucher> findByVoucherId(Integer voucherId);
boolean existsByUser_IdAndVoucher_Id(Integer userId, Integer voucherId);

}
