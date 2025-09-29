package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.UserRankRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserRankResponse;
import org.datn.bookstation.dto.response.UserRankSimpleResponse;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserRank;
import org.datn.bookstation.mapper.UserRankMapper;
import org.datn.bookstation.repository.RankRepository;
import org.datn.bookstation.repository.UserRankRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.UserRankService;
import org.datn.bookstation.specification.UserRankSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserRankServiceImpl implements UserRankService {
    private final UserRankRepository userRankRepository;
    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final UserRankMapper userRankMapper;

    @Override
    public List<UserRank> getAll() {
        return userRankRepository.findAll();
    }

    @Override
    public UserRank getById(Integer id) {
        return userRankRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<UserRank> add(UserRankRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        Rank rank = rankRepository.findById(request.getRankId()).orElse(null);
        if (user == null || rank == null) {
            return new ApiResponse<>(404, "User or Rank not found", null);
        }
        // Check duplicate for same user and rank
        List<UserRank> existingSameRank = userRankRepository.findByRankId(request.getRankId());
        boolean isDuplicate = existingSameRank.stream().anyMatch(ur -> ur.getUser() != null && ur.getUser().getId().equals(request.getUserId()));
        if (isDuplicate) {
            return new ApiResponse<>(409, "UserRank already exists for this user and rank", null);
        }
        // New logic: Prevent multiple active ranks for a user
        Byte reqStatus = request.getStatus() != null ? request.getStatus() : 1;
        if (reqStatus == 1) {
            // Tìm tất cả UserRank của user này có status = 1
            List<UserRank> activeRanks = userRankRepository.findAll().stream()
                .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(request.getUserId()) && ur.getStatus() != null && ur.getStatus() == 1)
                .collect(Collectors.toList());
            if (!activeRanks.isEmpty()) {
                return new ApiResponse<>(409, "Người dùng đã có một hạng đang hoạt động. Không thể tạo thêm hạng hoạt động mới.", null);
            }
        }
        // Nếu status = 0 thì vẫn cho phép tạo mới
        UserRank userRank = userRankMapper.toUserRank(request);
        userRank.setUser(user);
        userRank.setRank(rank);
        userRank.setStatus(reqStatus);
        userRank.setCreatedAt(Instant.now().toEpochMilli());
        UserRank saved = userRankRepository.save(userRank);
        return new ApiResponse<>(201, "Created", saved);
    }

    @Override
    public ApiResponse<UserRank> update(UserRankRequest request, Integer id) {
        UserRank existing = userRankRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "UserRank not found", null);
        }
        User user = userRepository.findById(request.getUserId()).orElse(null);
        Rank rank = rankRepository.findById(request.getRankId()).orElse(null);
        if (user == null || rank == null) {
            return new ApiResponse<>(404, "User or Rank not found", null);
        }
        existing.setUser(user);
        existing.setRank(rank);
        existing.setStatus(request.getStatus());
        existing.setUpdatedAt(Instant.now().toEpochMilli());
        UserRank saved = userRankRepository.save(existing);
        return new ApiResponse<>(200, "Updated", saved);
    }

    @Override
    public void delete(Integer id) {
        userRankRepository.deleteById(id);
    }

    @Override
    public ApiResponse<UserRank> toggleStatus(Integer id) {
        UserRank userRank = userRankRepository.findById(id).orElse(null);
        if (userRank == null) {
            return new ApiResponse<>(404, "UserRank not found", null);
        }
        
        // Kiểm tra xem UserRank này sẽ chuyển sang trạng thái hoạt động (1) hay không
        // willBeActive = true nếu status hiện tại là null hoặc 0 (sẽ chuyển thành 1)
        boolean willBeActive = userRank.getStatus() == null || userRank.getStatus() == 0;
        
        if (willBeActive) {
            Integer userId = userRank.getUser() != null ? userRank.getUser().getId() : null;
            if (userId != null) {
                // Tìm tất cả UserRank khác của cùng user đang có status = 1 (hoạt động)
                // Loại trừ UserRank hiện tại (id khác nhau)
                List<UserRank> activeRanks = userRankRepository.findAll().stream()
                    .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(userId)
                        && ur.getStatus() != null && ur.getStatus() == 1 && !ur.getId().equals(id))
                    .collect(Collectors.toList());
                
                // Nếu đã có UserRank khác đang hoạt động, trả về lỗi 409 (Conflict)
                if (!activeRanks.isEmpty()) {
                    return new ApiResponse<>(409, "Người dùng đã có một hạng đang hoạt động. Không thể bật thêm hạng hoạt động cho user này.", null);
                }
            }
        }
        
        // Toggle status: 1 -> 0 hoặc (null/0) -> 1
        userRank.setStatus(userRank.getStatus() != null && userRank.getStatus() == 1 ? (byte)0 : (byte)1);
        userRank.setUpdatedAt(Instant.now().toEpochMilli());
        UserRank saved = userRankRepository.save(userRank);
        return new ApiResponse<>(200, "Toggled status", saved);
    }

    @Override
    public PaginationResponse<UserRankResponse> getAllWithPagination(int page, int size, Integer userId, Integer rankId, Byte status, String userEmail, String rankName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<UserRank> specification = UserRankSpecification.filterBy(userId, rankId, status, userEmail, rankName);
        Page<UserRank> userRankPage = userRankRepository.findAll(specification, pageable);
        List<UserRankResponse> responses = userRankPage.getContent().stream()
                .map(userRankMapper::toUserRankResponse)
                .collect(Collectors.toList());
        return PaginationResponse.<UserRankResponse>builder()
                .content(responses)
                .pageNumber(userRankPage.getNumber())
                .pageSize(userRankPage.getSize())
                .totalElements(userRankPage.getTotalElements())
                .totalPages(userRankPage.getTotalPages())
                .build();
    }

    @Override
    public List<UserRankSimpleResponse> getByRankId(Integer rankId) {
        List<UserRank> userRanks = userRankRepository.findByRankId(rankId);
        return userRanks.stream().map(ur -> new UserRankSimpleResponse(
                ur.getId(),
                ur.getUser() != null ? ur.getUser().getEmail() : null,
                ur.getUser() != null ? ur.getUser().getFullName() : null,
                ur.getStatus(),
                ur.getCreatedAt(),
                ur.getUpdatedAt()
        )).collect(Collectors.toList());
    }

    @Override
    public PaginationResponse<UserRankSimpleResponse> getByRankIdWithFilter(int page, int size, Integer rankId, String email, String userName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserRank> userRankPage = userRankRepository.findByRankIdAndUserEmailContainingIgnoreCaseAndUserFullNameContainingIgnoreCase(
                rankId,
                email != null ? email : "",
                userName != null ? userName : "",
                pageable
        );
        List<UserRankSimpleResponse> responses = userRankPage.getContent().stream().map(ur -> new UserRankSimpleResponse(
                ur.getId(),
                ur.getUser() != null ? ur.getUser().getEmail() : null,
                ur.getUser() != null ? ur.getUser().getFullName() : null,
                ur.getStatus(),
                ur.getCreatedAt(),
                ur.getUpdatedAt()
        )).collect(Collectors.toList());
        return PaginationResponse.<UserRankSimpleResponse>builder()
                .content(responses)
                .pageNumber(userRankPage.getNumber())
                .pageSize(userRankPage.getSize())
                .totalElements(userRankPage.getTotalElements())
                .totalPages(userRankPage.getTotalPages())
                .build();
    }
}
