package gjavac.test.dai;

import gjavac.lib.UvmArray;

public class Storage {
    public String owner;
    public String state;
    public String baseAsset;
    public String maxChangeRatio;
    public String quotaAsset;

    //baseAsset以quotaAsset计价的价(baseAsset/quotaAsset)
    public String price;

    public UvmArray<String> feeders;
    public UvmArray<String> feedPrices;
}
