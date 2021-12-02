package gjavac.test.nft.auctionContract;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/25 10:31
 */
@Contract(storage = AuctionStorage.class)
public class AuctionContract extends UvmContract<AuctionStorage> {

    UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
    UvmSafeMathModule safemathModule = (UvmSafeMathModule) UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");

    @Override
    public void init() {
        print("auction contract creating");
        this.getStorage().tokenAddr = "";
        this.getStorage().timeBuffer = 150L;
        this.getStorage().auctionCount = 0;
        this.getStorage().state = "NOT_INITED";
        this.getStorage().admin = caller_address();
        this.getStorage().feeRate = 5L;
        this.getStorage().totalReward = new UvmMap<>();
        this.getStorage().currentReward = new UvmMap<>();
        print("auction contract created");
    }

    public final void initAuction(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        if (this.getStorage().state != utils.NOT_INITED()) {
            error("this token contract inited before");
            return;
        }
        UvmArray<String> parsed = utils.parseArgs(arg, 2, "argument format error, need format: token_addr,time_buffer");
        String tokenAddr = parsed.get(1);
        String timeBuffer = parsed.get(2);
        this.getStorage().tokenAddr = tokenAddr;
        this.getStorage().timeBuffer = tointeger(timeBuffer);
        this.getStorage().auctionCount = 0L;
        String fromAddress = utils.getFromAddress();
        if (fromAddress != caller_address()) {
            error("init_token can't be called from other contract");
            return;
        }
        this.getStorage().state = utils.COMMON();
        this.getStorage().admin = caller_address();
        this.getStorage().feeRate = 5L;
    }

