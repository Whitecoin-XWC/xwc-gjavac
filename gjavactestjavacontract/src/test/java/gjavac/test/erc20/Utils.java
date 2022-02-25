package gjavac.test.erc20;

import gjavac.lib.*;
import kotlin.Pair;

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

    public final void checkAdmin(ERC20Contract self) {
        String fromAddr = get_from_address();
        if (self.getStorage().admin != fromAddr) {
            UvmCoreLibs.error("you are not admin, can't call this function");
        }
    }

    public final void check_caller_frame_valid(ERC20Contract self) {
        return;
    }


    public UvmArray<String> parse_args(String arg, int count, String errorMsg) {
        UvmArray var10000;
        if (arg == null) {
            UvmCoreLibs.error(errorMsg);
            var10000 = UvmArray.create();
            return var10000;
        } else {
            UvmStringModule stringModule = (UvmStringModule) UvmCoreLibs.importModule(UvmStringModule.class, "string");
            UvmArray parsed = stringModule.split(arg, ",");
            if (parsed != null && parsed.size() == count) {
                return parsed;
            } else {
                UvmCoreLibs.error(errorMsg);
                var10000 = UvmArray.create();
                return var10000;
            }
        }
    }


    public UvmArray<String> parse_at_least_args(String arg, int count, String errorMsg) {
        UvmArray var10000;
        if (arg == null) {
            UvmCoreLibs.error(errorMsg);
            var10000 = UvmArray.create();
            return var10000;
        } else {
            UvmStringModule stringModule = (UvmStringModule) UvmCoreLibs.importModule(UvmStringModule.class, "string");
            UvmArray parsed = stringModule.split(arg, ",");
            if (parsed != null && parsed.size() >= count) {
                return parsed;
            } else {
                UvmCoreLibs.error(errorMsg);
                return UvmArray.create();
            }
        }
    }

    public boolean arrayContains(UvmArray col, Object item) {
        if (col != null && item != null) {
            ArrayIterator colIter = col.ipairs();
            for (Pair colKeyValuePair = (Pair) colIter.invoke(col, 0); colKeyValuePair.getFirst() != null; colKeyValuePair = (Pair) colIter.invoke(col, colKeyValuePair.getFirst())) {
                if (colKeyValuePair != null && colKeyValuePair.getSecond() == item) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }


    public void checkState(ERC20Contract self) {
        String state = self.getStorage().getState();
        if (state == this.NOT_INITED()) {
            UvmCoreLibs.error("contract token not inited");
        } else if (state == this.PAUSED()) {
            UvmCoreLibs.error("contract paused");
        } else if (state == this.STOPPED()) {
            UvmCoreLibs.error("contract stopped");
        }
    }

    public void checkStateInited(ERC20Contract self) {
        if (self.getStorage().state == this.NOT_INITED()) {
            UvmCoreLibs.error("contract token not inited");
        }
        return;
    }

    public boolean checkAddress(String addr) {
        boolean result = UvmCoreLibs.is_valid_address(addr);
        if (!result) {
            UvmCoreLibs.error("address format error");
            return false;
        } else {
            return true;
        }
    }


    public String getBalanceOfUser(ERC20Contract self, String addr) {
        Object balance = UvmCoreLibs.fast_map_get("users", addr);
        if (balance == null) {
            return "0";
        } else {
            return UvmCoreLibs.tostring(balance);
        }
    }

}