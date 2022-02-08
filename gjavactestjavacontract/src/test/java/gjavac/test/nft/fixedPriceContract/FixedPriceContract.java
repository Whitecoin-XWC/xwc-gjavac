package gjavac.test.nft.fixedPriceContract;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;


/**
 * Description: DemoContract
 * Created by moloq on 2021/11/17 13:51
 */
@Contract(storage = Storage.class)
public class FixedPriceContract extends UvmContract<Storage> {

    @Override
    public void init() {
        print("fixed price contract creating");
        this.getStorage().state = "COMMON";
        this.getStorage().admin = caller_address();
        this.getStorage().feeRate = 5L;
        this.getStorage().totalReward = new UvmMap<>();
        this.getStorage().currentRReward = new UvmMap<>();
    }

    public final void _buyNft(String tokenAddr, String tokenId, String symbol, String amount) {
        Utils utils = new Utils();
        final String fromAddress = utils.getFromAddress();
        final String tokenIdx = tokenAddr + "." + tokenId;
        String tokenInfoStr = (String) fast_map_get("token_list", tokenIdx);
        if (tokenInfoStr == null) {
            tokenInfoStr = "{}";
        }
        utils.require(tokenInfoStr != "{}", "token with Id not in sell list");
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap<String> tokenInfo = (UvmMap<String>) totable(json.loads(tokenInfoStr));
        long amountInt = tointeger(amount);
        utils.require(tokenInfo.get(symbol) == symbol, "token sell in different symbol");
        utils.require(tointeger(tokenInfo.get("price")) <= amountInt, "not match price");
        MultiOwnedContractSimpleInterface object = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenAddr);
        utils.require(object.supportsERC721Interface(), "tokenContract does not support ERC721 interface");
        UvmMap<Object> tokenData = (UvmMap<Object>) json.loads(object.queryTokenMinter(tokenId));
        final long copyRightFeeRate = tointeger(tokenData.get("fee"));
        final String tokenMinter = tostring(tokenData.get("minter"));
        final long closePrice = tointeger(tokenInfo.get("price"));

