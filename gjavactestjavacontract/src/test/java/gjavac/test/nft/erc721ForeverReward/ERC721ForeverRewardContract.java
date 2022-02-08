package gjavac.test.nft.erc721ForeverReward;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/17 15:46
 */
@Contract(storage = ERC721ForeverRewardStorage.class)
public class ERC721ForeverRewardContract extends UvmContract<ERC721ForeverRewardStorage> {

    @Override
    public void init() {
        print("erc721 contract creating");
        this.getStorage().name = "";
        this.getStorage().symbol = "";
        this.getStorage().state = "NOT_INITED";
        this.getStorage().admin = caller_address();
        this.getStorage().allTokenCount = 0;
        this.getStorage().fixedSellContract = "";
    }

    public void init_token(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        if (this.getStorage().getState() != utils.NOT_INITED()) {
            error("this token contract inited before");
            return;
        }

        UvmArray<String> parsed = utils.parseArgs(arg, 2, "argument format error, need format: name,symbol");
        String name = parsed.get(1);
        String symbol = parsed.get(2);
        if (name == null) {
            error("name needed");
            return;
        }

        if (symbol == null) {
            error("symbol needed");
            return;
        }
        this.getStorage().name = name;
        this.getStorage().symbol = symbol;
        String fromAddress = utils.getFromAddress();
        if (fromAddress != caller_address()) {
            error("init_token can't be called from other contract");
            return;
        }

        this.getStorage().state = utils.COMMON();
        UvmMap<String> uvmMap = new UvmMap<>();
        uvmMap.set("name", name);
        uvmMap.set("symbol", symbol);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        emit("Inited", json.dumps(uvmMap));
    }


    public void approve(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: to,tokenId");
        String to = parsed.get(1);
        String tokenId = parsed.get(2);
        Object owner = utils._ownerOf(tokenId);
        if (to == tostring(owner)) {
            error("ERC721: approval to current owner");
            return;
        }
        utils.require(tostring(owner) == utils.getFromAddress()
                || utils.isApprovedForAll(tostring(owner), utils.getFromAddress()), "ERC721: approve caller is not owner nor approved for all" + owner + utils.getFromAddress());
        utils._approve(to, tokenId);
    }

    public void setApprovalForAll(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: operator,approved");
        String operator = parsed.get(1);
        String approvedStr = parsed.get(2);
        boolean approved = false;
        if (approvedStr == "true") {
            approved = true;
        }
        String fromAddress = utils.getFromAddress();
        utils.require(operator != fromAddress, "ERC721: approve to caller");
        Object operatorApprovals = fast_map_get("_operatorApprovals", fromAddress);
        String data = "{}";
        if (operatorApprovals != null) {
            data = tostring(operatorApprovals);
        }
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap<Object> json_data = (UvmMap<Object>) totable(json.loads(data));
        json_data.set(operator, approved);
        fast_map_set("_operatorApprovals", fromAddress, json.dumps(json_data));
        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("owner", fromAddress);
        uvmMap.set("operator", operator);
        uvmMap.set("approved", approved);
        emit("ApprovalForAll", json.dumps(uvmMap));
    }

    public void transferFrom(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseAtLeastArgs(args, 3, "argument format error, need format: from,to,tokenId");
        String from = parsed.get(1);
        String to = parsed.get(2);
        String tokenId = parsed.get(3);
        String fromAddress = utils.getFromAddress();
        utils.require(utils._isApprovedOrOwner(fromAddress, tokenId), "ERC721: transfer caller is not owner nor approved" + fromAddress);
        utils._transfer(this, from, to, tokenId);
    }

