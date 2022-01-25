package gjavac.test.nft.erc721;

import gjavac.lib.Component;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/25 9:35
 */
public class ERC721Storage {
    public String name;
    public String symbol;
    public String state;
    public String admin;
    public long allTokenCount;

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

    public long getAllTokenCount() {
        return allTokenCount;
    }

    public void setAllTokenCount(long allTokenCount) {
        this.allTokenCount = allTokenCount;
    }
}
