package gjavac.test.cctarget;

import gjavac.lib.*;
import gjavac.test.ccsource.MultiOwnedContractSimpleInterface;
import gjavac.test.ccsource.SourceContract;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/21 14:34
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

    public UvmArray<String> parseArgs(String arg, int count, String errorMsg) {
        if (arg == null) {
            UvmCoreLibs.error(errorMsg);
            return UvmArray.create();
        } else {
            UvmStringModule stringModule = (UvmStringModule) UvmCoreLibs.importModule(UvmStringModule.class, "string");
            UvmArray parsedArray = stringModule.split(arg, ",");
            if (parsedArray != null && parsedArray.size() == count) {
                return parsedArray;
            } else {
                UvmCoreLibs.error(errorMsg);
                return UvmArray.create();
            }
        }
    }

    public final boolean require(boolean success, String text) {
        if (success)
            return true;
        error(text);
        return false;
    }

    public String get_from_address() {
        String prev_contract_id = UvmCoreLibs.get_prev_call_frame_contract_address();
        String fromAddress;
        if (prev_contract_id != null && UvmCoreLibs.is_valid_contract_address(prev_contract_id)) {
            fromAddress = prev_contract_id;
        } else {
            fromAddress = UvmCoreLibs.caller_address();
        }
        return fromAddress;
    }

    public void switchOn(TargetContract self) {
        require(self.getStorage()._switch, "business switch is off!");
    }

    public final void withdrawNativeAssetPrivate(TargetContract self, String from, String symbol, String amountStr) {
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
        emit("NativeBalanceChange", tojsonstring(uvmMap));
    }


    public void onlyWitness(TargetContract self) {
        require(caller_address() == self.getStorage().witness, "CCBase: caller is not the witness!");
    }

    public void check_caller_frame_valid(TargetContract sourceContract) {

    }

    public void checkAdmin(TargetContract self) {
        String fromAddr = get_from_address();
        if (self.getStorage().owner != fromAddr) {
            UvmCoreLibs.error("you are not admin, can't call this function");
        }
    }

    public void checkState(TargetContract self) {
        String state = self.getStorage().state;
        if (state == this.NOT_INITED()) {
            UvmCoreLibs.error("contract token not inited");
        } else if (state == this.PAUSED()) {
            UvmCoreLibs.error("contract paused");
        } else if (state == this.STOPPED()) {
            UvmCoreLibs.error("contract stopped");
        }
    }

    public final boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public final boolean isNotBlank(String str) {
        return !isBlank(str);
    }

}