    public final long createAuction(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(args, 6, "argument format error, need format: tokenId,tokenAddr,duration,reservePrice,symbol,minDeltaPrice");
        String tokenId = parsed.get(1);
        String tokenAddr = parsed.get(2);
        String duration = parsed.get(3);
        String reservePrice = parsed.get(4);
        String symbol = parsed.get(5);
        String minDeltaPrice = parsed.get(6);
        long auctionId = this.getStorage().auctionCount + 1;
        String fromAddress = utils.getFromAddress();
        this.getStorage().auctionCount = auctionId;
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenAddr);
        String owner = multiOwnedContractSimpleInterface.ownerOf(tokenId);
        utils.require(owner == fromAddress || owner == multiOwnedContractSimpleInterface.getApproved(tokenId), "Caller must be approved or owner for token id");
        utils.require(multiOwnedContractSimpleInterface.supportsERC721Interface(), "tokenContract does not support ERC721 interface");
        String curContract = get_current_contract_address();
        multiOwnedContractSimpleInterface.transferFrom(owner + "," + curContract + "," + tokenId);
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("tokenId", tokenId);
        uvmMap.set("tokenContract", tokenAddr);
        uvmMap.set("amount", 0);
        uvmMap.set("duration", duration);
        uvmMap.set("firstBidTime", 0);
        uvmMap.set("reservePrice", reservePrice);
        uvmMap.set("minDeltaPrice", minDeltaPrice);
        uvmMap.set("tokenOwner", fromAddress);
        uvmMap.set("bidder", "");
        uvmMap.set("symbol", symbol);
        fast_map_set("auctions", tostring(auctionId), json.dumps(uvmMap));
        emit("AuctionCreated", json.dumps(uvmMap));
        return auctionId;
    }

    public final void setAuctionReservePrice(String args) {
        Utils utils = new Utils();
        String fromAddress = utils.getFromAddress();
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: to,tokenId");
        String auctionId = parsed.get(1);
        String reservePrice = parsed.get(2);
        utils.require(fast_map_get("auctions", auctionId) == null, "auction Id not exists");
        String auctionData = tostring(fast_map_get("auctions", auctionId));
        UvmMap auctionObject = (UvmMap) json.loads(auctionData);
        utils.require(fromAddress == auctionObject.get("tokenOwner"), "Must be auction token owner");
        utils.require(tointeger(auctionObject.get("firstBidTime")) == 0, "Auction has already started");
        auctionObject.set("reservePrice", reservePrice);
        fast_map_set("auctions", auctionId, json.dumps(auctionObject));
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("auctionId", auctionId);
        uvmMap.set("tokenId", auctionObject.get("tokenId"));
        uvmMap.set("tokenContract", auctionObject.get("tokenContract"));
        uvmMap.set("reservePrice", reservePrice);
        emit("AuctionReservePriceUpdated", json.dumps(uvmMap));
    }

    public final void setAuctionMinDeltaPrice(String args) {
        Utils utils = new Utils();
        String fromAddress = utils.getFromAddress();
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: to,tokenId");
        String auctionId = parsed.get(1);
        String minDeltaPrice = parsed.get(2);
        utils.require(fast_map_get("auctions", auctionId) == null, "auction Id not exists");
        String auctionData = tostring(fast_map_get("auctions", auctionId));
        UvmMap auctionObject = (UvmMap) json.loads(auctionData);
        utils.require(fromAddress == auctionObject.get("tokenOwner"), "Must be auction token owner");
        utils.require(tointeger(auctionObject.get("firstBidTime")) == 0, "Auction has already started");
        auctionObject.set("minDeltaPrice", minDeltaPrice);
        fast_map_set("auctions", auctionId, json.dumps(auctionObject));
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("auctionId", auctionId);
        uvmMap.set("tokenId", auctionObject.get("tokenId"));
        uvmMap.set("tokenContract", auctionObject.get("tokenContract"));
        uvmMap.set("reservePrice", minDeltaPrice);
        emit("AuctionMinDeltaPriceUpdated", json.dumps(uvmMap));
    }

    public final void onDepositAsset(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmMap arg = (UvmMap) json.loads(args);
        long amout = tointeger(arg.get("num"));
        String symbol = tostring(arg.get("symbol"));
        String param = tostring(arg.get("param"));
        print(symbol);
        if (amout < 0) {
            error("deposit should greater than 0");
            return;
        }
        if (utils.isBlank(symbol)) {
            error("on_deposit_asset arg wrong");
            return;
        }
        String fromAddress = utils.getFromAddress();
        utils._createBid(this, param, tostring(amout), symbol);
    }

    public final void endAuction(String auctionId) {
        Utils utils = new Utils();
        utils.checkState(this);
        String fromAddress = utils.getFromAddress();
        utils.require(fast_map_get("auctions", auctionId) == null, "auction Id not exists");
        String auctionData = tostring(fast_map_get("auctions", auctionId));
        UvmMap auctionObject = (UvmMap) json.loads(auctionData);
        String lastBidder = tostring(auctionObject.get("bidder"));
        long blockNum = get_header_block_num();
        long firstBidTime = tointeger(auctionObject.get("firstBidTime"));
        long duration = tointeger(auctionObject.get("duration"));
        String curContract = get_current_contract_address();
        utils.require(firstBidTime != 0, "Auction hasn't begun");
        utils.require(blockNum >= firstBidTime + duration, "Auction hasn't completed");
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tostring(auctionObject.get("tokenContract")));
        UvmMap tokenData = (UvmMap) json.loads(multiOwnedContractSimpleInterface.queryTokenMinter(tostring(auctionObject.get("tokenId"))));
        long copyRightPayFee = tointeger(tokenData.get("fee"));
        String tokenMinter = tostring(tokenData.get("minter"));
        long closePrice = tointeger(auctionObject.get("amount"));
        UvmBigInt writePrice = safemathModule.bigint(
                safemathModule.mul(
                        safemathModule.bigint(closePrice), safemathModule.div(
                                safemathModule.mul(
                                        safemathModule.bigint(closePrice), safemathModule.bigint(this.getStorage().feeRate)), safemathModule.bigint(100))));
        UvmBigInt copyRightFee = safemathModule.div(safemathModule.mul(writePrice, safemathModule.bigint(copyRightPayFee)), safemathModule.bigint(100));
        UvmBigInt payValue = safemathModule.sub(writePrice, copyRightFee);
        utils.updateReward(this, tointeger(safemathModule.sub(safemathModule.bigint(closePrice), writePrice)), tostring(auctionObject.get("symbol")));
        multiOwnedContractSimpleInterface.feedTradePrice(tostring(auctionObject.get("tokenId")) + "," + tostring(auctionObject.get("symbol")) + "," + tostring(writePrice));
        utils.withdrawNativeAssetPrivate(this, tostring(auctionObject.get("tokenOwner")), tostring(auctionObject.get("symbol")), tostring(payValue));
        if (safemathModule.gt(copyRightFee, safemathModule.bigint(0))) {
            utils.withdrawNativeAssetPrivate(this, tokenMinter, tostring(auctionObject.get("symbol")), tostring(copyRightFee));
        }
        fast_map_set("auctions", auctionId, "{}");
        multiOwnedContractSimpleInterface.safeTransferFrom(curContract + "," + lastBidder + "," + auctionObject.get("tokenId"));
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("auctionId", auctionId);
        uvmMap.set("tokenId", auctionObject.get("tokenId"));
        uvmMap.set("tokenContract", auctionObject.get("tokenContract"));
        uvmMap.set("tokenOwner", auctionObject.get("tokenOwner"));
        uvmMap.set("bidder", auctionObject.get("lastBidder"));
        uvmMap.set("payValue", payValue);
        uvmMap.set("copyRightFee", copyRightFee);

        emit("AuctionEnded", json.dumps(uvmMap));
    }

    public final void cancelAuction(String auctionId) {
        Utils utils = new Utils();
        utils.checkState(this);
        String fromAddress = utils.getFromAddress();
        utils.require(fast_map_get("auctions", auctionId) == null, "auction Id not exists");
        String auctionData = tostring(fast_map_get("auctions", auctionId));
        UvmMap auctionObject = (UvmMap) json.loads(auctionData);
        utils.require(fromAddress == auctionObject.get("tokenOwner"), "Must be auction token owner");
        utils.require(tointeger(auctionObject.get("firstBidTime")) == 0, "Auction has already started");
        MultiOwnedContractSimpleInterface tokenContract = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tostring(auctionObject.get("tokenContract")));
        String curContract = get_current_contract_address();
        tokenContract.safeTransferFrom(curContract + "," + auctionObject.get("tokenOwner") + "," + auctionObject.get("tokenId"));
        fast_map_set("auctions", auctionId, "{}");
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("auctionId", auctionId);
        uvmMap.set("tokenId", auctionObject.get("tokenId"));
        uvmMap.set("tokenContract", auctionObject.get("tokenContract"));
        uvmMap.set("operator", "cancel");
        emit("AuctionCanceled", json.dumps(uvmMap));
    }

    public final void setFeeRate(String fee) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.require(tointeger(fee) >= 0 && tointeger(fee) <= 50, "invalid fee rate: " + fee);
        this.getStorage().feeRate = tointeger(fee);
    }

    public final void withdrawReward(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: amount,symbol");
        long amount = tointeger(parsed.get(1));
        String symbol = parsed.get(2);
        String fromAddress = utils.getFromAddress();
        utils.require(amount > 0, "amount must positive");
        utils.updateReward(this, 0 - amount, symbol);
        utils.withdrawNativeAssetPrivate(this, fromAddress, symbol, tostring(amount));
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("amount", amount);
        uvmMap.set("symbol", symbol);
        uvmMap.set("admin", fromAddress);
        emit("AdminWithdrawReward", json.dumps(uvmMap));
    }

    @Offline
    public final String getAuction(String auctionId) {
        return tostring(fast_map_get("auctions", auctionId));
    }

    @Offline
    public final boolean supportsERC721Interface(String arg) {
        return true;
    }

    @Offline
    public final String getInfo() {
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("timeBuffer", this.getStorage().timeBuffer);
        uvmMap.set("auctionCount", this.getStorage().auctionCount);
        uvmMap.set("state", this.getStorage().state);
        uvmMap.set("admin", this.getStorage().admin);
        uvmMap.set("feeRate", this.getStorage().feeRate);
        uvmMap.set("totalReward", this.getStorage().totalReward);
        uvmMap.set("currentReward", this.getStorage().currentReward);
        return json.dumps(uvmMap);
    }


}
