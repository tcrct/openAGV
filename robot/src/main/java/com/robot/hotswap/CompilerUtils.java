package com.robot.hotswap;

import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 动态编译工具类
 * @author laotang
 * @date 2019-5-30
 * http://www.360doc.com/content/17/1120/08/9200790_705450580.shtml
 * https://gitee.com/rico6/dynamiccompilerjava/blob/master/dynamiccompilerjava/src/dynamiccompilerjava/CompilerUtils.java
 * https://www.jianshu.com/p/b2e85dc63283
 * https://blog.csdn.net/zheng12tian/article/details/40617341
 * https://blog.csdn.net/u014653197/article/details/52796006
 */
public class CompilerUtils {

    private static final Logger logger = LoggerFactory.getLogger(CompilerUtils.class);

    private static class CompilerKitHolder {
        private static final CompilerUtils INSTANCE = new CompilerUtils();
    }
    private CompilerUtils() {}
    public static final CompilerUtils getInstance() {
        clear();
        return CompilerKitHolder.INSTANCE;
    }
    private static void clear(){

    }
    /****************************************************************************************/
    private JavaCompiler javaCompiler;


    private JavaCompiler getJavaCompiler() {
        if (javaCompiler == null) {
            synchronized (CompilerUtils.class) {
                if (javaCompiler == null) {
                    javaCompiler = ToolProvider.getSystemJavaCompiler();
                }
            }
        }
        return javaCompiler;
    }


    public Map<String, byte[]> compile(String javaName, String javaSrc)  {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            JavaFileObject javaFileObject = MemoryJavaFileManager.makeStringSource(javaName, javaSrc);
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
            if (task.call()) {
                return manager.getClassBytes();
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 编译java文件
     *
     * @param filePath
     *            文件或者目录（若为目录，自动递归编译）
     * @param sourceDir
     *            java源文件存放目录
     * @param targetDir
     *            编译后class类文件存放目录
     * @return boolean
     * @throws Exception
     */
    public boolean compiler(String filePath, String sourceDir, String targetDir) throws Exception {
//    public boolean compiler(List<File> sourceDirList, String sourceDir, String targetDir) throws Exception {
        // 错误信息
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        // 获取编译器实例
        JavaCompiler compiler = getJavaCompiler();
        // 获取标准文件管理器实例
        StandardJavaFileManager sdtManager = compiler.getStandardFileManager(null, null, null);
        try {
            if (ToolsKit.isEmpty(filePath) && ToolsKit.isEmpty(sourceDir) && ToolsKit.isEmpty(targetDir)) {
                return false;
            }
            // 得到filePath目录下的所有java源文件
            File sourceFile = new File(filePath);
            List<File> sourceFileList = new ArrayList<>();
            getSourceFiles(sourceFile, sourceFileList);
//            List<File> sourceFileList = getSourceFiles(sourceDirList);
            // 没有java文件，直接返回
            if (ToolsKit.isEmpty(sourceFileList)) {
                logger.warn("查找不到任何java文件");
                return false;
            }
            // 获取要编译的编译单元
            Iterable<? extends JavaFileObject> compilationUnits = sdtManager.getJavaFileObjectsFromFiles(sourceFileList);
            /**
             * 编译选项，在编译java文件时，编译程序会自动的去寻找java文件引用的其他的java源文件或者class。
             * -sourcepath选项就是定义java源文件的查找目录，
             * -classpath选项就是定义class文件的查找目录。
             */
            Iterable<String> options = Arrays.asList("-d", targetDir, "-sourcepath", sourceDir);
            JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, sdtManager, diagnostics, options, null, compilationUnits);
            // 运行编译任务
            boolean isCompiler =  compilationTask.call();
            if (!isCompiler) {
                StringWriter out = new StringWriter();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    out.append("Error on line " + diagnostic.getLineNumber() + " in " + diagnostic).append('\n');
                }
                logger.warn(out.toString());
            }
            return isCompiler;
        } finally {
            sdtManager.close();
        }
    }

    private List<File> getSourceFiles(List<File> sourceDirList) throws Exception {
        List<File> sourceFileList = new ArrayList<>();
        for(File sourceFile : sourceDirList) {
            if (sourceFile.isDirectory()) {
                // 得到该目录下以.java结尾的文件或者目录
                File[] childrenFiles = sourceFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isDirectory()) {
                            return true;
                        } else {
                            String name = pathname.getName();
                            return name.endsWith(".java") ? true : false;
                        }
                    }
                });
                for (File childFile : childrenFiles) {
                    if(childFile.isFile()) {
                        sourceFileList.add(childFile);
                    }
                }
            }
        }
        return sourceFileList;
    }

    /**
     * 查找该目录下的所有的java文件
     *
     * @param sourceFile
     * @param sourceFileList
     * @throws Exception
     */
    private void getSourceFiles(File sourceFile, List<File> sourceFileList) throws Exception {
        // 文件或者目录必须存在
        if (sourceFile.exists() && sourceFileList != null) {
            // 若file对象为目录
            if (sourceFile.isDirectory()) {
                // 得到该目录下以.java结尾的文件或者目录
                File[] childrenFiles = sourceFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isDirectory()) {
                            return true;
                        } else {
                            String name = pathname.getName();
                            return name.endsWith(".java") ? true : false;
                        }
                    }
                });
                // 递归调用
                for (File childFile : childrenFiles) {
                    getSourceFiles(childFile, sourceFileList);
                }
            } else {
                // 若file对象为文件
                if(!sourceFile.getAbsolutePath().contains("src"+File.separator+"test") && !sourceFile.getName().endsWith("Test")) {
                    sourceFileList.add(sourceFile);
                }
            }
        }
    }
}

