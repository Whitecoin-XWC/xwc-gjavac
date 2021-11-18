package gjavac.test;

import gjavac.MainKt;
import org.junit.Test;

/*
*   This class is for the testcases of the Java Smart Contract
*   The structure is that, the contract source code package should be added in this folder, and a new function
*   should be added in this class as a junit case.
* */

public class JavaContractTest {

    private static final String testClassesDir = "target/test-classes";

    @Test
    public void testJavaContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/java/DemoContract";
        String class2 = testClassesDir + "/gjavac/test/java/Utils";
        String class3 = testClassesDir + "/gjavac/test/java/MultiOwnedContractSimpleInterface";
        String class4 = testClassesDir + "/gjavac/test/java/DemoContractEntrypoint";
        String class5 = testClassesDir + "/gjavac/test/java/Storage";

        String[] classesToCompile = new String[] {class1, class2, class3, class4,class5, "-o", "outputs/java_results"};
        MainKt.main(classesToCompile);
    }

    @Test
    public void testDaiContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/dai/DaiPriceFeederForCdcContract";
        String class2 = testClassesDir + "/gjavac/test/dai/Utils";
        String class3 = testClassesDir + "/gjavac/test/dai/MultiOwnedContractSimpleInterface";
        String class4 = testClassesDir + "/gjavac/test/dai/DaiPriceFeederForCdcContractEntryPoint";
        String class5 = testClassesDir + "/gjavac/test/dai/Storage";

        String[] classesToCompile = new String[] {class1, class2, class3, class4,class5, "-o", "outputs/dai_results"};
        MainKt.main(classesToCompile);
    }

    @Test
    public void testSimpleContractCompile() {
        String class1 = testClassesDir + "/gjavac/test/simple/simpleContractTest";
        String class2 = testClassesDir + "/gjavac/test/simple/simpleEntryPoint";

        String[] classesToCompile = new String[] {class1, class2, "-o", "outputs/simpleTest_results"};
        MainKt.main(classesToCompile);
    }
}
