package org.triski.faster.mybatis.generator.reverse;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.NullProgressCallback;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.XmlFileMergerJaxp;
import org.mybatis.generator.internal.util.ClassloaderUtility;
import org.mybatis.generator.internal.util.messages.Messages;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * @author triski
 * @date 18-10-23
 *
 * 扩展于 MyBatisGenerator
 * <p>
 * 添加一个 xmlMerge 属性，用于操作生成的 xml 文件是否和原文件融合
 * ps：原来的 MyBatisGenerator 将此选项写死，每次都是融合，不覆盖
 * <p>
 * 增加一个 generate(List<TableConfiguration>) 方法，使得可以通过代码添加表
 */
public class MyBatisGenerator {
    private Configuration configuration;
    private ShellCallback shellCallback;
    private List<GeneratedJavaFile> generatedJavaFiles;
    private List<GeneratedXmlFile> generatedXmlFiles;
    private List<String> warnings;
    private Set<String> projects;

    /* modify here */
    private boolean xmlMerge = false;

    public MyBatisGenerator(Configuration configuration, ShellCallback shellCallback, List<String> warnings) throws InvalidConfigurationException {
        if (configuration == null) {
            throw new IllegalArgumentException(Messages.getString("RuntimeError.2"));
        } else {
            this.configuration = configuration;
            if (shellCallback == null) {
                this.shellCallback = new DefaultShellCallback(false);
            } else {
                this.shellCallback = shellCallback;
            }

            if (warnings == null) {
                this.warnings = new ArrayList();
            } else {
                this.warnings = warnings;
            }

            this.generatedJavaFiles = new ArrayList();
            this.generatedXmlFiles = new ArrayList();
            this.projects = new HashSet();
            this.configuration.validate();
        }
    }

    public boolean isXmlMerge() {
        return xmlMerge;
    }

    public void setXmlMerge(boolean xmlMerge) {
        this.xmlMerge = xmlMerge;
    }

    public void generate(ProgressCallback callback) throws SQLException, IOException, InterruptedException {
        this.generate(callback, (Set) null, (Set) null, true);
    }

    public void generate(ProgressCallback callback, Set<String> contextIds) throws SQLException, IOException, InterruptedException {
        this.generate(callback, contextIds, (Set) null, true);
    }

    public void generate(ProgressCallback callback, Set<String> contextIds, Set<String> fullyQualifiedTableNames) throws SQLException, IOException, InterruptedException {
        this.generate(callback, contextIds, fullyQualifiedTableNames, true);
    }

    /* modify here */
    public void generate(List<TableConfiguration> tableConfigurations) throws SQLException, IOException, InterruptedException {

        ProgressCallback callback = new NullProgressCallback();
        this.generatedJavaFiles.clear();
        this.generatedXmlFiles.clear();
        ObjectFactory.reset();
        RootClassInfo.reset();
        Object contextsToRun;

        contextsToRun = this.configuration.getContexts();

        if (this.configuration.getClassPathEntries().size() > 0) {
            ClassLoader classLoader = ClassloaderUtility.getCustomClassloader(this.configuration.getClassPathEntries());
            ObjectFactory.addExternalClassLoader(classLoader);
        }

        int totalSteps = 0;

        Context context;
        Iterator var11;
        for (var11 = ((List) contextsToRun).iterator(); var11.hasNext(); totalSteps += context.getIntrospectionSteps()) {
            context = (Context) var11.next();
        }

        ((ProgressCallback) callback).introspectionStarted(totalSteps);
        var11 = ((List) contextsToRun).iterator();

        while (var11.hasNext()) {
            context = (Context) var11.next();
            for (TableConfiguration tc : tableConfigurations)
                context.addTableConfiguration(tc);
            context.introspectTables((ProgressCallback) callback, this.warnings, null);
        }

        totalSteps = 0;

        for (var11 = ((List) contextsToRun).iterator(); var11.hasNext(); totalSteps += context.getGenerationSteps()) {
            context = (Context) var11.next();
        }

        ((ProgressCallback) callback).generationStarted(totalSteps);
        var11 = ((List) contextsToRun).iterator();

        while (var11.hasNext()) {
            context = (Context) var11.next();
            context.generateFiles((ProgressCallback) callback, this.generatedJavaFiles, this.generatedXmlFiles, this.warnings);
        }


        ((ProgressCallback) callback).saveStarted(this.generatedXmlFiles.size() + this.generatedJavaFiles.size());
        var11 = this.generatedXmlFiles.iterator();

        while (var11.hasNext()) {
            GeneratedXmlFile gxf = (GeneratedXmlFile) var11.next();
            this.projects.add(gxf.getTargetProject());
            this.writeGeneratedXmlFile(gxf, (ProgressCallback) callback);
        }

        var11 = this.generatedJavaFiles.iterator();

        while (var11.hasNext()) {
            GeneratedJavaFile gjf = (GeneratedJavaFile) var11.next();
            this.projects.add(gjf.getTargetProject());
            this.writeGeneratedJavaFile(gjf, (ProgressCallback) callback);
        }

        var11 = this.projects.iterator();

        while (var11.hasNext()) {
            String project = (String) var11.next();
            this.shellCallback.refreshProject(project);
        }

        ((ProgressCallback) callback).done();
    }


