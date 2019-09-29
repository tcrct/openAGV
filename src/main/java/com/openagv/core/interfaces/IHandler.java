package com.openagv.core.interfaces;

import com.openagv.exceptions.AgvException;

public interface IHandler {

    void doHandler(IRequest request, IResponse response) throws AgvException;

}
