package gjavac.test.apicase;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: gjavac
 * Created by moloq on 2022/1/24 14:15
 */
@Contract(storage = Storage.class)
public class ApiCase extends UvmContract<Storage> {
    @Override
    public void init() {

    }

    public int test_transfer_from_contract_to_address(String address) {
        return transfer_from_contract_to_address(address, get_system_asset_symbol(), get_system_asset_precision());
    }

    public long test_get_contract_balance_amount(String address) {
        return get_contract_balance_amount(address, get_system_asset_symbol());
    }

    public long test_get_chain_now() {
        return get_chain_now();
    }

    public long test_get_chain_random() {
        return get_chain_random();
    }

    public long test_get_header_block_num() {
        return get_header_block_num();
    }

    public String test_get_current_contract_address() {
        return get_current_contract_address();
    }

    public String test_caller() {
        return caller();
    }

    public String test_caller_address() {
        return caller_address();
    }

    public boolean test_is_valid_address(String address) {
        return is_valid_address(address);
    }

    public boolean test_is_valid_contract_address(String address) {
        return is_valid_contract_address(address);
    }

    public long test_get_transaction_fee() {
        return get_transaction_fee();
    }

    public String test_get_transaction_id() {
        //TODO
        return "";
    }

    public long test_transfer_from_contract_to_public_account(String args) {
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parseArgs(args, 2, "need to account name,amount");
        return transfer_from_contract_to_public_account(tostring(parsed.get(1)), get_system_asset_symbol(), tointeger(parsed.get(2)));
    }

    public void test_import_contract(String address) {
        //TODO
//        importContract(address);
    }

    public void test_import_contract_from_address(String address) {
        //TODO
//        importContractFromAddress()
    }

    public String test_get_prev_call_frame_contract_address() {
        return get_prev_call_frame_contract_address();
    }

    public String test_get_prev_call_frame_api_name() {
        return get_prev_call_frame_api_name();
    }

    public int test_get_contract_call_frame_stack_size() {
        return get_contract_call_frame_stack_size();
    }

    public int test_wait_for_future_random(int blockHeight) {
        //TODO
        return 0;
    }

    @Offline
    public String test_hex_to_bytes(String args) {
        //TODO
        UvmTable totable = hex_to_bytes(args);
        return bytes_to_hex(totable);
    }

    public void test_bytes_to_hex() {
        //TODO
    }

    public void test_sha1_hex() {
        //TODO
    }

    public void test_sha3_hex() {
        //TODO
    }

    public void test_sha256_hex() {
        //TODO
    }

    public void test_ripemd160_hex() {
        //TODO
    }

    public void cbor_encode() {
        //TODO
    }

    public void test_cbor_decode() {
        //TODO
    }

    public void test_signature_recover() {
        //TODO
    }
}
