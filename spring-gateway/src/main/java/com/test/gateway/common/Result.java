package com.test.gateway.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.test.gateway.exception.BaseException;
import com.test.gateway.exception.ErrorType;
import com.test.gateway.exception.SystemErrorType;
import lombok.Getter;

import java.time.Instant;
import java.time.ZonedDateTime;

@Getter
public class Result<T> {

	public static final String SUCCESSFUL_CODE = "000000";
	public static final String SUCCESSFUL_MESG = "处理成功";

	private String code;
	private String mesg;
	private Instant time;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	public Result() {
		this.time = ZonedDateTime.now().toInstant();
	}

	/**
	 * @param errorType
	 */
	public Result(ErrorType errorType) {
		this.code = errorType.getCode();
		this.mesg = errorType.getMesg();
		this.time = ZonedDateTime.now().toInstant();
	}

	/**
	 * @param errorType
	 * @param data
	 */
	public Result(ErrorType errorType, T data) {
		this(errorType);
		this.data = data;
	}

	/**
	 * 内部使用，用于构造成功的结果
	 *
	 * @param code
	 * @param mesg
	 * @param data
	 */
	private Result(String code, String mesg, T data) {
		this.code = code;
		this.mesg = mesg;
		this.data = data;
		this.time = ZonedDateTime.now().toInstant();
	}

	/**
	 * 快速创建成功结果并返回结果数据
	 *
	 * @param data
	 * @return Result
	 */
	public static Result success(Object data) {
		return new Result<>(SUCCESSFUL_CODE, SUCCESSFUL_MESG, data);
	}

	/**
	 * 快速创建成功结果
	 *
	 * @return Result
	 */
	public static Result success() {
		return success(null);
	}

	/**
	 * 系统异常类没有返回数据
	 *
	 * @return Result
	 */
	public static Result fail() {
		return new Result(SystemErrorType.SYSTEM_ERROR);
	}

	/**
	 * 系统异常类没有返回数据
	 *
	 * @param baseException
	 * @return Result
	 */
	public static Result fail(BaseException baseException) {
		return fail(baseException, null);
	}

	/**
	 * 系统异常类并返回结果数据
	 *
	 * @param data
	 * @return Result
	 */
	public static Result fail(BaseException baseException, Object data) {
		return new Result<>(baseException.getErrorType(), data);
	}

	/**
	 * 系统异常类并返回结果数据
	 *
	 * @param errorType
	 * @param data
	 * @return Result
	 */
	public static Result fail(ErrorType errorType, Object data) {
		return new Result<>(errorType, data);
	}

	/**
	 * 系统异常类并返回结果数据
	 *
	 * @param errorType
	 * @return Result
	 */
	public static Result fail(ErrorType errorType) {
		return Result.fail(errorType, null);
	}

	/**
	 * 系统异常类并返回结果数据
	 *
	 * @param data
	 * @return Result
	 */
	public static Result fail(Object data) {
		return new Result<>(SystemErrorType.SYSTEM_ERROR, data);
	}


	/**
	 * 成功code=000000
	 *
	 * @return true/false
	 */
	@JsonIgnore
	public boolean isSuccess() {
		return SUCCESSFUL_CODE.equals(this.code);
	}

	/**
	 * 失败
	 *
	 * @return true/false
	 */
	@JsonIgnore
	public boolean isFail() {
		return !isSuccess();
	}
}