# gjavac developing environment

This documents introduces how to setup a developing environment for gjavac. As this project is developed by java and kotlin, a Java, Kotlin and Maven developing environment can be certainly used under command line.  However, Intellij IDEA is strongly recommanded for the project gjavac as it makes life much easier. In the next section the IDEA developing enviroment under Windows will be introduced along with how to add/run smart contracts.



# Dependencies

1. Java 1.8+ (This is compulsory and any version higher than 1.8 is not supported currently)
2. IntelliJ IDEA 2021.2.3 (Community Edition) (Any other version should work as well)



# Installation & compilation

1. Install Java 1.8 and setup the java path to the system environment variables.

2. Install IntelliJ IDEA.

3. Download the source code to your workspace:

   `git clone https://github.com/Whitecoin-XWC/xwc-gjavac.git`

4. Start IDEA and open the gjavace workspace as a project. Note: don't create a new project, but use "Open" to open the gjavac as a Maven project.

5. Wait IDEA to download all necessary libraries including kotlin runtime .

6. Once all the libraries are ready, the whole project can be built:

   <img src="https://github.com/Whitecoin-XWC/xwc-gjavac/blob/master/image-20211119165236190.png" alt="image-20211119165236190" style="zoom: 50%;" />

7. The project consists of 4 modules: 

   - gjavac-compiler: entry of the gjavac compiler, the main function is in 'Main.kt'.
   - gjavac-core: the core library of java contract, including all type/structure/internal API definitions of writting a XWC smart contract.
   - gjavactestjavacontract: the java smart contract testcases, any new Java testcases should be added here.
   - gjavactestkotlincontract: the kotlin smart contract testcases, any new kotline testcases should be added here.

8. The java smart contract testcases are driven by the junit, so it is quite easy to run all testcases:

   ​	<img src="C:\Users\David Yang\AppData\Roaming\Typora\typora-user-images\image-20211119171317387.png" alt="image-20211119171317387" style="zoom:67%;" />

9. Now the gjavac compiler developing environment is setup and you can start writting your first XWC Java smart contract. The compiled output file .ass can be found at *gjavactestjavacontract/outputs/\**



# Java smart contract Deployment

TBD

- `uvm_ass path-of-.ass-file` to generate bytecode file(*.out)
- `package_gpc path-of-bytecode-file path-of-metadata-json-file` to generate contract file(*.gpc)



# XWC node local testing environment

TBD

Please refer to https://github.com/Whitecoin-XWC/xwc_chain_local_testing_environment

