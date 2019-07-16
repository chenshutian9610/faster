package org.triski.faster.mybatis.generator.resultmap;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author triski
 * @date 2019/7/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XStreamAlias("mapper")
public class Mapper {
    @XStreamAsAttribute
    private String namespace;

    @XStreamImplicit
    private List<ResultMap> resultMaps;
}
