package com.openagv.core.command;

public interface Command {

    <T> T execute(Object object) ;

}
