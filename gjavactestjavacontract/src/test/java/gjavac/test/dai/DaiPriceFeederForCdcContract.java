package gjavac.test.dai;

import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;


@Contract(storage = Storage.class)
public class DaiPriceFeederForCdcContract extends UvmContract<Storage>{
    public static final int MAX_FEEDERS_COUNT = 100;
    public static final String STATE_NOT_INITED = "NOT_INITED";
    public static final String STATE_COMMON = "COMMON";

    @Override
    public void init() {
        Storage storage = this.getStorage();
        //assert(storage != null);
        storage.owner = caller_address();
        storage.state = STATE_NOT_INITED;
        storage.baseAsset = "";
        storage.maxChangeRatio = "";
        storage.quotaAsset = "";
        storage.feeders = UvmArray.<String>create();
        storage.feedPrices = UvmArray.<String>create();
        storage.price = "";
    }

    @Override
    public void on_destory() {
        UvmCoreLibs.error("can't destroy price feeder contract");
    }

    //args: baseAsset,quotaAsset,init_price,maxChangeRatio
    public String init_config(String arg) {
        Utils utils = new Utils();
        utils.checkOwner(this);

        Storage storage = this.getStorage();
        if(storage.state != STATE_NOT_INITED) {
            UvmCoreLibs.error("already inited");
            return "FAIL";
        }

        UvmArray<String> argsArray = utils.parseArgs(arg,4,"arg format wrong,need format:baseAsset,quotaAsset,init_price,maxChangeRatio");

        String strPrice = argsArray.get(3);
        if(UvmCoreLibs.tonumber(strPrice) <= 0) {
            UvmCoreLibs.error("price must > 0");
            return "FAIL";
        }

        String strMaxChangeRatio = argsArray.get(4);
        if (UvmCoreLibs.tonumber(strMaxChangeRatio) <= 0) {
            UvmCoreLibs.error("maxChangeRatio must > 0");
            return "FAIL";
        }

        storage.baseAsset = argsArray.get(1);
        storage.quotaAsset = argsArray.get(2);

        storage.feeders = UvmArray.<String>create();
        storage.feeders.add(storage.owner);

        storage.price = strPrice;

        storage.feedPrices = UvmArray.<String>create();
        storage.feedPrices.add(strPrice);

        storage.maxChangeRatio = strMaxChangeRatio;
        storage.state = STATE_COMMON;
        UvmCoreLibs.emit("Inited", arg);

        return "OK";
    }

    @Override
    public void on_deposit_asset(String args) {
        UvmCoreLibs.error("not supported deposit to price feeder contract");
    }

    public void change_owner(String new_owner) {
        Utils utils = new Utils();
        utils.checkOwner(this);
        utils.checkAddress(new_owner);

        Storage storage = this.getStorage();
        String currentOwner = storage.owner;
        if(new_owner == currentOwner) {
            UvmCoreLibs.error("new owner address can't be same with the old one");
        }

        storage.owner = new_owner;
        UvmCoreLibs.emit("OwnerChanged", new_owner);
    }

    public String add_feeder(String feeder_to_add) {
        Utils utils = new Utils();
        utils.checkOwner(this);
        utils.checkState(this);
        utils.checkAddress(feeder_to_add);

        Storage storage = this.getStorage();
        UvmArray<String> feeders = storage.feeders;
        UvmArray<String> feedPrices = storage.feedPrices;

        if(utils.arrayContains(feeders, feeder_to_add)){
            UvmCoreLibs.error("new feeder address can't be existed in the current feeders list");
            return "FAIL";
        }

        if(feeders.size() >= MAX_FEEDERS_COUNT) {
            UvmCoreLibs.error("exceed max feeders count:" + MAX_FEEDERS_COUNT);
            return "FAIL";
        }

        feeders.add(feeder_to_add);
        //Assign the current average price.
        feedPrices.add(storage.price);
        //Obviously, no need to recalculate storage.price (average price).

        UvmCoreLibs.emit("FeederAdded", feeder_to_add);

        return "OK";
    }

