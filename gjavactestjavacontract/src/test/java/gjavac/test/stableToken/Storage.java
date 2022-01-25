package gjavac.test.stableToken;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/11 14:11
 */
public class Storage {
    public String name;
    public String symbol;
    public Long supply;
    public Long precision;
    public String state;
    public boolean allowLock;
    public Long fee;
    /* 每次最低转账金额 */
    public Long minTransferAmount;
    /* 手续费接收地址 */
    public String feeReceiveAddress;
    /* admin user address */
    public String admin;
    public String minter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Long getSupply() {
        return supply;
    }

    public void setSupply(Long supply) {
        this.supply = supply;
    }

    public Long getPrecision() {
        return precision;
    }

    public void setPrecision(Long precision) {
        this.precision = precision;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean getAllowLock() {
        return allowLock;
    }

    public void setAllowLock(boolean allowLock) {
        this.allowLock = allowLock;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Long getMinTransferAmount() {
        return minTransferAmount;
    }

    public void setMinTransferAmount(Long minTransferAmount) {
        this.minTransferAmount = minTransferAmount;
    }

    public String getFeeReceiveAddress() {
        return feeReceiveAddress;
    }

    public void setFeeReceiveAddress(String feeReceiveAddress) {
        this.feeReceiveAddress = feeReceiveAddress;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getMinter() {
        return minter;
    }

    public void setMinter(String minter) {
        this.minter = minter;
    }
}
