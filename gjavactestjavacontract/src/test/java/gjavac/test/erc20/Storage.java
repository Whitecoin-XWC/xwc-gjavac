package gjavac.test.erc20;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/8 14:47
 */
public class Storage {
    String name;
    String symbol;
    long supply;
    long precision;
    String state;
    boolean allowLock;
    long fee;
    long minTransferAmount;
    String feeReceiveAddress;
    String admin;

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

    public long getSupply() {
        return supply;
    }

    public void setSupply(long supply) {
        this.supply = supply;
    }

    public long getPrecision() {
        return precision;
    }

    public void setPrecision(long precision) {
        this.precision = precision;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isAllowLock() {
        return allowLock;
    }

    public void setAllowLock(boolean allowLock) {
        this.allowLock = allowLock;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getMinTransferAmount() {
        return minTransferAmount;
    }

    public void setMinTransferAmount(long minTransferAmount) {
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
}