    public final void safeTransferFrom(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseAtLeastArgs(args, 3, "argument format error, need format: from,to,tokenId,data(optional)");
        final String from = parsed.get(1);
        final String to = parsed.get(2);
        final String tokenId = parsed.get(3);
        String fromAddress = utils.getFromAddress();
        String data = "";
        if (parsed.size() > 3) {
            data = parsed.get(4);
        }
        if (is_valid_address(to)) {
            utils.require(utils._isApprovedOrOwner(fromAddress, tokenId), "ERC721: transfer caller is not owner nor approved" + fromAddress);
            utils._transfer(this, from, to, tokenId);
            utils._checkOnERC721Received(from, to, tokenId, data);
        } else {
            utils.require(utils._isApprovedOrOwner(fromAddress, tokenId), "ERC721: transfer caller is not owner nor approved");
            utils._transfer(this, from, to, tokenId);
        }
    }

    public void safeMint(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseArgs(args, 3, "argument format error, need format: to,tokenId,feeRate");
        String to = parsed.get(1);
        String data = parsed.get(2);
        long feeRate = tointeger(parsed.get(3));
        utils._safeMint(this, to, data, "", feeRate);
    }

    public void mint(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseArgs(args, 3, "argument format error, need format: to,tokenId,feeRate");
        String to = parsed.get(1);
        String data = parsed.get(2);
        long feeRate = tointeger(parsed.get(3));
        utils._mint(this, to, data, feeRate);
    }

    public String tokenOfOwnerByIndex(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: owner,index");
        String owner = parsed.get(1);
        String index = parsed.get(2);
        String amount = balanceOf(owner);
        utils.require(tointeger(index) <= tointeger(amount), "ERC721Enumerable: owner index out of bounds");
        Object data = fast_map_get("_ownedTokens", owner);
        if (data == null) {
            error("unkown data error");
            return "";
        }
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmArray totable = (UvmArray) totable(json.loads(tostring(data)));
        return tostring(totable.get((int) tointeger(index)));
    }

    public void setAuctionContract(String addr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (is_valid_address(addr)) {
            this.getStorage().auctionContract = addr;
        } else {
            error("Illegal auction contract address");
        }
    }

    public void changeAdmin(String addr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (is_valid_address(addr)) {
            this.getStorage().admin = addr;
        } else {
            error("Illegal admin address");
        }
    }

    public void setFixedSellContract(String addr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (is_valid_address(addr)) {
            this.getStorage().fixedSellContract = addr;
        } else {
            error("Illegal auction contract address");
        }
    }

    public void on_deposit_asset(String args) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap arg = (UvmMap) totable(json.loads(args));
        long amount = tointeger(arg.get("num"));
        String symbol = tostring(arg.get("symbol"));
        String param = tostring(arg.get("param"));
        if (amount < 0) {
            error("deposit should greater than 0");
            return;
        }
        if (symbol.length() < 1) {
            error("on_deposit_asset arg wrong");
        }

        String fromAddress = utils.getFromAddress();
        UvmMap userAssets = (UvmMap) totable(json.loads(tostring(fast_map_get("_nativeBalances", fromAddress))));
        long oldBalance = tointeger(userAssets.get(symbol));
        long newBalance = oldBalance + amount;
        userAssets.set(symbol, newBalance);
        String newUserAssetsStr = json.dumps(userAssets);
        fast_map_set("_nativeBalances", fromAddress, newUserAssetsStr);
        UvmMap<Object> uvmMap = UvmMap.create();
        uvmMap.set("address", fromAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("change", amount);
        uvmMap.set("reason", "deposit");
        emit("NativeBalanceChange", json.dumps(uvmMap));
        if (param.length() > 0) {
            UvmStringModule stringModule = importModule(UvmStringModule.class, "string");
            UvmArray<String> parsed = stringModule.split(param, ",");
            if (parsed.size() >= 3) {
                transferFrom(param);
            }
        }
    }

    public void withdrawAsset(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: symbol,amount");
        String fromAddress = utils.getFromAddress();
        String symbol = parsed.get(1);
        String amount = parsed.get(2);
        utils.withdrawNativeAssetPrivate(this, fromAddress, symbol, amount);
    }

