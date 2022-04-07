package gjavac.test.ccsource;

import gjavac.lib.*;

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

    public void switchOn(SourceContract self) {
        require(self.getStorage()._switch, "business switch is off!");
    }

    public final void withdrawNativeAssetPrivate(SourceContract self, String from, String symbol, String amountStr) {
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

    public void _releaseAsset(SourceContract self, String _recviceAddress, String _symbol, long _amount) {
        if (self.getStorage().nativeSymbol == _symbol) {
            //check if eought asset to release
            require(tointeger(get_contract_balance_amount(caller_address(), _symbol)) >= _amount, "Insufficient native asset to release!");

            //transfer native asset
            withdrawNativeAssetPrivate(self, _recviceAddress, _symbol, tostring(_amount));
        } else {
            //get token contract address by symbol
            String tokenContract = tostring(self.getStorage().tokenContracts.get(_symbol));
            require(is_valid_address(tokenContract), "Symbol hasn't registered.");

            MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenContract);

            //check if eought asset to release
            require(multiOwnedContractSimpleInterface.balanceOf(get_from_address()) >= _amount, "Insufficient token balance to release!");

            // Transfer token
            multiOwnedContractSimpleInterface.safeTransfer(_recviceAddress, _amount);
        }

    }

    public void onlyWitness(SourceContract self) {
        require(caller_address() == self.getStorage().witness, "CCBase: caller is not the witness!");
    }

    public void check_caller_frame_valid(SourceContract sourceContract) {

    }

    public void checkAdmin(SourceContract self) {
        String fromAddr = get_from_address();
        if (self.getStorage().owner != fromAddr) {
            UvmCoreLibs.error("you are not admin, can't call this function");
        }
    }

    public void checkState(SourceContract self) {
        String state = self.getStorage().state;
        if (state == this.NOT_INITED()) {
            UvmCoreLibs.error("contract token not inited");
        } else if (state == this.PAUSED()) {
            UvmCoreLibs.error("contract paused");
        } else if (state == this.STOPPED()) {
            UvmCoreLibs.error("contract stopped");
        }
    }

    public void _lockNativeAsset(SourceContract self, String remoteAddress, String symbol, long amount) {
        require(remoteAddress.length() == self.getStorage().VALID_REMOTE_ADDRESS_LENGTH, "Invalid length of remote address!");
        require(amount > 0, "Can't lock 0 amount!");

        // Make sure there is some native asset to lock
        String from_address = get_from_address();
        if (from_address != caller_address()) {
            error("only common user account can lock balance");
        }

        long balance = get_contract_balance_amount(get_current_contract_address(), symbol);
        require(tointeger(balance) <= self.getStorage().nativeCap, "Exceed native asset lock limit!");
        self.getStorage().eventNonce = self.getStorage().eventNonce + 1;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", self.getStorage().eventNonce);
        uvmMap.set("amount", amount);
        uvmMap.set("symbol", symbol);
        uvmMap.set("localAddress", get_from_address());
        uvmMap.set("remoteAddress", remoteAddress);
        emit("AssetLocked", tojsonstring(uvmMap));
    }

    public final boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public final boolean isNotBlank(String str) {
        return !isBlank(str);
    }

}
