package gjavac.test.nft.erc721;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/25 9:37
 */
@Contract(storage = ERC721Storage.class)
public class ERC721Contract extends UvmContract<ERC721Storage> {

    UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");

    @Override
    public void init() {
        print("erc721 contract creating");
        this.getStorage().name = "";
        this.getStorage().symbol = "";
        this.getStorage().state = "NOT_INITED";
        this.getStorage().admin = caller_address();
        this.getStorage().allTokenCount = 0L;
        print("erc721 contract created");
    }

    public void initToken(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        pprint("arg" + arg);
        if (this.getStorage().state != utils.NOT_INITED()) {
            error("this token contract inited before");
            return;
        }
        UvmArray<String> parsed = utils.parseArgs(arg, 2, "argument format error, need format: name,symbol");
        String name = parsed.get(1);
        String symbol = parsed.get(2);
        if (utils.isBlank(name)) {
            error("name needed");
            return;
        }
        if (utils.isBlank(symbol)) {
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
        UvmMap uvmMap = new UvmMap();
        uvmMap.set("name", name);
        uvmMap.set("symbol", symbol);
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
                || utils.isApprovedForAll(tostring(owner), utils.getFromAddress()), "ERC721: approve caller is not owner nor approved for all");
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
        UvmMap json_data = (UvmMap) totable(data);
        json_data.set(operator, approved);
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        fast_map_set("_operatorApprovals", fromAddress, json.dumps(json_data));
        UvmMap uvmMap = new UvmMap();
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
        utils.require(utils._isApprovedOrOwner(fromAddress, tokenId), "ERC721: transfer caller is not owner nor approved");
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
            utils.require(utils._isApprovedOrOwner(fromAddress, tokenId), "ERC721: transfer caller is not owner nor approved");
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
