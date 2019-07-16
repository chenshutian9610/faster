package org.triski.faster.mybatis;

import org.triski.faster.mybatis.generator.main.TableGenerator;

/**
 * @author triski
 * @date 18-12-11
 * <p>
 * 示例
 */
class Main {
    public static void main(String args[]) throws Exception {
        String packageToScan = Main.class.getPackage().getName();
        TableGenerator generator = new TableGenerator(packageToScan);
        generator.forward();    //  正向工程
        generator.reverse();    //  逆向工程
    }
}
