package space.kuikui.oj.service;

import space.kuikui.oj.model.entity.UserRank;

public interface UserRankService {
    public void updateRankingData();
     int addUserRank(UserRank userRank);
}
