package gjavac.test.nft.auctionContract;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/25 10:34
 */
@Component
public class Utils {

    public UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");

    public String NOT_INITED() {
        return "NOT_INITED";
    }


    public String COMMON() {
        return "COMMON";
    }


    public String PAUSED() {
        return "PAUSED";
    }


    public String STOPPED() {
        return "STOPPED";
    }

    public final String getFromAddress() {
        String fromAddress;
        final String prevContractId = get_prev_call_frame_contract_address();
        if (isNotBlank(prevContractId) && is_valid_address(prevContractId)) {
            fromAddress = prevContractId;
        } else {
            fromAddress = caller_address();
        }
        return fromAddress;
    }

    public final void checkAdmin(AuctionContract self) {
        String fromAddress = getFromAddress();
        if (self.getStorage().admin != fromAddress) {
            error("you are not admin, can't call this function");
        }
    }

    public final void checkState(AuctionContract self) {
        String state = self.getStorage().state;
        if (state != COMMON()) {
            error("state error, now state is " + state);
        }
    }

    public final void checkStateInited(AuctionContract self) {
        if (self.getStorage().state == NOT_INITED())
            error("contract token not inited");
    }

    public final UvmArray<String> parseArgs(String arg, int count, String errorMsg) {
        if (isBlank(arg)) {
            error(errorMsg);
            return UvmArray.create();
        }
        UvmStringModule stringModule = importModule(UvmStringModule.class, "string");
        UvmArray<String> parsed = stringModule.split(arg, ",");
        if (parsed != null && parsed.size() == count) {
            return parsed;
        } else {
            error(errorMsg);
            return UvmArray.create();
        }
    }

    public final UvmArray<String> parseAtLeastArgs(String arg, int count, String errorMsg) {
        if (isBlank(arg)) {
            error(errorMsg);
            return UvmArray.create();
        }

        UvmStringModule stringModule = importModule(UvmStringModule.class, "string");
        UvmArray<String> parsed = stringModule.split(arg, ",");
        if (parsed != null && parsed.size() >= count) {
            return parsed;
        } else {
            error(errorMsg);
            return UvmArray.create();
        }

    }

    public final void updateReward(AuctionContract table, Long amount, String symbol) {
        AuctionStorage storage = table.getStorage();
        UvmMap<Long> totalReward = storage.getTotalReward();
        UvmMap<Long> currentRReward = storage.getCurrentReward();
        if (amount > 0) {
            if (totalReward.get(symbol) != null) {
                totalReward.set(symbol, totalReward.get(symbol) + amount);
            } else {
                totalReward.set(symbol, amount);
            }
        }

        if (currentRReward.get(symbol) != null) {
            currentRReward.set(symbol, currentRReward.get(symbol) + amount);
        } else {
            currentRReward.set(symbol, amount);
        }
    }

    public final boolean checkAddress(String addr) {
        boolean result = is_valid_address(addr);
        if (!result) {
            error("address format error");
        }
        return result;
    }

    public final boolean require(boolean success, String text) {
        if (success)
            return true;
        error(text);
        return false;
    }

    public final boolean checkContractAddress(String addr) {
        boolean valid_address = is_valid_address(addr);
        if (!valid_address) {
            error("contract address format error");
            return false;
        }
        return valid_address;
    }

    public final void withdrawNativeAssetPrivate(AuctionContract self, String from, String symbol, String amountStr) {
        checkState(self);
        long amount = tointeger(amountStr);
        if (isBlank(symbol) || amount <= 0) {
            error("invalid params");
            return;
        }
        int res1 = transfer_from_contract_to_address(from, symbol, amount);
        if (res1 != 0) {
            error("transfer asset " + symbol + " to " + from + " amount:" + tostring(amount) + " error, error code: " + tostring(res1));
            return;
        }

        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("address", from);
        uvmMap.set("symbol", symbol);
        uvmMap.set("change", 0 - amount);
        uvmMap.set("reason", "withdraw");
        emit("NativeBalanceChange", json.dumps(uvmMap));
    }

    public final void _createBid(AuctionContract self, String auctionId, String amount, String symbol) {
        String fromAddress = getFromAddress();
        require(tostring(fast_map_get("auctions", tostring(auctionId))) != null, "auction Id not exists");
        String auctionData = tostring(fast_map_get("auctions", tostring(auctionId)));
        UvmMap auctionObject = (UvmMap) totable(json.loads(auctionData));
        String lastBidder = tostring(auctionObject.get("bidder"));
        long block_num = tointeger(get_header_block_num());
        require(symbol == auctionObject.get("symbol"), "The auction must use the same asset.");
        long firstBidTime = tointeger(auctionObject.get("firstBidTime"));
        long duration = tointeger(auctionObject.get("duration"));
        require(firstBidTime == 0 || block_num < (firstBidTime + duration), "Auction expired");
        require(tointeger(amount) >= tointeger(auctionObject.get("reservePrice")), "Must send at least reservePrice");
        require(tointeger(amount) >= (tointeger(auctionObject.get("amount")) + tointeger(auctionObject.get("minDeltaPrice"))),
                "Must send no less than last bid by minDeltaPrice amount");
        if (firstBidTime == 0) {
            auctionObject.set("firstBidTime", block_num);
        } else {
            withdrawNativeAssetPrivate(self, lastBidder, tostring(auctionObject.get("symbol")), tostring(auctionObject.get("amount")));
        }
        auctionObject.set("amount", amount);
        auctionObject.set("bidder", fromAddress);
        fast_map_set("auctions", tostring(auctionId), json.dumps(auctionObject));
        boolean extended = false;
        if (firstBidTime + duration - block_num < self.getStorage().timeBuffer) {
            long oldDuration = tointeger(auctionObject.get("duration"));
            long newDuration = oldDuration + tointeger(self.getStorage().timeBuffer) - (firstBidTime + duration) - block_num;
            auctionObject.set("duration", newDuration);
        }
        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("auctionId", tostring(auctionId));
        uvmMap.set("tokenId", auctionObject.get("tokenId"));
        uvmMap.set("tokenContract", auctionObject.get("tokenContract"));
        uvmMap.set("from_addr", fromAddress);
        uvmMap.set("amount", amount);
        uvmMap.set("lastBidder", lastBidder);
        uvmMap.set("extended", extended);
        emit("AuctionBid", json.dumps(uvmMap));

    }


    public final boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public final boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
