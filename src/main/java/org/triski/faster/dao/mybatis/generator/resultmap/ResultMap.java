package org.triski.faster.dao.mybatis.generator.resultmap;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author triski
 * @date 2019/7/12
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@XStreamAlias("resultMap")
public class ResultMap {

    @NonNull
    @XStreamAsAttribute
    private String id;

    @NonNull
    @XStreamAsAttribute
    private String type;

    @XStreamAlias("id")
    private Result idTag;
    @XStreamImplicit
    private List<Result> resultTags;

}