package com.robot.hotswap;

import cn.hutool.core.util.ClassUtil;
import com.robot.utils.RobotUtil;
import com.robot.utils.SettingUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by laotang on 2019/5/27.
 */
public class DuangClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(DuangClassLoader.class);
    private static String BASE_PACKAGE_PATH = "";
    /**
     * 所有业务类的名称
     */
    private Set<String> CLASSLOADER_SET;


    public DuangClassLoader() {
        super(DuangClassLoader.class.getClassLoader());
        init();
    }

    private void init() {
        String packagePath = SettingUtil.getString("package.name");
        if(!RobotUtil.isDevMode() && ToolsKit.isEmpty(packagePath)) {
            logger.info("热部署功能只允许在开发环境下运行");
            return;
        }
        CLASSLOADER_SET = new HashSet<>();
        String classFileDir = ClassUtil.getClassPath();
        BASE_PACKAGE_PATH = classFileDir;
        Set<Class<?>> classSet = ClassUtil.scanPackage(packagePath);
        if (ToolsKit.isNotEmpty(classSet)) {
            for (Class<?> clazz : classSet) {
                if(!ClassUtil.isNormalClass(clazz)) {
                    continue;
                }
                String fileName = clazz.getName().replace('.', '/').concat(".class");
                File file = new File(classFileDir + fileName);
                if (file.isFile()) {
                    getClassData(file);
                }
                CLASSLOADER_SET.add(clazz.getName());
            }
        }

    }

    public Set<String> getClassKeySet() {
        return CLASSLOADER_SET;
    }

    private static String getClassAbsolutePath(File file) {
        String classPath = file.getAbsolutePath();
        classPath = classPath.substring(BASE_PACKAGE_PATH.length(), classPath.length()-6);
        classPath = classPath.replace(File.separator, ".");
        return classPath;
    }

    /**
     * 获取类数据
     */
    private void getClassData(File classPathFile) {
        try {
            InputStream fin = new FileInputStream(classPathFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int byteNumRead = 0;
            while ((byteNumRead = fin.read(buffer)) != -1) {
                bos.write(buffer, 0, byteNumRead);
            }
            byte[] classBytes = bos.toByteArray();
            String classKey = getClassAbsolutePath(classPathFile);
            if(!getClassKeySet().contains(classKey) ) {
                getClassKeySet().add(classKey);
                defineClass(classKey, classBytes, 0, classBytes.length);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class cls = null;
        cls = findLoadedClass(name);
        if (cls == null) {
            cls=getParent().loadClass(name);
        }
        if (cls == null) {
            throw new ClassNotFoundException(name);
        }
        if (resolve) {
            resolveClass(cls);
        }
        return cls;
    }

}
