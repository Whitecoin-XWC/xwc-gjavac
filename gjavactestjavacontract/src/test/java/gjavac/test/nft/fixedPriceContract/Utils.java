package gjavac.test.nft.fixedPriceContract;

import gjavac.lib.Component;
import gjavac.lib.UvmArray;
import gjavac.lib.UvmMap;
import gjavac.lib.UvmStringModule;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/17 14:13
 */
@Component
public class Utils {

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

    public final void checkAdmin(FixedPriceContract self) {
        String fromAddress = getFromAddress();
        if (self.getStorage().admin != fromAddress) {
            error("you are not admin, can't call this function");
        }
    }

    public final void checkState(FixedPriceContract self) {
        String state = self.getStorage().state;
        if (state != COMMON()) {
            error("state error, now state is " + state);
        }
    }

    public final void checkStateInited(FixedPriceContract self) {
        if (self.getStorage().state == NOT_INITED())
            error("contract token not inited");
    }

    public final String ownerOf(String tokenId){
        Object owners = fast_map_get("_owners", tokenId);
        if (owners == null){
            return "";
        }
        return tostring(owners);
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

    public final void updateReward(FixedPriceContract table, Long amount, String symbol) {
        Storage storage = table.getStorage();
        UvmMap<Long> totalReward = storage.getTotalReward();
        UvmMap<Long> currentRReward = storage.getCurrentRReward();
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

    public final int getArrayIdx(UvmArray<String> array, String key) {
        int size = array.size();
        int idx = 1;
        while (idx <= size) {
            if (array.get(idx) == key) {
                return idx;
            }
            idx = idx + 1;
        }
        return 0;
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

    public final void withdrawNativeAssetPrivate(FixedPriceContract self,String from,String symbol,String amountStr){
        checkState(self);
        long amount = tointeger(amountStr);
        if (symbol == null || symbol.length() < 1 || amount < 0){
            error("invalid params");
            return;
        }
        int res1 = transfer_from_contract_to_address(from, symbol, amount);
        if (res1 != 0){
            error("transfer asset " + symbol + " to " + from + " amount:" + tostring(amount) + " error, error code: " + tostring(res1));
        }
    }

    public final boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public final boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
