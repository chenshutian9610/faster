package org.triski.faster;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.tool.schema.TargetType;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.util.EnumSet;

/**
 * @author chenshutian
 * @date 2019/7/26
 */
@Slf4j
public class MainTest {
    @Test
    void test() throws Exception{
        System.out.println(URLEncoder.encode("@","gbk"));
    }
}
