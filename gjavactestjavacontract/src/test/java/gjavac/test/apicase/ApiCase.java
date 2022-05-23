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

    @Offline
    public String test_get_transaction_id() {
        return get_transaction_id();
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

    @Offline
    public int test_wait_for_future_random(int blockHeight) {
        return wait_for_future_random(blockHeight);
    }

    @Offline
    public UvmTable test_hex_to_bytes(String args) {
        return hex_to_bytes(args);
    }

    @Offline
    public String test_bytes_to_hex(String args) {
        return bytes_to_hex(hex_to_bytes(args));
    }

    @Offline
    public String test_sha1_hex(String arg) {
        return sha1_hex(arg);
    }

    @Offline
    public String test_sha3_hex(String arg) {
        return sha3_hex(arg);
    }

    @Offline
    public String test_sha256_hex(String arg) {
        return sha256_hex(arg);
    }

    @Offline
    public String test_ripemd160_hex(String arg) {
        return ripemd160_hex(arg);
    }

    @Offline
    public String test_cbor_encode(String arg) {
        return cbor_encode(arg);
    }

    @Offline
    public Object test_cbor_decode(String arg) {
        return cbor_decode(arg);
    }

    @Offline
    public String test_signature_recover(String args) {
        Utils utils = new Utils();
        UvmArray<String> parse = utils.parseArgs(args, 2, "need 2 params");
        return signature_recover(parse.get(1), parse.get(2));
    }
}