    public void generate(ProgressCallback callback, Set<String> contextIds, Set<String> fullyQualifiedTableNames, boolean writeFiles) throws SQLException, IOException, InterruptedException {
        if (callback == null) {
            callback = new NullProgressCallback();
        }

        this.generatedJavaFiles.clear();
        this.generatedXmlFiles.clear();
        ObjectFactory.reset();
        RootClassInfo.reset();
        Object contextsToRun;
        if (contextIds != null && contextIds.size() != 0) {
            contextsToRun = new ArrayList();
            Iterator var6 = this.configuration.getContexts().iterator();

            while (var6.hasNext()) {
                Context context = (Context) var6.next();
                if (contextIds.contains(context.getId())) {
                    ((List) contextsToRun).add(context);
                }
            }
        } else {
            contextsToRun = this.configuration.getContexts();
        }

        if (this.configuration.getClassPathEntries().size() > 0) {
            ClassLoader classLoader = ClassloaderUtility.getCustomClassloader(this.configuration.getClassPathEntries());
            ObjectFactory.addExternalClassLoader(classLoader);
        }

        int totalSteps = 0;

        Context context;
        Iterator var11;
        for (var11 = ((List) contextsToRun).iterator(); var11.hasNext(); totalSteps += context.getIntrospectionSteps()) {
            context = (Context) var11.next();
        }

        ((ProgressCallback) callback).introspectionStarted(totalSteps);
        var11 = ((List) contextsToRun).iterator();

        while (var11.hasNext()) {
            context = (Context) var11.next();
            context.introspectTables((ProgressCallback) callback, this.warnings, fullyQualifiedTableNames);
        }

        totalSteps = 0;

        for (var11 = ((List) contextsToRun).iterator(); var11.hasNext(); totalSteps += context.getGenerationSteps()) {
            context = (Context) var11.next();
        }

        ((ProgressCallback) callback).generationStarted(totalSteps);
        var11 = ((List) contextsToRun).iterator();

        while (var11.hasNext()) {
            context = (Context) var11.next();
            context.generateFiles((ProgressCallback) callback, this.generatedJavaFiles, this.generatedXmlFiles, this.warnings);
        }

        if (writeFiles) {
            ((ProgressCallback) callback).saveStarted(this.generatedXmlFiles.size() + this.generatedJavaFiles.size());
            var11 = this.generatedXmlFiles.iterator();

            while (var11.hasNext()) {
                GeneratedXmlFile gxf = (GeneratedXmlFile) var11.next();
                this.projects.add(gxf.getTargetProject());
                this.writeGeneratedXmlFile(gxf, (ProgressCallback) callback);
            }

            var11 = this.generatedJavaFiles.iterator();

            while (var11.hasNext()) {
                GeneratedJavaFile gjf = (GeneratedJavaFile) var11.next();
                this.projects.add(gjf.getTargetProject());
                this.writeGeneratedJavaFile(gjf, (ProgressCallback) callback);
            }

            var11 = this.projects.iterator();

            while (var11.hasNext()) {
                String project = (String) var11.next();
                this.shellCallback.refreshProject(project);
            }
        }

        ((ProgressCallback) callback).done();
    }

