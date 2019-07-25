//package org.triski.faster.dao.mybatis;
//
//import org.triski.faster.commons.utils.MapBuilder;
//import org.triski.faster.dao.mybatis.generator.reverse.MybatisUtils;
//import org.triski.faster.dao.mybatis.generator.reverse.TableInfo;
//import org.triski.faster.dao.mybatis.generator.reverse.TableList;
//
//import java.util.Map;
//
///**
// * @author triski
// * @date 18-12-11
// */
//class UsefulExample {
//    public static void main(String args[]) {
//        // 要生成的表
//        TableList tableList = new TableList()
//                .addTableMap(new TableInfo("tb_user", "User"))
//                .addTableMap(new TableInfo("tb_id_card", "IDCard"));
//        Map<String, String> tables = MapBuilder.put("tb_user", "User").put("tb_id_card", "IDCard").build();
//        // 开始生成
//        MybatisUtils generator = new MybatisUtils("mybatis/faster.yml");
//        generator.generate(tables);
//    }
//}
