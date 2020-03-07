package com.robot.hotswap;

import cn.hutool.core.thread.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

/**
 * 按指定路径对文件进行监听
 * @author laotang
 */
public class FileListener {
    private static final Logger logger = LoggerFactory.getLogger(FileListener.class);
//    private static ExecutorService fixedThreadPool = Executors.newCachedThreadPool();
    private WatchService ws;
    private String listenerPath;
    private FileListener(String path) {
        try {
            ws = FileSystems.getDefault().newWatchService();
            this.listenerPath = path;
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
//        fixedThreadPool.execute(new FileWatcher(ws,this.listenerPath));
        ThreadUtil.execute(new FileWatcher(ws,listenerPath));
    }

    /**
     * 按指定的路径进行，修改，删除，创建等添加监听
     * @param dir      文件夹路径
     * @throws IOException
     */
    public static void addListener(String dir) throws IOException {
        FileListener fileListener = new FileListener(dir);
        Path p = Paths.get(dir);
        p.register(fileListener.ws,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_CREATE);
    }


    public static void main(String[] args) throws IOException {
        FileListener.addListener("F:\\");
        FileListener.addListener("d:\\");
    }
}

/**
 * 监听线程
 */
class FileWatcher implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FileListener.class);
    private WatchService service;
    private String rootPath;

    public FileWatcher(WatchService service, String rootPath) {
        this.service = service;
        this.rootPath = rootPath;
    }

    @Override
    public void run() {
        try {
            while(true){
                WatchKey watchKey = service.take();
                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                for(WatchEvent<?> event : watchEvents){
                    //TODO 根据事件类型采取不同的操作。。。。。。。
                    System.out.println("["+rootPath+event.context()+"]文件发生了["+event.kind()+"]事件"+    event.count());
                }
                watchKey.reset();
            }
        } catch (InterruptedException e) {
            logger.info("文件监听时出错: " + e.getMessage(), e);
        }finally{
            try {
                service.close();
            } catch (IOException e) {
                logger.info("文件监听关闭时出错: " + e.getMessage(), e);
            }
        }

    }
}
