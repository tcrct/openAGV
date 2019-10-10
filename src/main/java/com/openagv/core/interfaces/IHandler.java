package com.openagv.core.interfaces;

import com.openagv.exceptions.AgvException;

public interface IHandler {

    boolean doHandler(IRequest request, IResponse response) throws AgvException;

}
