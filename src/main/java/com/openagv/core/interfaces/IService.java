package com.openagv.core.interfaces;

import com.openagv.exceptions.AgvException;

/**
 *
 */
public interface IService {

    <T> T index(IRequest request) throws AgvException;

}
