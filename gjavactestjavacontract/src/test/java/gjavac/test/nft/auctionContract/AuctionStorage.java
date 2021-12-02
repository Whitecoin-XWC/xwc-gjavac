package gjavac.test.nft.auctionContract;

import gjavac.lib.UvmMap;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/25 10:30
 */
public class AuctionStorage {
    public String tokenAddr;
    public long timeBuffer;
    public long auctionCount;
    public String admin;
    public String state;
    public long feeRate;
    public UvmMap<Long> totalReward;
    public UvmMap<Long> currentReward;

    public String getTokenAddr() {
        return tokenAddr;
    }

    public void setTokenAddr(String tokenAddr) {
        this.tokenAddr = tokenAddr;
    }

    public long getTimeBuffer() {
        return timeBuffer;
    }

    public void setTimeBuffer(long timeBuffer) {
        this.timeBuffer = timeBuffer;
    }

    public long getAuctionCount() {
        return auctionCount;
    }

    public void setAuctionCount(long auctionCount) {
        this.auctionCount = auctionCount;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(long feeRate) {
        this.feeRate = feeRate;
    }

    public UvmMap<Long> getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(UvmMap<Long> totalReward) {
        this.totalReward = totalReward;
    }

    public UvmMap<Long> getCurrentReward() {
        return currentReward;
    }

    public void setCurrentReward(UvmMap<Long> currentReward) {
        this.currentReward = currentReward;
    }
}
