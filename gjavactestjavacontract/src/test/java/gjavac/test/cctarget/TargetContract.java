package gjavac.test.cctarget;

import gjavac.lib.*;

import java.rmi.Remote;

import static gjavac.lib.UvmCoreLibs.*;
import static gjavac.lib.UvmCoreLibs.caller_address;

/**
 * Description: gjavac
 * Created by moloq on 2022/4/1 9:19
 */
@Contract(storage = Storage.class)
public class TargetContract extends UvmContract<Storage> {
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
        storage.tokenContracts = UvmMap.create();
        storage.tokenCaps = UvmMap.create();
        storage.rollbackNonces = new UvmMap<>();
        storage.tokenFees = UvmMap.create();
        storage.allSymbols = UvmArray.create();
        storage.handledNonces = new UvmMap<>();
    }

    @Offline
    public String getTokenContract(String symbol) {
        return tostring(fast_map_get("tokenContracts", symbol));
    }

    @Offline
    public long getTokenCap(String symbol) {
        return tointeger(fast_map_get("tokenCaps", symbol));
    }

    @Offline
    public long getTokenFee(String symbol) {
        return tointeger(fast_map_get("tokenFees", symbol));
    }

    public void createTokenContract(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 5, "need params name,symbol,address,decimals,cap");
        String name = parseArgs.get(1);
        utils.require(name.length() > 0 && name.length() < 20, "Name can't be null or longer than 19 string!");
        String symbol = parseArgs.get(2);
        utils.require(symbol.length() > 0 && symbol.length() < 10, "Symbol can't be null or longer than 9 string!");
        utils.require(fast_map_get("tokenContracts", symbol) == null, "Token contract with same symbol has been created!");
        String address = parseArgs.get(3);
        fast_map_set("tokenContracts", symbol, address);
        long decimals = tointeger(parseArgs.get(4));
        long cap = tointeger(parseArgs.get(5));
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("name", name);
        uvmMap.set("symbol", symbol);
        uvmMap.set("decimals", decimals);
        uvmMap.set("cap", cap);
        this.getStorage().allSymbols.add(symbol);
        emit("TokenCreated", tojsonstring(uvmMap));
        UvmMap uvmMap1 = new UvmMap();
        uvmMap1.set("symbol", symbol);
        uvmMap1.set("oldCap", 0);
        uvmMap1.set("newCap", cap);
        fast_map_set("tokenCaps", symbol, cap);
        emit("TokenCapChanged", tojsonstring(uvmMap1));
    }

    public void setTokenCap(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 2, "need params symbol,cap");
        String symbol = parseArgs.get(1);
        long oldCap = tointeger(fast_map_get("tokenCaps", symbol));
        long newCap = tointeger(parseArgs.get(2));
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("symbol", symbol);
        uvmMap.set("oldCap", oldCap);
        uvmMap.set("newCap", newCap);
        emit("TokenCapChanged", tojsonstring(uvmMap));
    }

    public void setTokenFee(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 2, "need params symbol,fees");
        String symbol = parseArgs.get(1);
        long fee = tointeger(parseArgs.get(2));
        fast_map_set("tokenFees", symbol, fee);
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("symbol", symbol);
        uvmMap.set("fee", fee);
        emit("TokenFeeSetted", tojsonstring(uvmMap));
    }

    public void redeemToken(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.switchOn(this);
        utils.onlyWitness(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 3, "need params remoteAddress,symbol,amount");
        String remoteAddress = parseArgs.get(1);
        String symbol = parseArgs.get(2);
        long amount = tointeger(parseArgs.get(3));
        String contractAddres = tostring(fast_map_get("tokenContracts", symbol));
        utils.require(contractAddres != null, "Token contract hasn't created!");
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, contractAddres);
        utils.require(multiOwnedContractSimpleInterface.balanceOf(utils.get_from_address()) >= amount, "Insufficient balance!");
        long redeemFee = tointeger(fast_map_get("tokenFees", symbol));
        utils.require(amount > redeemFee, "redeem amount less than redeemFee!");
        multiOwnedContractSimpleInterface.burnFrom(utils.get_from_address(), amount);
        utils.require(multiOwnedContractSimpleInterface.mintTo(get_current_contract_address(), redeemFee), "Mint failed unexpectedly!");
        this.getStorage().eventNonce = this.getStorage().eventNonce + 1;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", this.getStorage().eventNonce);
        uvmMap.set("localAddress", utils.get_from_address());
        uvmMap.set("remoteAddress", remoteAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("amount", amount - redeemFee);
        uvmMap.set("fee", redeemFee);
        emit("AssetRedeemed", tojsonstring(uvmMap));
    }

    public void mintToken(String args) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.onlyWitness(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 5, "need params eventnonce,localAddress,remoteAddress,symbol,amount");
        long eventNonce = tointeger(parseArgs.get(1));
        utils.require(!toboolean(fast_map_get("handledNonces", tostring(eventNonce))), "Event was already handled!");
        String symbol = parseArgs.get(4);
        String contractAddres = tostring(fast_map_get("tokenContracts", symbol));
        utils.require(contractAddres != null, "Token contract hasn't created!");
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, contractAddres);
        String localAddress = parseArgs.get(2);
        long amount = tointeger(parseArgs.get(3));
        utils.require(multiOwnedContractSimpleInterface.mintTo(localAddress, amount), "Mint failed unexpectedly!");
        this.getStorage().lastHandledNonce = eventNonce;
        fast_map_set("handledNonces", tostring(eventNonce), true);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", eventNonce);
        uvmMap.set("localAddress", localAddress);
        uvmMap.set("remoteAddress", parseArgs.get(3));
        uvmMap.set("symbol", symbol);
        uvmMap.set("amount", amount);
        emit("AssetMinted", tojsonstring(uvmMap));
    }

    public void withdrawFee(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 3, "need params _recviceAddress,_symbol,_amount");
        String symbol = parseArgs.get(2);
        String contractAddres = tostring(fast_map_get("tokenContracts", symbol));
        utils.require(contractAddres != null, "Token contract hasn't created!");
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, contractAddres);
        long amount = tointeger(parseArgs.get(3));
        utils.require(multiOwnedContractSimpleInterface.balanceOf(get_current_contract_address()) >= amount, "total redeemFee less than can transfer amount!");
        String recviceAddress = parseArgs.get(1);
        multiOwnedContractSimpleInterface.transfer(recviceAddress, amount);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("recviceAddress", recviceAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("amount", amount);
        emit("TokenFeeTransfer", tojsonstring(uvmMap));
    }

    public void rollBackToken(String args) {
        Utils utils = new Utils();
        utils.onlyWitness(this);
        utils.switchOn(this);
        UvmArray<String> parseArgs = utils.parseArgs(args, 5, "need params _eventNonce,_recviceAddress,_symbol,_amount,_fee");
        String contractAddres = tostring(fast_map_get("tokenContracts", parseArgs.get(3)));
        utils.require(contractAddres != null, "Token contract hasn't created!");
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, contractAddres);
        String recviceAddress = parseArgs.get(2);
        long amount = tointeger(parseArgs.get(4));
        utils.require(multiOwnedContractSimpleInterface.mintTo(recviceAddress, amount), "Mint failed unexpectedly!");
        long fee = tointeger(parseArgs.get(5));
        utils.require(multiOwnedContractSimpleInterface.balanceOf(get_current_contract_address()) >= fee, "redeemFee can not more than contract balance!");
        multiOwnedContractSimpleInterface.transfer(recviceAddress, fee);
        long eventNonce = tointeger(parseArgs.get(1));
        fast_map_set("rollbackNonces", tostring(eventNonce), true);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nonce", eventNonce);
        uvmMap.set("recviceAddress", recviceAddress);
        uvmMap.set("symbol", parseArgs.get(3));
        uvmMap.set("amount", amount);
        uvmMap.set("redeemFee", fee);
        emit("AssetRedeemRollback", tojsonstring(uvmMap));
    }
}
