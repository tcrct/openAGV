/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.app;

import com.google.inject.*;
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
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;

/**
 * Displays a dialog showing information about the application.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ShowRoomAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "application.showroom";

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
  private static final String SERVICE_CLASS_NAME = "com.robot.service.ShowRoomService";

  /**
   * Creates a new instance.
   *
   * @param appState Stores the application's current state.
   * @param portalProvider Provides access to a portal.
   * @param dialogParent The parent component for dialogs shown by this action.
   */
  @Inject
  public ShowRoomAction(ApplicationState appState,
                        SharedKernelServicePortalProvider portalProvider,
                        @ApplicationFrame Component dialogParent) {
    this.appState = requireNonNull(appState, "appState");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");

    putValue(NAME, "ShowRoom");
    putValue(MNEMONIC_KEY, Integer.valueOf('A'));

    ImageIcon icon = ImageDirectory.getImageIcon("/menu/help-contents.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {

    int dialogResult
            = JOptionPane.showConfirmDialog(dialogParent,
            "<html><p>运行展厅所有示例</p></html>",
            BUNDLE.getString("aboutAction.optionPane_applicationInformation.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (dialogResult != JOptionPane.OK_OPTION) {
      return;
    } else  {
      System.out.println("showroom is start");
      try {
        Class clazz = Class.forName(SERVICE_CLASS_NAME);
//        Injector injector = Guice.createInjector(new Module() {
//          @Override
//          public void configure(Binder binder) {
//            binder.bind(clazz).in(Scopes.SINGLETON);
//          }
//        });
//        Object object = injector.getInstance(clazz);
        Object object = clazz.newInstance();
        Method method = clazz.getMethod("runAll");
        method.invoke(object);
      } catch (Exception e) {

      }
    }


  }
}
