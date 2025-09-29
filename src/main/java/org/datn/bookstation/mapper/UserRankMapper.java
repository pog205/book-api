package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.UserRankRequest;
import org.datn.bookstation.dto.response.UserRankResponse;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserRank;
import org.springframework.stereotype.Component;

@Component
public class UserRankMapper {
    public UserRank toUserRank(UserRankRequest request) {
        UserRank userRank = new UserRank();
        if (request.getUserId() != null) {
            User user = new User();
            user.setId(request.getUserId());
            userRank.setUser(user);
        }
        if (request.getRankId() != null) {
            Rank rank = new Rank();
            rank.setId(request.getRankId());
            userRank.setRank(rank);
        }
        userRank.setStatus(request.getStatus());
        return userRank;
    }

    public UserRankResponse toUserRankResponse(UserRank userRank) {
        return new UserRankResponse(
            userRank.getId(),
            userRank.getUser() != null ? userRank.getUser().getId() : null,
            userRank.getUser() != null ? userRank.getUser().getEmail() : null,
            userRank.getRank() != null ? userRank.getRank().getId() : null,
            userRank.getRank() != null ? userRank.getRank().getRankName() : null,
            userRank.getStatus()
        );
    }
}
