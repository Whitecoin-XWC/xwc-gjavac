package gjavac.test;

import gjavac.MainKt;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/*
 *   This class is for the testcases of the Java Smart Contract
 *   The structure is that, the contract source code package should be added in this folder, and a new function
 *   should be added in this class as a junit case.
 * */

public class JavaContractTest {

    private static final String testClassesDir = "target/test-classes";

//    @Test
//    public void testJavaContractCompile() {
//        String class1 = testClassesDir + "/gjavac/test/java/DemoContract";
//        String class2 = testClassesDir + "/gjavac/test/java/Utils";
//        String class3 = testClassesDir + "/gjavac/test/java/MultiOwnedContractSimpleInterface";
//        String class4 = testClassesDir + "/gjavac/test/java/DemoContractEntrypoint";
//        String class5 = testClassesDir + "/gjavac/test/java/Storage";
//
//        String[] classesToCompile = new String[]{class1, class2, class3, class4, class5, "-o", "outputs/java_results"};
//        MainKt.main(classesToCompile);
//    }
//
//    @Test
//    public void testDaiContractCompile() {
//        String class1 = testClassesDir + "/gjavac/test/dai/DaiPriceFeederForCdcContract";
//        String class2 = testClassesDir + "/gjavac/test/dai/Utils";
//        String class3 = testClassesDir + "/gjavac/test/dai/MultiOwnedContractSimpleInterface";
//        String class4 = testClassesDir + "/gjavac/test/dai/DaiPriceFeederForCdcContractEntryPoint";
//        String class5 = testClassesDir + "/gjavac/test/dai/Storage";
//
//        String[] classesToCompile = new String[]{class1, class2, class3, class4, class5, "-o", "outputs/dai_results"};
//        MainKt.main(classesToCompile);
//    }
//
    @Test
    public void testSimpleContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/simple/SimpleContractTest";
        String class2 = testClassesDir + "/gjavac/test/simple/simpleEntryPoint";
        String class3 = testClassesDir + "/gjavac/test/simple/SimpleStorage";
        String[] classesToCompile = new String[]{class1, class2,class3,"-o","outputs/simpleTest_results"};
        MainKt.main(classesToCompile);
    }

    @Test
    public void testCCSourceContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/ccsource/ContractEntrypoint";
        String class2 = testClassesDir + "/gjavac/test/ccsource/MultiOwnedContractSimpleInterface";
        String class3 = testClassesDir + "/gjavac/test/ccsource/SourceContract";
        String class4 = testClassesDir + "/gjavac/test/ccsource/Storage";
        String class5 = testClassesDir + "/gjavac/test/ccsource/Utils";
        String[] classesToCompile = new String[]{class1, class2,class3,class4,class5,"-o","outputs/ccsourceTest_results"};
        MainKt.main(classesToCompile);
    }

//    @Test
//    public void testSimple1ContractCompile() {
//        String class1 = testClassesDir + "/gjavac/test/simple2/simpleContractTest";
//        String class2 = testClassesDir + "/gjavac/test/simple2/simpleEntryPoint";
////        String class3 = testClassesDir + "/gjavac/test/simple2/MultiOwnedContractSimpleInterface";
//        String class4 = testClassesDir + "/gjavac/test/simple2/Storage";
//
//        String[] classesToCompile = new String[]{class1, class2, class4,"-o", "outputs/simple2Test_results"};
//        MainKt.main(classesToCompile);
//    }

    @Test
    public void testFixedPriceContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/nft/fixedPriceContract/ContractEntrypoint";
        String class2 = testClassesDir + "/gjavac/test/nft/fixedPriceContract/FixedPriceContract";
        String class3 = testClassesDir + "/gjavac/test/nft/fixedPriceContract/Storage";
        String class4 = testClassesDir + "/gjavac/test/nft/fixedPriceContract/Utils";
        String class5 = testClassesDir + "/gjavac/test/nft/fixedPriceContract/MultiOwnedContractSimpleInterface";

        String[] classesToCompile = new String[]{class1, class2, class3, class4, class5, "-o", "outputs/fixedPriceContractTest_results"};
        MainKt.main(classesToCompile);
    }
//
    @Test
    public void testErc721ForeverContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/nft/erc721ForeverReward/ContractEntrypoint";
        String class2 = testClassesDir + "/gjavac/test/nft/erc721ForeverReward/ERC721ForeverRewardContract";
        String class3 = testClassesDir + "/gjavac/test/nft/erc721ForeverReward/ERC721ForeverRewardStorage";
        String class4 = testClassesDir + "/gjavac/test/nft/erc721ForeverReward/Utils";
        String class5 = testClassesDir + "/gjavac/test/nft/erc721ForeverReward/MultiOwnedContractsInterface";


        String[] classesToCompile = new String[]{class1, class2, class3, class4,class5, "-o", "outputs/erc721Forever_results"};
        MainKt.main(classesToCompile);
    }
//
    @Test
    public void testAuctionContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/nft/auctionContract/ContractEntrypoint";
        String class2 = testClassesDir + "/gjavac/test/nft/auctionContract/AuctionContract";
        String class3 = testClassesDir + "/gjavac/test/nft/auctionContract/AuctionStorage";
        String class4 = testClassesDir + "/gjavac/test/nft/auctionContract/Utils";
        String class5 = testClassesDir + "/gjavac/test/nft/auctionContract/MultiOwnedContractSimpleInterface";


        String[] classesToCompile = new String[]{class1, class2, class3, class4,class5, "-o", "outputs/auction_results"};
        MainKt.main(classesToCompile);
    }
//
//    @Test
//    public void testIssueoneContractCompile() {
//        String class1 = testClassesDir + "/gjavac/test/issueone/ContractEntrypoint";
//        String class3 = testClassesDir + "/gjavac/test/issueone/IssueOneContract";
//        String class4 = testClassesDir + "/gjavac/test/issueone/Storage";
//
//
//        String[] classesToCompile = new String[]{class1, class3, class4, "-o", "outputs/issueone_results"};
//        MainKt.main(classesToCompile);
//    }

}
