package gjavac.test.nft.erc721ForeverReward;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/17 15:46
 */
public class ERC721ForeverRewardStorage {
    public String name;
    public String symbol;
    public String state;
    public String admin;
    public String fixedSellContract;
    public String auctionContract;
    public int allTokenCount;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getFixedSellContract() {
        return fixedSellContract;
    }

    public void setFixedSellContract(String fixedSellContract) {
        this.fixedSellContract = fixedSellContract;
    }

    public String getAuctionContract() {
        return auctionContract;
    }

    public void setAuctionContract(String auctionContract) {
        this.auctionContract = auctionContract;
    }

    public int getAllTokenCount() {
        return allTokenCount;
    }

    public void setAllTokenCount(int allTokenCount) {
        this.allTokenCount = allTokenCount;
    }
}
