package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.repository.RankRepository;
import org.datn.bookstation.service.RankService;
import org.springframework.stereotype.Service;
import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.mapper.RankMapper;
import org.datn.bookstation.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.RankResponse;
import org.datn.bookstation.mapper.RankResponseMapper;
import org.datn.bookstation.specification.RankSpecification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class RankServiceImpl implements RankService {
    private final RankRepository rankRepository;
    private final RankMapper rankMapper;
    private final RankResponseMapper rankResponseMapper;

    @Override
    public List<Rank> getAll() {
        return rankRepository.findAll();
    }

    @Override
    public List<Rank> getAllActiveRanks() {
        return rankRepository.findAll().stream()
            .filter(rank -> rank.getStatus() != null && rank.getStatus() == 1)
            .toList();
    }

    @Override
    public Rank getById(Integer id) {
        return rankRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<Rank> add(RankRequest rankRequest) {
        if (rankRepository.existsByRankName(rankRequest.getRankName())) {
            return new ApiResponse<>(400, "Tên hạng đã tồn tại", null);
        }
        Rank rank = rankMapper.toRank(rankRequest);
        rank.setCreatedAt(java.time.Instant.now().toEpochMilli());
        Rank saved = rankRepository.save(rank);
        return new ApiResponse<>(201, "Tạo mới thành công", saved);
    }

    @Override
    public Rank update(Rank rank, Integer id) {
        Rank existing = rankRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy hạng"));
        rank.setId(id);
        rank.setCreatedAt(existing.getCreatedAt());
        rank.setUpdatedAt(Instant.now().toEpochMilli());
        return rankRepository.save(rank);
    }

    @Override
    public void delete(Integer id) {
        rankRepository.deleteById(id);
    }

    @Override
    public PaginationResponse<RankResponse> getAllWithPagination(int page, int size, String name, Byte status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Rank> spec = RankSpecification.filterBy(name, status);
        Page<Rank> rankPage = rankRepository.findAll(spec, pageable);
        List<RankResponse> responses = rankPage.getContent().stream().map(rankResponseMapper::toResponse).toList();
        return new PaginationResponse<>(
            responses,
            rankPage.getNumber(),
            rankPage.getSize(),
            rankPage.getTotalElements(),
            rankPage.getTotalPages()
        );
    }

    @Override
    public ApiResponse<Rank> toggleStatus(Integer id) {
        Rank rank = rankRepository.findById(id).orElse(null);
        if (rank == null) {
            return new ApiResponse<>(404, "Không tìm thấy", null);
        }
        if (rank.getStatus() == null) {
            rank.setStatus((byte) 1);
        } else {
            rank.setStatus((byte) (rank.getStatus() == 1 ? 0 : 1));
        }
        rank.setUpdatedAt(Instant.now().toEpochMilli());
        rankRepository.save(rank);
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", rank);
    }
}
