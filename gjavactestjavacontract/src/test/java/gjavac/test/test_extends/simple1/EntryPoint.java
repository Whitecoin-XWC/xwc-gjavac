package gjavac.test.test_extends.simple1;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/17 10:46
 */
public class EntryPoint {
    public UvmContract main() {
        print("hello java");
        DemoContract contract = new DemoContract();
//        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
