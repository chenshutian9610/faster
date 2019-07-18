package org.triski.faster.springweb;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.triski.faster.commons.annotation.Comment;
import org.triski.faster.commons.annotation.Ignore;
import org.triski.faster.commons.exception.FasterException;
import org.triski.faster.commons.utils.ClasspathUtils;
import org.triski.faster.commons.utils.JavaDocCache;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author triski
 * @date 2018/11/2
 * @export debug
 */
@RestController
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    private List<ClassInfo> classInfos = Collections.emptyList();

    @Autowired
    private WebApplicationContext context;

    @Value("${debug.enable:false}")
    protected boolean debugEnable;

    @RequestMapping({"/debug", "/debug.do", "/debug.action", "/debug.htm", "/debug.html"})
    public String debug() throws IOException {
        if (classInfos.size() == 0) {
            initClassInfoList(Collections.emptyList());
        }

        // 获取页面模板
        InputStream in = ClasspathUtils.getResourcesAsStream("html/debug.html");
        String html = StreamUtils.copyToString(in, Charsets.UTF_8);
        // 将获得的 ClassInfo 列表注入并返回给前端
        return html.replace("${jsonValues}", JSON.toJSONString(classInfos));
    }

    /****************************** 内部方法 *******************************/

    /** 通过反射获取 url 和接口属性 */
    private void initClassInfoList(List<String> ignoreFields) {
        // 前置判断
        if (debugEnable == false) {
            throw new FasterException("请在系统配置 debug.enable=true");
        }
        // 获取所有 Controller 的 Class 列表 (除 basicErrorController 和 debugController)
        Map<String, Object> map = context.getBeansWithAnnotation(Controller.class);
        List<Class> classes = new ArrayList<>();
        map.forEach((name, instance) -> {
            if (StringUtils.equalsAny(name, "basicErrorController", "debugController") == false) {
                classes.add(instance.getClass());
            }
        });
        if (classes.size() == 0) {
            throw new FasterException("当前系统没有 Controller 对象");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("find controllers with @Controller: {}", classes);
        }
        // 初始化 JavadocCache
        List<Class> javadocList = new ArrayList<>();
        javadocList.addAll(classes);
        for (Class clazz : classes) {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                Parameter[] parameters = method.getParameters();
                for (Parameter parameter : parameters) {
                    boolean isIgnore = parameter.isAnnotationPresent(Ignore.class);
                    if (isNormalType(parameter.getType()) == false && isIgnore == false) {
                        javadocList.add(parameter.getType());
                    }
                }
            }
        }
        JavaDocCache.init(javadocList);
        // classes 循环
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        List<ClassInfo> classInfos = new ArrayList<>(classes.size());
        for (Class clazz : classes) {
            // 创建 ClassInfo 对象
            String prefixUrl = getUrl(clazz);
            if (prefixUrl.length() == 0) {
                prefixUrl = "/";
            }
            List<MethodInfo> methodInfos = new ArrayList<>();
            ClassInfo classInfo = new ClassInfo(prefixUrl, getDoc(clazz), methodInfos);
            if (classInfo.getComment().length() == 0) {
                classInfo.setComment(getComment(clazz));
            }
            // methods 循环
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                MethodInfo methodInfo;
                List<ArgInfo> args;
                String suffixUrl = getUrl(method);
                if (suffixUrl.length() != 0) {
                    // 创建 MethodInfo 对象
                    args = new ArrayList<>();
                    methodInfo = new MethodInfo(suffixUrl, getDoc(clazz, method), args);
                    if (methodInfo.getComment().length() == 0) {
                        methodInfo.setComment(getComment(method));
                    }
                    // parameters 循环
                    String[] parameterNames = discoverer.getParameterNames(method);
                    Parameter[] parameters = method.getParameters();
                    ArgInfo argInfo;
                    for (int i = 0; i < parameters.length; i++) {
                        // 是否是常见的普通类型
                        if (isNormalType(parameters[i].getType())) {
                            // 创建 ArgInfo 对象
                            argInfo = new ArgInfo(parameterNames[i], getDoc(clazz, method, parameterNames[i]));
                            if (argInfo.getComment().length() == 0) {
                                argInfo.setComment(getComment(parameters[i]));
                            }
                            args.add(argInfo);
                            continue;
                        }
                        // 如果参数不是被忽略的对象则继续
                        boolean isIgnore = parameters[i].isAnnotationPresent(Ignore.class) || ignoreFields.contains(parameterNames[i]);
                        if (isIgnore == false) {
                            Field[] MethodInfo = parameters[i].getType().getDeclaredFields();
                            for (Field field : MethodInfo) {
                                isIgnore = field.isAnnotationPresent(Ignore.class) || ignoreFields.contains(field.getName());
                                if (isIgnore == false) {
                                    argInfo = new ArgInfo(field.getName(), getDoc(parameters[i].getType(), field));
                                    if (argInfo.getComment().length() == 0) {
                                        argInfo.setComment(getComment(field));
                                    }
                                    args.add(argInfo);
                                }
                            }
                        }
                    }
                    methodInfos.add(methodInfo);
                }
            }
            classInfos.add(classInfo);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("generate classInfo list: {}", classInfos);
        }
        synchronized (this.classInfos) {
            this.classInfos = classInfos;
        }
    }

    private String getDoc(Class clazz) {
        return JavaDocCache.getComment(clazz.getSimpleName());
    }

    private String getDoc(Class clazz, Method method) {
        return JavaDocCache.getComment(String.format("%s::%s", clazz.getSimpleName(), method.getName()));
    }

    private String getDoc(Class clazz, Method method, String parameterName) {
        return JavaDocCache.getComment(String.format("%s::%s#%s", clazz.getSimpleName(), method.getName(), parameterName));
    }

    private String getDoc(Class clazz, Field field) {
        return JavaDocCache.getComment(String.format("%s#%s", clazz.getSimpleName(), field.getName()));
    }

    private String getComment(AnnotatedElement clazz) {
        String comment = "";
        if (clazz.isAnnotationPresent(Comment.class))
            comment = clazz.getAnnotation(Comment.class).value();
        return comment;
    }

    private String getUrl(AnnotatedElement element) {
        String url = "";
        if (element.isAnnotationPresent(RequestMapping.class))
            url = element.getAnnotation(RequestMapping.class).value()[0];
        return url;
    }

    private boolean isNormalType(Class clazz) {
        return Objects.equals(clazz, String.class) || ClassUtils.isPrimitiveOrWrapper(clazz);
    }

    /****************************** 内部类 *******************************/

    @Data
    @AllArgsConstructor
    private static class ClassInfo {
        String root;
        String comment;
        List<MethodInfo> methods;
    }

    @Data
    @AllArgsConstructor
    private static class MethodInfo {
        String method;
        String comment;
        List<ArgInfo> args;
    }

    @Data
    @AllArgsConstructor
    private static class ArgInfo {
        private String arg;
        private String comment;
    }
}
