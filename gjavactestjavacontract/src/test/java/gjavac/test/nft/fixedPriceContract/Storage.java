package gjavac.test.nft.fixedPriceContract;

import gjavac.lib.UvmMap;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/17 13:52
 */
public class Storage {
    public String tokenAddr;
    public String admin;
    public String state;
    public Long feeRate;
    public UvmMap<Long> totalReward;
    public UvmMap<Long> currentRReward;

    public String getTokenAddr() {
        return tokenAddr;
    }

    public void setTokenAddr(String tokenAddr) {
        this.tokenAddr = tokenAddr;
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

    public Long getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Long feeRate) {
        this.feeRate = feeRate;
    }

    public UvmMap<Long> getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(UvmMap<Long> totalReward) {
        this.totalReward = totalReward;
    }

    public UvmMap<Long> getCurrentRReward() {
        return currentRReward;
    }

    public void setCurrentRReward(UvmMap<Long> currentRReward) {
        this.currentRReward = currentRReward;
    }
}
