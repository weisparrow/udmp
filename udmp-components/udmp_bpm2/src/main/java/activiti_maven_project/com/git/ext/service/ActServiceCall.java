package activiti_maven_project.com.git.ext.service;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activiti_maven_project.com.git.common.exception.BpmException;
import activiti_maven_project.com.git.common.util.JsonUtil;

public class ActServiceCall implements JavaDelegate {
	static Logger logger = LogManager.getLogger();
	
	private static Map<String,Client> map=new HashMap<String,Client>();
	
	private Expression input_field;

	private Expression method;
	
	private Expression wsdl;
	
	private Expression output_field;
	
	private int lastRunTimes= 3;
	
	private int sleeptime=1000;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		
		logger.debug("ActServiceCall begin...activity is ["+((ActivityExecution)execution).getActivity().getId()+"] fields is"+toString());
		
		String input=findResourceFormVariables(execution);
		String outJson="";
		String errorMessage="";
		boolean callSuccess = false;
		do {
			try{
				outJson=call(execution, input);
				callSuccess = true;// 调用成功
				lastRunTimes = 0;// 执行成功，则无需重试，直接退出
			} catch (Exception e) {
				e.printStackTrace();
				lastRunTimes--;// 可以重试的次数-1
				errorMessage = e.getMessage();
			Thread.sleep(sleeptime);// 延迟指定的毫秒数
			}
		}while (lastRunTimes > 0);
		
		if (callSuccess == false)// 重试N次后仍然失败
		{
			throw new BpmException(errorMessage);
		}
		
		if ("Fault.faultcode".equals(outJson)) {
			throw new BpmException(outJson);
		}
		
		
		
		saveVariableFromReturnValue(execution, outJson);
		
		logger.debug("ActServiceCall end...");
	}
	
	
	/**
	 * webservice 调用
	 * @param execution
	 * @param _json
	 * @return
	 * @throws Exception
	 */
	private String call(DelegateExecution execution,String _json) throws Exception{
		String wsdlUrl=((String)wsdl.getValue(execution)).trim();
		Client  cl;
		if(null==map.get(wsdlUrl)){
			JaxWsDynamicClientFactory  factory =JaxWsDynamicClientFactory.newInstance();
			cl =factory.createClient(wsdlUrl);
			map.put(wsdlUrl, cl);
		}else{
			 cl=map.get(wsdlUrl);
		}
		 Object[] obj =cl.invoke((String)method.getValue(execution),new Object[]{_json});
		 String rejson =obj[0].toString();
		 return rejson;
	}
	
	private String findResourceFormVariables(DelegateExecution execution){

		StringBuffer json=new StringBuffer();
		String input=(String)input_field.getValue(execution);
		
		Map<String,String> map= JsonUtil.getMap4Json(input);
		
		
		for(String key:map.keySet()){
			if(StringUtils.isBlank(json.toString())){
				json.append("{\"").append(map.get(key)).append("\":\"").append(execution.getVariable(key).toString()).append("\"");
			}else{
				json.append(",\"").append(map.get(key)).append("\":\"").append(execution.getVariable(key).toString()).append("\"");
			}
		}
		if(StringUtils.isNotBlank(json.toString())){
			json.append("}");
		}
		return json.toString();
	}

	
	private void saveVariableFromReturnValue(DelegateExecution execution,String json){
		Map<String, Object> jsonMaps = JsonUtil.getMap4Json(json);
		String output=(String)output_field.getValue(execution);
		Map<String,String>map= JsonUtil.getMap4Json(output);
		for(String key:map.keySet()){
			execution.setVariable(map.get(key), jsonMaps.get(key));
		}
		
	}

	
	private void checkParms(DelegateExecution execution) throws BpmException{
		if(null==input_field || null==input_field.getValue(execution) || StringUtils.isBlank((String)input_field.getValue(execution))){
			logger.error("webservice call field: [input_field] is null,process Id is " + execution.getId());
			throw new BpmException("webservice call field: [input_field] is null");
		}
		if(null==method || null==method.getValue(execution) || StringUtils.isBlank((String)method.getValue(execution))){
			logger.error("webservice call field: [method] is null,process Id is " + execution.getId());
			throw new BpmException("webservice call field: [method] is null");
		}	
		if(null==wsdl || null==wsdl.getValue(execution) || StringUtils.isBlank((String)wsdl.getValue(execution))){
			logger.error("webservice call field: [wsdl] is null,process Id is " + execution.getId());
			throw new BpmException("webservice call field: [wsdl] is null");
		}
		
	}
	
	
	

	public static Map<String, Client> getMap() {
		return map;
	}


	public static void setMap(Map<String, Client> map) {
		ActServiceCall.map = map;
	}


	public Expression getInput_field() {
		return input_field;
	}


	public void setInput_field(Expression input_field) {
		this.input_field = input_field;
	}


	public Expression getMethod() {
		return method;
	}


	public void setMethod(Expression method) {
		this.method = method;
	}


	public Expression getWsdl() {
		return wsdl;
	}


	public void setWsdl(Expression wsdl) {
		this.wsdl = wsdl;
	}


	public Expression getOutput_field() {
		return output_field;
	}


	public void setOutput_field(Expression output_field) {
		this.output_field = output_field;
	}


	public int getLastRunTimes() {
		return lastRunTimes;
	}


	public void setLastRunTimes(int lastRunTimes) {
		this.lastRunTimes = lastRunTimes;
	}


	public int getSleeptime() {
		return sleeptime;
	}


	public void setSleeptime(int sleeptime) {
		this.sleeptime = sleeptime;
	}


	@Override
	public String toString() {
		return "ActServiceCall [input_field=" + input_field + ", method=" + method + ", wsdl=" + wsdl
				+ ", output_field=" + output_field + ", lastRunTimes=" + lastRunTimes + ", sleeptime=" + sleeptime
				+ "]";
	}
	
	
}
