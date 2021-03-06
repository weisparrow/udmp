/**
 * Copyright (c) 2005-2012 springside.org.cn
 */
package cn.com.git.udmp.common.utils;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;

import cn.com.git.udmp.common.exception.FrameworkException;

/**
 * 关于异常的工具类.
 * @author Spring Cao
 * @version 2016-2-6
 */
public class Exceptions extends ExceptionUtils{

	/**
	 * 将CheckedException转换为UncheckedException.
	 */
	public static FrameworkException unchecked(Exception e) {
		if (e instanceof RuntimeException) {
			return (FrameworkException) e;
		} else {
			return new FrameworkException("",e);
		}
	}

	/**
	 * 将ErrorStack转化为String. 改用父类的getStackTrace(Throwable throwable)方法
	 */
	public static String getStackTraceAsString(Throwable e) {
		if (e == null){
			return "";
		}
		StringWriter stringWriter = new StringWriter();
//		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	/**
	 * 判断异常是否由某些底层的异常引起.
	 */
	public static boolean isCausedBy(Exception ex, Class<? extends Exception>... causeExceptionClasses) {
		Throwable cause = ex.getCause();
		while (cause != null) {
			for (Class<? extends Exception> causeClass : causeExceptionClasses) {
				if (causeClass.isInstance(cause)) {
					return true;
				}
			}
			cause = cause.getCause();
		}
		return false;
	}

	/**
	 * 在request中获取异常类
	 * @param request
	 * @return 
	 */
	public static Throwable getThrowable(HttpServletRequest request){
		Throwable ex = null;
		if (request.getAttribute("exception") != null) {
			ex = (Throwable) request.getAttribute("exception");
		} else if (request.getAttribute("javax.servlet.error.exception") != null) {
			ex = (Throwable) request.getAttribute("javax.servlet.error.exception");
		}
		return ex;
	}
	
}
