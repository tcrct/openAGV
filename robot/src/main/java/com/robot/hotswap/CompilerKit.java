package com.robot.hotswap;

import cn.hutool.core.io.FileUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * 动态编译工具类
 * @author laotang
 * @date 2019-5-31
 */
public class CompilerKit {

    private static final Logger logger = LoggerFactory.getLogger(CompilerKit.class);

    private static class CompilerKitHolder {
        private static final CompilerKit INSTANCE = new CompilerKit();
    }

    private CompilerKit() {
//        fileListener();
//        rootPath = ClassUtil.getClassPath()+ "com" ;
        rootPath = "C:\\workspace\\IdeaProjects\\agv4j\\src\\main";
        if (rootPath.endsWith("-web") || rootPath.endsWith("-WEB")) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf(File.separator));
        }
    }

    public static final CompilerKit duang() {
        clear();
        return CompilerKit.CompilerKitHolder.INSTANCE;
    }

    private static void clear() {

    }
//    private void fileListener() {
//        rootPath = PathKit.getWebRootPath();
//        String dirListenerPath = rootPath + rootItemPath() + File.separator + "com.signetz.openapi.controller".replace(".", File.separator);
//        System.out.println(dirListenerPath);
//        try {
//            FileListener.addListener(dirListenerPath);
//        } catch (Exception e) {
//            logger.warn(e.getMessage(),e);
//        }
//    }
    /****************************************************************************************/
    private static String rootPath;
    private String dirPath;
    private String sourceDir;
    private String targetDir;

    /**
     * java文件夹目录，IDEA下到java目录
     *
     * @return
     */
    public CompilerKit dir(String dirPath) {
        this.dirPath = dirPath;
        return this;
    }

    /**
     * 源代码目录
     *
     * @param javaDir
     * @return
     */
    public CompilerKit javaDir(String javaDir) {
        this.sourceDir = javaDir;
        return this;
    }

    /**
     * 指定class目录
     *
     * @param classDir class目录
     * @return
     */
    public CompilerKit classDir(String classDir) {
        this.targetDir = classDir;
        return this;
    }

    /**
     * maven开发模式下的固定路径
     *
     * @return
     */
    private String rootItemPath() {
        return File.separator + "src" + File.separator + "main" + File.separator + "java" ;
    }

    /**
     * 设置默认值
     */
    private void setDefaultValue() {
        if (ToolsKit.isEmpty(dirPath)) {
            dirPath = rootPath;// + rootItemPath();
        }
        if (ToolsKit.isEmpty(sourceDir)) {
            sourceDir = rootPath;// + rootItemPath();
        }
        if (ToolsKit.isEmpty(targetDir)) {
            targetDir = rootPath + File.separator + "target" + File.separator + "classes" ;
        }
    }

    /**
     * 源代码根路径位置
     *
     * @return 源代码根目录
     */
    public String dir() {
        setDefaultValue();
        logger.warn("hotswap dir path : " + dirPath);
        return dirPath;
    }

    /**
     * 指定编译后的class保存位置
     *
     * @return dir目录路径
     */
    public String classDir() {
//        setDefaultValue();
//        logger.warn("hotswap class path : " + targetDir);
        if (ToolsKit.isEmpty(targetDir)) {
            targetDir = rootPath + File.separator + "target" + File.separator + "classes" ;
        }
        return targetDir;
    }

    /**
     * 编译
     */
    public void compiler(List<File> watchingDirs) {
        boolean isSuccess = false;
        setDefaultValue();
        try {
            if (ToolsKit.isNotEmpty(targetDir)) {
                FileUtil.mkdir(new File(targetDir));
            }
            isSuccess = CompilerUtils.getInstance().compiler(dirPath, sourceDir, targetDir);
        } catch (Exception e) {
            logger.warn("动态编译时出错: " + e.getMessage(), e);
        }
        if (isSuccess) {
            ClassLoaderHelper.getInstance().hotSwap();
        } else {
            logger.warn("热部署失败");
        }
    }
}