    public final void feedTradePrice(String args) {
        Utils utils = new Utils();
        UvmArray<String> stringUvmArray = utils.parseArgs(args, 3, "argument format error, need format: tokenId,symbol,amount");
        final String tokenId = stringUvmArray.get(1);
        final String symbol = stringUvmArray.get(2);
        final long amount = tointeger(stringUvmArray.get(3));
        final String fromAddress = utils.getFromAddress();
        utils.require(fromAddress == this.getStorage().fixedSellContract, "This interface does not allow external calls");
        utils.require(utils._ownerOf(tokenId) != "", "A token that does not exist cannot be feed");
        fast_map_set("_allTokenTradePrice", tokenId, symbol + ',' + tostring(amount));
    }

    @Offline
    public UvmArray queryLastTradePrice(String tokenId) {
        Utils utils = new Utils();
        return utils.getTradePrice(tokenId);
    }

    @Offline
    public final String queryTokenMinter(String tokenId) {
        Object allTokenMinter = fast_map_get("_allTokenMinter", tokenId);
        String minter = "";
        if (allTokenMinter != null) {
            minter = tostring(allTokenMinter);
        }
        Object allTokenMintFee = fast_map_get("_allTokenMintFee", tokenId);
        long fee = 0;
        if (allTokenMintFee != null) {
            fee = tointeger(allTokenMintFee);
        }
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap<Object> uvmMap = new UvmMap<>();
        uvmMap.set("minter", minter);
        uvmMap.set("fee", fee);
        return json.dumps(uvmMap);
    }

    @Offline
    public UvmMap queryUserAssets(String address) {
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        return (UvmMap) json.loads(tostring(fast_map_get("_nativeBalances", address)));
    }

    @Offline
    public long totalSupply() {
        Utils utils = new Utils();
        utils.checkState(this);
        return this.getStorage().allTokenCount;
    }

    @Offline
    public Object tokenByIndex(String index) {
        Utils utils = new Utils();
        utils.checkState(this);
        utils.require(tointeger(index) < this.getStorage().allTokenCount, "ERC721Enumerable: global index out of bounds");
        return fast_map_get("_allTokens", index);
    }

    @Offline
    public static boolean supportsERC721Interface() {
        return true;
    }

    @Offline
    public String balanceOf(String owner) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        if (owner == null || owner.length() < 1) {
            error("arg error, need owner address as argument");
            return null;
        }
        utils.checkAddress(owner);
        return fast_map_get("_balances", owner) == null ? "0" : tostring(fast_map_get("_balances", owner));
    }

    @Offline
    public String tokenName(String args) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().name;
    }

    @Offline
    public String ownerOf(String tokenId) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        Object owners = fast_map_get("_owners", tokenId);
        if (owners == null) {
            error("ERC721: owner query for nonexistent token");
            return "";
        }
        return tostring(owners);
    }


    @Offline
    public String symbol(String args) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().symbol;
    }

    @Offline
    public String tokenURI(String tokenId) {
        Utils utils = new Utils();
        Object token = fast_map_get("_owners", tokenId);
        if (token == null) {
            error("ERC721Metadata: URI query for nonexistent token");
            return "";
        }
        return utils.baseUri() + tokenId;
    }

    @Offline
    public String getApproved(String tokenId) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        utils.require(utils._ownerOf(tokenId) != "", "ERC721: approved query for nonexistent token");
        Object tokenApprovals = fast_map_get("_tokenApprovals", tokenId);
        if (tokenApprovals == null) {
            return "";
        }
        return tostring(tokenApprovals);
    }

    @Offline
    public boolean isApprovedForAll(String args) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        UvmArray<String> parsed = utils.parseArgs(args, 2, "argument format error, need format: owner,operator");
        String owner = parsed.get(1);
        String operator = parsed.get(2);
        return utils.isApprovedForAll(owner, operator);
    }

}
