package com.openagv.core.interfaces;

import com.openagv.exceptions.AgvException;

/**
 *
 */
public interface IService<T> {

    String MAIN_ACTION_NAME = "index";

    T index(IRequest request) throws AgvException;

}