    public String remove_feeder(String feeder_to_remove) {
        Utils utils = new Utils();
        utils.checkOwner(this);
        utils.checkState(this);

        Storage storage = this.getStorage();
        UvmArray<String> feeders = storage.feeders;
        UvmArray<String> feedPrices = storage.feedPrices;

        if(feeders.size() <= 1) {
            UvmCoreLibs.error("only one feeder existed, can't remove");
            return "FAIL";
        }
        int iFeederIdx = utils.findElementInArray(feeders, feeder_to_remove,"The feeder is not existed! feeder:" + feeder_to_remove);

        UvmTableModule uvmTableModule = (UvmTableModule)UvmCoreLibs.importModule(UvmTableModule.class, "table");
        uvmTableModule.remove(feeders, iFeederIdx);
        uvmTableModule.remove(feedPrices, iFeederIdx);

        //the storage.price (average price) should be recalculated, since the feed prices were changed.
        storage.price = utils.calculatePrice(feedPrices);

        UvmCoreLibs.emit("FeederRemoved", feeder_to_remove);
        return "OK";
    }

    public String feed_price(String priceStr) {
        Utils utils = new Utils();
        utils.checkState(this);

        Storage storage = this.getStorage();
        UvmArray<String> feeders = storage.feeders;
        UvmArray<String> feedPrices = storage.feedPrices;

        String fromAddress = utils.getFromAddress();
        int iFeederIdx = utils.findElementInArray(feeders, fromAddress,"The feeder is not existed! feeder:" + fromAddress);

        String originalPriceStr = feedPrices.get(iFeederIdx);

        double dPrice = UvmCoreLibs.tonumber(priceStr);
        double dOriginalPrice = UvmCoreLibs.tonumber(originalPriceStr);
        double dMaxChangeRatio = UvmCoreLibs.tonumber(storage.maxChangeRatio);

        UvmMathModule uvmMathModule = (UvmMathModule)UvmCoreLibs.importModule(UvmMathModule.class, "math");

        double priceChangeRatio = uvmMathModule.abs(dPrice - dOriginalPrice) / dOriginalPrice;

        if (priceChangeRatio > dMaxChangeRatio) {
            UvmCoreLibs.error("exceed max change ratio:" + storage.maxChangeRatio);
            return "FAIL";
        }

        feedPrices.set(iFeederIdx, priceStr);
        storage.price = utils.calculatePrice(feedPrices);

        UvmMap eventArg = UvmMap.create();
        eventArg.set("feeder", fromAddress);
        eventArg.set("price", priceStr);
        String eventArgStr = UvmCoreLibs.tojsonstring(eventArg);
        UvmCoreLibs.emit("PriceFeeded", eventArgStr);
        return "OK";
    }

    @Offline
    public String owner(String arg) {
        Storage storage = this.getStorage();
        return storage.owner;
    }

    @Offline
    public String feeders(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);

        Storage storage = this.getStorage();
        UvmArray<String> feeders = storage.feeders;
        UvmJsonModule jsonModule = (UvmJsonModule)UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        return jsonModule.dumps(feeders);
    }

    @Offline
    public String feedPrices(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);

        Storage storage = this.getStorage();
        UvmArray<String> feeders = storage.feeders;
        UvmArray<String> feederPrices = storage.feedPrices;
        int iCount = feeders.size();
        UvmMap<String> mapFeederPrice = UvmMap.<String>create();
        for (int iIdx = 1; iIdx <= iCount; iIdx++) {
            mapFeederPrice.set(feeders.get(iIdx), feederPrices.get(iIdx));
        }

        return UvmCoreLibs.tojsonstring(mapFeederPrice);
    }

    @Offline
    public String getPrice(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        Storage storage = this.getStorage();
        return storage.price;
    }

    @Offline
    public String baseAsset(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        Storage storage = this.getStorage();
        return storage.baseAsset;
    }

    @Offline
    public String quotaAsset(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        Storage storage = this.getStorage();
        return storage.quotaAsset;
    }

    @Offline
    public String state(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        Storage storage = this.getStorage();
        return storage.state;
    }
}
