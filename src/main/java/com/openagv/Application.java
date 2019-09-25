package com.openagv;

import com.google.inject.Guice;
import com.openagv.core.AgvContext;
import com.openagv.core.AutoImportModule;

/**
 * Hello world!
 *
 */
public class Application {

    private static Application application;

    public static Application duang() {
        if(application == null) {
            application = new Application();
        }
        return application;
    }

    private Application(){
    }

    public void run() {
        if(null == AgvContext.getGuiceInjector()){
            AgvContext.getModules().add(new AutoImportModule());
            AgvContext.setGuiceInjector(Guice.createInjector(AgvContext.getModules()));
        }
    }

}
