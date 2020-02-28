package org.opentcs.guing.application.action.app;

import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;

/**
 * 一键运行地图显示的所有项目
 *
 * @author Laotang
 */
public class RunMapProjectAction
    extends AbstractAction {

  /**
   * This action's ID.
   * 与app.setting配置文件中的key一致
   */
  public final static String ID = "robot.runAll.project";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The parent component for dialogs shown by this action.
   */
  private final Component dialogParent;
  /***
   * 动作地图所有项目的类
   */
  private static Class runMapProjectClazz;
  /***
   * 动作地图所有项目的对象
   */
  private static Object runMapProject;

  /**
   * Creates a new instance.
   *
   * @param appState 存储应用程序的当前状态
   * @param portalProvider 提供对门户的访问
   * @param dialogParent 此操作显示的对话框的父组件
   */
  @Inject
  public RunMapProjectAction(ApplicationState appState,
                             SharedKernelServicePortalProvider portalProvider,
                             @ApplicationFrame Component dialogParent) {
    this.appState = requireNonNull(appState, "appState");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");

    putValue(NAME, "RunMapProject");
    putValue(MNEMONIC_KEY, Integer.valueOf('R'));

    ImageIcon icon = ImageDirectory.getImageIcon("/menu/cog-go.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  //TODO 一键运行地图显示的所有项目
  @Override
  public void actionPerformed(ActionEvent evt) {
    int dialogResult
            = JOptionPane.showConfirmDialog(dialogParent,
            "<html><p>是否一键运行地图显示的所有项目？</p></html>",
            "Warning",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (dialogResult != JOptionPane.OK_OPTION) {
      return;
    } else  {
      try {
        String classPath = System.getProperty(ID);
        if (null == classPath || classPath.length() == 0) {
          System.out.println("请先在系统中设置System.setProperty(\""+ID+"\", \"类全路径\")，并且必须要有runAll()方法");
          return;
        }
        if (null == runMapProject) {
          runMapProjectClazz = Class.forName(classPath);
          runMapProject = runMapProjectClazz.newInstance();
        }
        Method method = runMapProjectClazz.getMethod("runAll");
        method.invoke(runMapProject);
      } catch (Exception e) {

      }
    }


  }
}
