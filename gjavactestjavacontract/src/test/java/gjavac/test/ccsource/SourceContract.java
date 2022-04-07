package gjavac.test.ccsource;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;
import static gjavac.lib.UvmCoreLibs.importContractFromAddress;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/21 14:28
 */
@Contract(storage = Storage.class)
public class SourceContract extends UvmContract<Storage> {
    @Override
    public void init() {
        Storage storage = this.getStorage();
        storage.state = "NOT_INITED";
        storage.owner = caller_address();
        storage.witness = caller_address();
        storage.monitor = caller_address();
        storage._switch = true;
        storage.lastHandledNonce = 0L;
        storage.eventNonce = 0L;
        storage.nativeSymbol = "XWC";
        storage.nativeCap = 10000000 * 100000000L;
        storage.tokenContracts = UvmMap.create();
        storage.tokenCaps = new UvmMap<>();
        storage.rollbackNonces = new UvmMap<>();
        storage.handledNonces = new UvmMap<>();
    }

    @Offline
    public void setNativeSymbol(String newSymbol) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        this.getStorage().nativeSymbol = newSymbol;
    }

    @Offline
    public String getNativeSymbol() {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        return this.getStorage().nativeSymbol;
    }

    @Offline
    public long getNativeCap() {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        return this.getStorage().nativeCap;
    }

    @Offline
    public String getTokenContract(String symbol) {
        return tostring(fast_map_get("tokenContracts", symbol));
    }

    @Offline
    public Object getTokenCap(String symbol) {
        return tostring(fast_map_get("tokenCaps", symbol));
    }

    @Offline
    public String getContractCurrent() {
        return get_current_contract_address();
    }

    @Offline
    public Object getContractBalances() {
        return get_contract_balance_amount(get_current_contract_address(), "XWC");
    }


    public void init_token() {
        this.getStorage().state = "COMMON";
    }

    public void setNativeCap(long newCap) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        long old_cap = this.getStorage().nativeCap;
        this.getStorage().nativeCap = newCap;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("symbol", this.getStorage().nativeSymbol);
        uvmMap.set("oldCap", old_cap);
        uvmMap.set("newCap", newCap);
        emit("TokenCapChanged", tojsonstring(uvmMap));
    }

    public void setTokenCap(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 2, "need arg symbol,newCap");
        String symbol = parsed.get(1);
        long newCap = tointeger(parsed.get(2));
        Object old_cap = fast_map_get("tokenCaps", symbol);
        fast_map_set("tokenCaps", symbol, newCap);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("symbol", symbol);
        uvmMap.set("oldCap", old_cap);
        uvmMap.set("newCap", newCap);
        emit("TokenCapChanged", tojsonstring(uvmMap));
    }

    public void regTokenContract(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 3, "need symbol,contract,cap");
        String symbol = parsed.get(1);
        String contract = parsed.get(2);
        long cap = tointeger(parsed.get(3));
        utils.require(fast_map_get("tokenContracts", symbol) == null, "Token contract with same symbol has been registered!");
        fast_map_set("tokenContracts", symbol, contract);
        fast_map_set("tokenCaps", symbol, cap);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("symbol", symbol);
        uvmMap.set("contractAddress", contract);
        uvmMap.set("cap", cap);
        emit("TokenRegistered", tojsonstring(uvmMap));
        UvmMap capChangeMap = UvmMap.create();
        capChangeMap.set("symbol", symbol);
        capChangeMap.set("oldCap", 0);
        capChangeMap.set("newCap", cap);
        emit("TokenCapChanged", tojsonstring(capChangeMap));
    }


    // native lock
    public void on_deposit_asset(String args) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.checkState(this);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap arg = (UvmMap) totable(json.loads(args));
        long amount = tointeger(arg.get("num"));
        String symbol = tostring(arg.get("symbol"));
        String remoteAddress = tostring(arg.get("param"));
        utils._lockNativeAsset(this, remoteAddress, symbol, amount);
    }

    public void lockAsset(String arg) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 3, "need remoteAddress,symbol,amount");
        String remoteAddress = parsed.get(1);
        String symbol = parsed.get(2);
        long amount = tointeger(parsed.get(3));
        utils.require(remoteAddress.length() == this.getStorage().VALID_REMOTE_ADDRESS_LENGTH, "Invalid length of remote address!");
        utils.require(amount > 0, "Can't lock 0 amount!");
        utils.require(fast_map_get("tokenContracts", symbol) != null, "Token contract hasn't registered!");
        String tokenContract = tostring(fast_map_get("tokenContracts", symbol));
        utils.require(amount == 0, "Token contract can't receive the native amount");
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenContract);
        utils.require(multiOwnedContractSimpleInterface.balanceOf(utils.get_from_address()) >= amount, "Insufficient balance!");
        utils.require(multiOwnedContractSimpleInterface.allowance(utils.get_from_address(), caller_address()) >= amount, "Insufficient allowance!");
        Object tokenCaps = fast_map_get("tokenCaps", symbol);
        utils.require(multiOwnedContractSimpleInterface.balanceOf(caller_address()) + amount <= tointeger(tokenCaps), "Exceed token lock limit!");
        multiOwnedContractSimpleInterface.safeTransferFrom(utils.get_from_address(), caller_address(), amount);
        this.getStorage().eventNonce = this.getStorage().eventNonce + 1;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", this.getStorage().eventNonce);
        uvmMap.set("localAddress", utils.get_from_address());
        uvmMap.set("remoteAddress", remoteAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("amount", amount);
        emit("AssetLocked", tojsonstring(uvmMap));
    }

    public void releaseAsset(String arg) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.onlyWitness(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 5, "need eventNonce,localAddress,remoteAddress,symbol and amount");
        String eventNonce = parsed.get(1);
        utils.require(!toboolean(fast_map_get("handledNonces", eventNonce)), "Event was already handled!");
        String localAddress = parsed.get(2);
        String remoteAddress = parsed.get(3);
        String symbol = parsed.get(4);
        long amount = tointeger(parsed.get(5));
        utils._releaseAsset(this, localAddress, symbol, amount);
        this.getStorage().lastHandledNonce = tointeger(eventNonce);
        fast_map_set("handledNonces", eventNonce, true);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", eventNonce);
        uvmMap.set("localAddress", localAddress);
        uvmMap.set("remoteAddress", remoteAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("amount", amount);
        emit("AssetReleased", tojsonstring(uvmMap));
    }

    public void rollBackToken(String arg) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.onlyWitness(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 4, "need eventNonce,receiveAddress,symbol and amount");
        String eventNonce = parsed.get(1);
        utils.require(!toboolean(fast_map_get("rollbackNonces", eventNonce)), "Event was already handled!");
        String receiveAddress = parsed.get(2);
        String symbol = parsed.get(3);
        long amount = tointeger(parsed.get(4));
        utils._releaseAsset(this, receiveAddress, symbol, amount);
        fast_map_set("rollbackNonces", eventNonce, true);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", eventNonce);
        uvmMap.set("receiveAddress", receiveAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("amount", amount);
        emit("AssetLockRollback", tojsonstring(uvmMap));
    }

    public void pause() {
        Utils utils = new Utils();
        utils.check_caller_frame_valid(this);
        if (this.getStorage().state == "STOPPED") {
            error("this contract stopped now, can't pause");
        }
        if (this.getStorage().state == "PAUSED") {
            error("this contract paused now, can't pause");
        }
        utils.checkAdmin(this);
        this.getStorage().state = "PAUSED";
        emit("Paused", "");
    }

    public void resume() {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        if (this.getStorage().state != "PAUSED") {
            error("this contract not paused now, can't resume");
        }
        utils.checkAdmin(this);
        this.getStorage().state = "COMMON";
        emit("Resumed", "");
    }

    public void stop() {
        Utils utils = new Utils();
        utils.check_caller_frame_valid(this);
        if (this.getStorage().state == "STOPPED") {
            error("this contract stopped now, can't stop");
        }
        if (this.getStorage().state == "PAUSED") {
            error("this contract paused now, can't stop");
        }
        utils.checkAdmin(this);
        this.getStorage().state = "STOPPED";
        emit("Stopped", "");
    }
}
