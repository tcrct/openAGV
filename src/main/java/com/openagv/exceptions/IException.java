package com.openagv.exceptions;

public interface IException {

	int FAIL_CODE = 1;
	String FAIL_MESSAGE = "ERROR";
	
	int SUCCESS_CODE = 0;
	String SUCCESS_MESSAGE = "SUCCESS";
	
	int getCode();
	
	String getMessage();
}