        final long writePrice = tointeger(closePrice - tointeger(closePrice * this.getStorage().feeRate / 100));
        final long copyRightFee = tointeger(writePrice * copyRightFeeRate / 100);
        final long payValue = writePrice - copyRightFee;
        utils.updateReward(this, closePrice - writePrice, symbol);
        utils.withdrawNativeAssetPrivate(this, tokenInfo.get("tokenOwner"), tokenInfo.get("symbol"), tostring(payValue));
        if (copyRightFee > 0) {
            utils.withdrawNativeAssetPrivate(this, tokenMinter, tokenInfo.get("symbol"), tostring(copyRightFee));
        }
        fast_map_set("token_list", tokenIdx, "{}");
        Object tokensStr = fast_map_get("user_tokens", tokenInfo.get("tokenOwner"));
        if (tokensStr == null) {
            tokensStr = "[]";
        }
        UvmArray<String> userTokens = (UvmArray<String>) totable(json.loads(tostring(tokensStr)));
        long idx = tointeger(utils.getArrayIdx(userTokens, tokenIdx));
        utils.require(idx > 0, "token idx not exist in user token list");
        // TODO table.remove(userTokens, idx)
        fast_map_set("user_tokens", tokenInfo.get("tokenOwner"), json.dumps(userTokens));
        final String curContract = get_current_contract_address();
        object.safeTransferFrom(curContract + "," + fromAddress + "," + tokenId);
        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("tokenAddr", tokenAddr);
        uvmMap.set("tokenId", tokenId);
        uvmMap.set("seller", tokenInfo.get("tokenOwner"));
        uvmMap.set("buyer", fromAddress);
        uvmMap.set("payValue", payValue);
        uvmMap.set("copyRightFee", copyRightFee);
        emit("DealEvent", json.dumps(uvmMap));
    }


    @Offline
    public final boolean supportsERC721Interface(String args) {
        return false;
    }

    public String sellNft(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(args, 4, "argument format error, need format: tokenId,tokenAddr,price,symbol");
        String tokenId = parsed.get(1);
        String tokenAddr = parsed.get(2);
        String price = parsed.get(3);
        String symbol = parsed.get(4);
        String fromAddress = utils.getFromAddress();
        utils.checkContractAddress(tokenAddr);
        MultiOwnedContractSimpleInterface erc721Object = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenAddr);
        String owner = erc721Object.ownerOf(tokenId);
        utils.require(owner == fromAddress || owner == erc721Object.getApproved(tokenId), "Caller must be approved or owner for token id");
        utils.require(erc721Object.supportsERC721Interface(), "tokenContract does not support ERC721 interface");
        String curContract = get_current_contract_address();
        erc721Object.transferFrom(owner + "," + curContract + "," + tokenId);
        UvmMap<Object> askData = new UvmMap();
        askData.set("tokenId", tokenId);
        askData.set("tokenContract", tokenAddr);
        askData.set("price", price);
        askData.set("tokenOwner", fromAddress);
        askData.set("symbol", symbol);
        String tokenIdx = tokenAddr + "." + tokenId;
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        fast_map_set("token_list", tokenIdx, json.dumps(askData));
        UvmArray<Object> userTokens = (UvmArray<Object>) totable(json.loads(tostring(fast_map_get("user_tokens", fromAddress))));
        userTokens.add(tokenIdx);
        String userTokensStr = json.dumps(userTokens);
        fast_map_set("user_tokens", fromAddress, userTokensStr);
        UvmMap<Object> eventArg = UvmMap.create();
        eventArg.set("tokenId", tokenId);
        eventArg.set("tokenContract", tokenAddr);
        eventArg.set("price", price);
        eventArg.set("tokenOwner", fromAddress);
        eventArg.set("symbol", symbol);
        emit("AskCreated", json.dumps(eventArg));
        return tokenId;
    }

    public void changeSellParam(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(args, 4, "argument format error, need format: tokenId,tokenAddr,price,symbol");
        String tokenId = parsed.get(1);
        String tokenAddr = parsed.get(2);
        String price = parsed.get(3);
        String symbol = parsed.get(4);
        String fromAddress = utils.getFromAddress();
        String tokenIdx = tokenAddr + "." + tokenId;
        String tokenInfoStr = "{}";
        Object token_list = fast_map_get("token_list", tokenIdx);
        if (token_list != null) {
            tokenInfoStr = tostring(token_list);
        }
        utils.require(tokenInfoStr != "{}", "token with Id not in sell list");
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap tokenInfo = (UvmMap) totable(json.loads(tokenInfoStr));
        utils.require(fromAddress == tokenInfo.get("tokenOwner"), "Change sell praram not from owner");
        tokenInfo.set("symbol", symbol);
        tokenInfo.set("price", price);
        fast_map_set("token_list", tokenIdx, json.dumps(tokenInfo));
        UvmMap<Object> eventArg = UvmMap.create();
        eventArg.set("tokenId", tokenId);
        eventArg.set("tokenContract", tokenAddr);
        eventArg.set("price", price);
        eventArg.set("tokenOwner", fromAddress);
        eventArg.set("symbol", symbol);
        emit("AskChanged", json.dumps(eventArg));
    }

    public void on_deposit_asset(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap arg = (UvmMap) totable(json.loads(args));
        long amount = tointeger(arg.get("num"));
        String symbol = tostring(arg.get("symbol"));
        String param = tostring(arg.get("param"));
        if (amount <= 0) {
            error("deposit should greater than 0");
            return;
        }
        if (symbol == null || symbol.length() < 1) {
            error("on_deposit_asset arg wrong");
            return;
        }

        UvmArray<String> parsed = utils.parseArgs(param, 2, "argument format error, need format: tokenAddr,tokenId");
        String tokenAddr = parsed.get(1);
        utils.checkContractAddress(tokenAddr);
        String tokenId = parsed.get(2);
        _buyNft(tokenAddr, tokenId, symbol, tostring(amount));
    }

    public void setFeeRate(String fee) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        print("fee:" + fee);
        utils.require(tointeger(fee) >= 0 && tointeger(fee) <= 50, "invalid fee rate: " + fee);
        this.getStorage().feeRate = tointeger(fee);
    }

    public void revokeSellNft(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: tokenId,tokenAddr");
        String tokenId = parsed.get(1);
        String tokenAddr = parsed.get(2);
        String fromAddress = utils.getFromAddress();
        utils.checkContractAddress(tokenAddr);
        String tokenIdx = tokenAddr + "." + tokenId;
        Object tokenList = fast_map_get("token_list", tokenIdx);
        String tokenInfoStr = "{}";
        if (tokenList != null) {
            tokenInfoStr = tostring(tokenList);
        }
        utils.require(tokenInfoStr != "{}", "token with Id not in sell list");
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap tokenInfo = (UvmMap) totable(json.loads(tokenInfoStr));
        utils.require(fromAddress == tokenInfo.get("tokenOwner"), "Change sell praram not from owner");
        MultiOwnedContractSimpleInterface erc721Object = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenAddr);
        String owner = erc721Object.ownerOf(tokenId);
        String curContract = get_current_contract_address();
        utils.require(owner == curContract, "Caller must be approved or owner for token id");
        utils.require(erc721Object.supportsERC721Interface(), "tokenContract does not support ERC721 interface");
        erc721Object.transferFrom(curContract + "," + fromAddress + "," + tokenId);
        fast_map_set("token_list", tokenIdx, "{}");
        String tokenStr = "[]";
        Object o = fast_map_get("user_tokens", tostring(tokenInfo.get("tokenOwner")));
        if (o != null) {
            tokenStr = tostring(o);
        }
        UvmArray userTokens = (UvmArray) totable(json.loads(tokenStr));
        long idx = tointeger(utils.getArrayIdx(userTokens, tokenIdx));
        utils.require(idx > 0, "token idx not exist in user token list");
        // TODO table.remove(userTokens, idx)
        fast_map_set("user_tokens", tostring(tokenInfo.get("tokenOwner")), json.dumps(userTokens));
        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("tokenId", tokenId);
        uvmMap.set("tokenContract", tokenAddr);
        uvmMap.set("price", tokenInfo.get("price"));
        uvmMap.set("tokenOwner", tokenInfo.get("tokenOwner"));
        uvmMap.set("symbol", tokenInfo.get("symbol"));
        emit("AskRevoked", json.dumps(uvmMap));
    }

    public void withdrawReward(String args) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: amount,symbol");
        long amount = tointeger(parsed.get(1));
        String symbol = parsed.get(2);
        String fromAddress = utils.getFromAddress();
        utils.require(amount > 0, "amount must positive");
        utils.updateReward(this, 0 - amount, symbol);
        utils.withdrawNativeAssetPrivate(this, fromAddress, symbol, tostring(amount));
        UvmMap<Object> uvmMap = new UvmMap();
        uvmMap.set("amount", amount);
        uvmMap.set("symbol", symbol);
        uvmMap.set("admin", fromAddress);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        emit("AdminWithdrawReward", json.dumps(uvmMap));
    }

    @Offline
    public String getTokenInfo(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: owner,operator");
        String tokenAddr = parsed.get(1);
        String tokenId = parsed.get(2);
        String tokenIdx = tokenAddr + "." + tokenId;
        Object tokenList = fast_map_get("token_list", tokenIdx);
        if (tokenList == null) {
            return "{}";
        }
        return tostring(tokenList);
    }

    @Offline
    public String getSellList() {
        Utils utils = new Utils();
        String fromAddress = utils.getFromAddress();
        Object user_tokens = fast_map_get("user_tokens", fromAddress);
        if (user_tokens == null) {
            return "[]";
        }
        return tostring(user_tokens);
    }

    @Offline
    public String getInfo() {
        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("state", this.getStorage().state);
        uvmMap.set("admin", this.getStorage().admin);
        uvmMap.set("feeRate", this.getStorage().feeRate);
        uvmMap.set("totalReward", this.getStorage().totalReward);
        uvmMap.set("currentReward", this.getStorage().currentRReward);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        return json.dumps(uvmMap);
    }


}
