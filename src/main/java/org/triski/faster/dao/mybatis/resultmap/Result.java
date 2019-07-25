package org.triski.faster.dao.mybatis.resultmap;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author triski
 * @date 2019/7/12
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@XStreamAlias("result")
public class Result {
    @NonNull
    @XStreamAsAttribute
    private String column;

    @NonNull
    @XStreamAsAttribute
    private String property;
}