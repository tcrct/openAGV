package com.openagv.core;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.inject.*;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Named;
import com.openagv.db.IDao;
import com.openagv.db.annotation.ClientId;
import com.openagv.db.mongodb.MongoDao;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * 自动注入对象，基于google guice
 * 每个Service必须要实现一个接口且注明@Service注解
 *
 * 注入泛型
 * https://blog.csdn.net/chuanli5157/article/details/100755428
 * 第一个方法是，在TestFoo的IFoo接口成员声明时，去掉泛型参数，这样Guice在运行时查找匹配的实现时就会按照IFoo的实现类去查找，而不是按照IFoo<Integer> 或 IFoo<String> 这样的带有泛型参数的接口去查找
 * 第二个方法是，在FooModule中configure函数里，在bind的时候使用TypeLiteral类来在注册的时候保持泛型参数信息进行注册，这样就可以保证注册的信息里是以带有泛型参数的接口注册的，代码例子如下:
 *
 * @author Laotang
 */
public class AutoImportModule extends AbstractModule {

    private final static Log logger = LogFactory.get();

    @Override
    public void configure() {
        String packageName = SettingUtils.getString("package.name");
        java.util.Objects.requireNonNull(packageName, "请在app.setting文件里先设置package.name");
        Set<Class<?>> classSet = ClassUtil.scanPackage(packageName, new Filter<Class<?>>() {
            @Override
            public boolean accept(Class<?> aClass) {
                return ToolsKit.isInjectClass(aClass);
            }
        });
        for (Class clazz : classSet) {
            binder(clazz);
        }
        AppContext.getInjectClassSet().addAll(classSet);

//        try {
//            for (Class clazz : AppContext.getInjectClassSet()) {
//                InjectDao(clazz);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 绑定到容器
     * @param clazz 要注入的类
     * @param <T>  泛型类
     */
    private <T> void binder(Class<T> clazz) {
        java.util.Objects.requireNonNull(clazz,"要注入的class不能为null");
        try {
            if(ToolsKit.isInjectServiceClass(clazz)) {
                Class<T> interfaceClass = (Class<T>) clazz.getInterfaces()[0];
                bind(interfaceClass).to(clazz).in(Scopes.SINGLETON);
//                InjectDao(clazz);
            }
        } catch (Exception e){
            logger.error("bind is fail : " + e.getMessage(), e);
        }
    }

    private void InjectDao(Class<?> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            boolean isInjectAnn = field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(javax.inject.Inject.class);
            if(isInjectAnn && MongoDao.class.equals(field.getType())) {
                ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                Type[] types = paramType.getActualTypeArguments();
                if(ToolsKit.isNotEmpty(types)) {
                    // <>里的泛型类
                    Class<?> paramTypeClass = ClassUtil.loadClass(types[0].getTypeName());
//                    String key = ToolsKit.isEmpty(importAnnot.client()) ? DbClientFactory.getMongoDefaultClientId() : importAnnot.client();
                    Object daoObj = ReflectUtil.newInstance(MongoDao.class, paramTypeClass); //MongoUtils.getMongoDao(key, paramTypeClass, proxyList);
//                    Object serviceObj = AppContext.getGuiceInjector().getExistingBinding(Key.get(clazz));
//                    Key key = AppContext.getGuiceInjector().getBinding(clazz).getKey();
//                    Object serviceObj = AppContext.getGuiceInjector().getExistingBinding(key);
                    Object serviceObj = AppContext.getGuiceInjector().getBinding(clazz);
//                    Object serviceObj = ReflectUtil.newInstance(clazz.getName());
                    if(null != daoObj) {
                        field.setAccessible(true);
                        field.set(serviceObj, daoObj);
                    }
                }
            }
        }
    }



}