    private void writeGeneratedJavaFile(GeneratedJavaFile gjf, ProgressCallback callback) throws InterruptedException, IOException {
        try {
            File directory = this.shellCallback.getDirectory(gjf.getTargetProject(), gjf.getTargetPackage());
            File targetFile = new File(directory, gjf.getFileName());
            String source;
            if (targetFile.exists()) {
                if (this.shellCallback.isMergeSupported()) {
                    source = this.shellCallback.mergeJavaFile(gjf.getFormattedContent(), new File(targetFile.getAbsolutePath()), MergeConstants.OLD_ELEMENT_TAGS, gjf.getFileEncoding());
                } else if (this.shellCallback.isOverwriteEnabled()) {
                    source = gjf.getFormattedContent();
                    this.warnings.add(Messages.getString("Warning.11", targetFile.getAbsolutePath()));
                } else {
                    source = gjf.getFormattedContent();
                    targetFile = this.getUniqueFileName(directory, gjf.getFileName());
                    this.warnings.add(Messages.getString("Warning.2", targetFile.getAbsolutePath()));
                }
            } else {
                source = gjf.getFormattedContent();
            }

            callback.checkCancel();
            callback.startTask(Messages.getString("Progress.15", targetFile.getName()));
            this.writeFile(targetFile, source, gjf.getFileEncoding());
        } catch (ShellException var6) {
            this.warnings.add(var6.getMessage());
        }

    }

    private void writeGeneratedXmlFile(GeneratedXmlFile gxf, ProgressCallback callback) throws InterruptedException, IOException {
        try {
            File directory = this.shellCallback.getDirectory(gxf.getTargetProject(), gxf.getTargetPackage());
            File targetFile = new File(directory, gxf.getFileName());
            String source;
            if (targetFile.exists()) {
                /* modify here *///gxf.isMergeable()
                if (this.xmlMerge) {
                    source = XmlFileMergerJaxp.getMergedSource(gxf, targetFile);
                } else if (this.shellCallback.isOverwriteEnabled()) {
                    source = gxf.getFormattedContent();
                    this.warnings.add(Messages.getString("Warning.11", targetFile.getAbsolutePath()));
                } else {
                    source = gxf.getFormattedContent();
                    targetFile = this.getUniqueFileName(directory, gxf.getFileName());
                    this.warnings.add(Messages.getString("Warning.2", targetFile.getAbsolutePath()));
                }
            } else {
                source = gxf.getFormattedContent();
            }

            callback.checkCancel();
            callback.startTask(Messages.getString("Progress.15", targetFile.getName()));
            this.writeFile(targetFile, source, "UTF-8");
        } catch (ShellException var6) {
            this.warnings.add(var6.getMessage());
        }

    }

    private void writeFile(File file, String content, String fileEncoding) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter osw;
        if (fileEncoding == null) {
            osw = new OutputStreamWriter(fos);
        } else {
            osw = new OutputStreamWriter(fos, fileEncoding);
        }

        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(content);
        bw.close();
    }

    private File getUniqueFileName(File directory, String fileName) {
        File answer = null;
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < 1000; ++i) {
            sb.setLength(0);
            sb.append(fileName);
            sb.append('.');
            sb.append(i);
            File testFile = new File(directory, sb.toString());
            if (!testFile.exists()) {
                answer = testFile;
                break;
            }
        }

        if (answer == null) {
            throw new RuntimeException(Messages.getString("RuntimeError.3", directory.getAbsolutePath()));
        } else {
            return answer;
        }
    }

    public List<GeneratedJavaFile> getGeneratedJavaFiles() {
        return this.generatedJavaFiles;
    }

    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        return this.generatedXmlFiles;
    }
}
