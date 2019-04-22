package com.qingwenwei.constant;


public class ResponseResult {
	
	/** 
	 * 状态码 
	 */
	private String code;
	
	/** 
	 * 提示信息 
	 */
	private String message;

	/**
	 * 实体 
	 */
	private Object data;
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}
