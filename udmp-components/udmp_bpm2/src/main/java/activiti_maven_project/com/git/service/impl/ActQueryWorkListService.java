package activiti_maven_project.com.git.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.NativeHistoricProcessInstanceQuery;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import activiti_maven_project.com.git.common.Page;
import activiti_maven_project.com.git.common.exception.BpmException;
import activiti_maven_project.com.git.common.vo.ActHisActInstListPage;
import activiti_maven_project.com.git.common.vo.ActHisActInstListVO;
import activiti_maven_project.com.git.common.vo.ActHisProcessListPage;
import activiti_maven_project.com.git.common.vo.ActHisProcessListVO;
import activiti_maven_project.com.git.common.vo.ActHisTaskListPage;
import activiti_maven_project.com.git.common.vo.ActHisTaskListVO;
import activiti_maven_project.com.git.common.vo.ActTaskCountVO;
import activiti_maven_project.com.git.common.vo.ActWorkListPage;
import activiti_maven_project.com.git.common.vo.ActWorkListVO;
import activiti_maven_project.com.git.mybatis.client.SqlQueryMapper;
import activiti_maven_project.com.git.service.QueryWorkListService;
import activiti_maven_project.com.git.service.TaskService_;
import net.sf.json.JSONNull;

@Service("actQueryWorkListService")
public class ActQueryWorkListService implements QueryWorkListService {

	private static Logger log = LogManager.getLogger();

	@Autowired(required = true)
	private SqlQueryMapper sqlQueryMapper;

	@Resource
	private TaskService taskService;
	
	@Resource
	private TaskService_ taskService_;

	@Resource
	private HistoryService historyService;
	
	@Resource
	private IdentityService identityService;
	
	@Resource
	private ManagementService managementService;
	
	/**
	 * 按照参数条件查询个人待办任务
	 * 
	 * @param userId
	 * @param orgCd
	 * @param roleCd
	 * @param _map
	 * @return
	 * @throws BpmException
	 */
	public ActWorkListPage queryWorkList(String userId, java.util.Map<String, Object> _map, Page _page) throws BpmException {
		
		/**
		 * 使用taskCandidateOrAssigned进行查询
		 * 
		 * 	if(如果组织机构通过扩展实现)
		 *	List<Group> groups=identityService.createGroupQuery().groupMember(userId).list();
		 *  List<String> groupIds = new ArrayList<String>();
		 *  for (Group group : groups) {
		 *    groupIds.add(group.getId());
		 *  }
		 *	TaskQuery tq = taskService.createTaskQuery().taskCandidateOrAssigned(userId).taskCandidateGroupIn(groupIds).active().includeProcessVariables();
		 *	if(如果组织机构未扩展实现)
		 *	TaskQuery tq = taskService.createTaskQuery().taskCandidateOrAssigned(userId).active().includeProcessVariables();
		 *
		 *  使用includeProcessVariables时activiti在分页查询时会通过程序在内存中分页
		 *  
		 */	
		
		ActWorkListPage alp=new ActWorkListPage();
		TaskQuery tq = taskService.createTaskQuery().taskAssignee(userId).active().includeProcessVariables();
		if (null != _map) {
			for (String dataKey : _map.keySet()) {
				if(null!=_map.get(dataKey) && !(_map.get(dataKey) instanceof JSONNull)){
					Object obj=_map.get(dataKey);
					tq.processVariableValueEquals(dataKey, _map.get(dataKey));
				}
			}
		}
		
		tq.orderByTaskDueDate().asc();
		long count =tq.count();
		List<Task> list =new ArrayList<Task>();
		if (null != _page) {
			list= tq.listPage(_page.getStart_index(), _page.getPageSize());
		} else {
			list= tq.list();
		}
		
		if(null==_page){
			_page=new Page();
		}
		_page.setTotalRecord(count);
		alp.setActWorkListVOs(pottingList(ActWorkListVO.class,list));
		alp.setPage(_page);
		return alp;
	}

	public Long conutWorkList(String userId, String orgCd, String roleCd, java.util.Map<String, Object> _map)
			throws BpmException {
		TaskQuery tq = taskService.createTaskQuery().taskAssignee(userId).active();
		if (null != _map) {
			for (String dataKey : _map.keySet()) {
				tq.processVariableValueEquals(dataKey, _map.get(dataKey));
			}
		}
		long count = tq.count();
		return count;

	}

	/**
	 * 按照参数条件查询池任务
	 * 
	 * @param userId
	 * @param orgCd
	 * @param roleCd
	 * @param _map
	 * @return
	 * @throws BpmException
	 */
	public ActWorkListPage queryCandiWorkList(String _candidateUserId, java.util.Map<String, Object> _map, Page _page)
			throws BpmException {
		ActWorkListPage alp=new ActWorkListPage();
		TaskQuery tq = taskService.createTaskQuery().taskCandidateUser(_candidateUserId).includeProcessVariables();
		if (null != _map) {
			for (String dataKey : _map.keySet()) {
				if(null!=_map.get(dataKey) && !(_map.get(dataKey) instanceof JSONNull)){
					tq.processVariableValueEquals(dataKey, _map.get(dataKey));
				}
			}
		}
		
		long count =tq.count();
		
		tq.orderByTaskDueDate().asc();
		List<Task> list =new ArrayList<Task>();
		if (null != _page) {
			list= tq.listPage(_page.getStart_index(), _page.getPageSize());
		} else {
			list= tq.list();
		}
		if(null==_page){
			_page=new Page();
		}
		_page.setTotalRecord(count);
		alp.setActWorkListVOs(pottingList(ActWorkListVO.class,list));
		alp.setPage(_page);
		return alp;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param <E>
	 * @param list
	 * @return
	 * @throws BpmException
	 */
	private <T, K> List<K> pottingList(Class<K> cls,List<T> actList) throws BpmException {
		List<K> ks=new ArrayList<K>();
		for (T t : actList) {
			try {
				K k = cls.newInstance();
				PropertyUtils.copyProperties(k,t);
				ks.add(k);
			} catch (IllegalAccessException e) {
				throw new BpmException(e.getMessage());
			} catch (InvocationTargetException e) {
				throw new BpmException(e.getMessage());
			} catch (NoSuchMethodException e) {
				throw new BpmException(e.getMessage());
			} catch (InstantiationException e) {
				throw new BpmException(e.getMessage());
			}
			
		}
		return ks;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param <E>
	 * @param list
	 * @return
	 * @throws BpmException
	 */
	private <T, K> List<K> pottingList(Class<K> cls,List<T> actList,List<T> actList2) throws BpmException {
		List<K> ks=new ArrayList<K>();
		for (T t : actList) {
			for(T t2:actList2){
				
			}
			try {
				K k = cls.newInstance();
				PropertyUtils.copyProperties(k,t);
				ks.add(k);
			} catch (IllegalAccessException e) {
				throw new BpmException(e.getMessage());
			} catch (InvocationTargetException e) {
				throw new BpmException(e.getMessage());
			} catch (NoSuchMethodException e) {
				throw new BpmException(e.getMessage());
			} catch (InstantiationException e) {
				throw new BpmException(e.getMessage());
			}
			
		}
		return ks;
	}
	
	

	public Long conutCandiWorkList(String _candidateUserId, java.util.Map<String, Object> _map) throws BpmException {
		TaskQuery tq = taskService.createTaskQuery().taskCandidateUser(_candidateUserId);
		if (null != _map) {
			for (String dataKey : _map.keySet()) {
				tq.processVariableValueEquals(dataKey, _map.get(dataKey));
			}
		}
		long count = tq.count();
		return count;
	}

	/**
	 * 按照参数条件查询挂起任务
	 * 
	 * @param userId
	 * @param orgCd
	 * @param roleCd
	 * @param _map
	 * @return
	 * @throws BpmException
	 */
	public ActWorkListPage querySuspendList(String userId, java.util.Map<String, Object> _map, Page _page) throws BpmException {
		ActWorkListPage alp=new ActWorkListPage();
		TaskQuery tq = taskService.createTaskQuery().taskAssignee(userId).suspended().includeProcessVariables();
		if (null != _map) {
			for (String dataKey : _map.keySet()) {
				tq.processVariableValueEquals(dataKey, _map.get(dataKey));
			}
		}
		tq.orderByTaskDueDate().asc();
		List<Task> list =new ArrayList<Task>();
		if (null != _page) {
			list= tq.listPage(_page.getStart_index(), _page.getPageSize());
		} else {
			list= tq.list();
		}
		long count = this.conutCandiWorkList(userId, _map);
		
		_page.setTotalRecord(count);
		alp.setActWorkListVOs(pottingList(ActWorkListVO.class,list));
		alp.setPage(_page);
		return alp;
	}

	/**
	 * 
	 */
	public Long conutSuspendList(String userId, java.util.Map<String, Object> _map) throws BpmException {
		TaskQuery tq = taskService.createTaskQuery().taskAssignee(userId).suspended();
		if (null != _map) {
			for (String dataKey : _map.keySet()) {
				tq.processVariableValueEquals(dataKey, _map.get(dataKey));
			}
		}
		long count = tq.count();
		return count;
	}

	/**
	 * 按照参数条件查询历史任务
	 * 
	 * @param userId
	 * @param orgCd
	 * @param roleCd
	 * @param _map
	 * @return
	 * @throws BpmException
	 */
	public ActHisProcessListPage queryHisList(String userId, java.util.Map<String, Object> _map, Page _page)
			throws BpmException {
//		HistoricProcessInstanceQuery hpq = historyService.createHistoricProcessInstanceQuery().involvedUser(userId).excludeSubprocesses(true)
//				.orderByProcessInstanceStartTime().desc();
//		if(_map.containsKey("template")){
//			hpq.processDefinitionName((String)_map.get("template"));
//		}
				
		StringBuffer sql=new StringBuffer();
		sql.append(" SELECT DISTINCT R.*,DEF.KEY_ AS PROC_DEF_KEY_,DEF.NAME_ AS PROC_DEF_NAME_,DEF.VERSION_ AS PROC_DEF_VERSION_,DEF.DEPLOYMENT_ID_ AS DEPLOYMENT_ID_");
		sql.append(" FROM ").append(managementService.getTableName(HistoricProcessInstance.class)).append(" R");
		sql.append(" LEFT OUTER JOIN ").append(managementService.getTableName(ProcessDefinitionEntity.class)).append(" DEF ON R.PROC_DEF_ID_ = DEF.ID_");
		sql.append(" WHERE R.ID_ IN( ");
		sql.append(" SELECT DISTINCT RES.ID_ FROM ").append(managementService.getTableName(HistoricProcessInstance.class)).append(" RES WHERE(");
		sql.append(" EXISTS (SELECT LINK.USER_ID_ FROM ").append(managementService.getTableName(HistoricIdentityLinkEntity.class)).append(" LINK WHERE USER_ID_ = #{userId}	AND LINK.PROC_INST_ID_ = RES.ID_))");
		sql.append(" AND RES.SUPER_PROCESS_INSTANCE_ID_ IS NULL");
		sql.append(" UNION ");
		sql.append(" SELECT DISTINCT RES.SUPER_PROCESS_INSTANCE_ID_ FROM ").append(managementService.getTableName(HistoricProcessInstance.class)).append("  RES WHERE(");
		sql.append(" EXISTS (SELECT LINK.USER_ID_ FROM ").append(managementService.getTableName(HistoricIdentityLinkEntity.class)).append(" LINK WHERE USER_ID_ = #{userId}	AND LINK.PROC_INST_ID_ = RES.ID_))");
		sql.append(" AND RES.SUPER_PROCESS_INSTANCE_ID_ IS NOT NULL )");
		
		if(_map.containsKey("template")){
			sql.append(" AND DEF.NAME_ LIKE #{template}");
		}
		
		
		sql.append(" ORDER BY R.START_TIME_ DESC ");
		
		log.debug("queryHisList sql==={}",sql.toString());
		
		NativeHistoricProcessInstanceQuery nq=historyService.createNativeHistoricProcessInstanceQuery().sql(sql.toString());
		nq.parameter("userId", userId);
		if(_map.containsKey("template")){
			nq.parameter("template", "%"+(String)_map.get("template")+"%");
		}
		
		List<HistoricProcessInstance> list=new ArrayList<HistoricProcessInstance>();
		
		long count=nq.count();
		
		
		if (null != _page) {
//			list= hpq.listPage(_page.getStart_index(), _page.getPageSize());
			list=nq.listPage(_page.getStart_index(), _page.getPageSize());
		} else {
//			list= hpq.list();
			list=nq.list();
		}
		if(null==_page){
			_page=new Page();
		}
		_page.setTotalRecord(count);
		ActHisProcessListPage ahp=new ActHisProcessListPage();
		ahp.setActHisProcessListVOs(pottingList(ActHisProcessListVO.class,list));
		ahp.setPage(_page);
		return ahp;
	}

	/**
	 * 
	 */
	public ActHisProcessListPage queryFinishHisList(String userId, java.util.Map<String, Object> _map,
			Page _page) throws BpmException {
		HistoricProcessInstanceQuery hpq = historyService.createHistoricProcessInstanceQuery().involvedUser(userId)
				.orderByProcessInstanceStartTime().desc().finished();
		List<HistoricProcessInstance> list=new ArrayList<HistoricProcessInstance>();
		long count=hpq.count();
		if (null != _page) {
			list= hpq.listPage(_page.getStart_index(), _page.getPageSize());
		} else {
			list= hpq.list();
		}
		if(null==_page){
			_page=new Page();
		}
		_page.setTotalRecord(count);
		ActHisProcessListPage ahp=new ActHisProcessListPage();
		ahp.setActHisProcessListVOs(pottingList(ActHisProcessListVO.class,list));
		ahp.setPage(_page);
		return ahp;
	}

	public ActHisProcessListPage queryhisListAll(java.util.Map<String, Object> _map, Page _page) throws BpmException {
		HistoricProcessInstanceQuery hpq = historyService.createHistoricProcessInstanceQuery()
				.orderByProcessInstanceStartTime().desc();
		List<HistoricProcessInstance> list=new ArrayList<HistoricProcessInstance>();
		long count=hpq.count();
		if (null != _page) {
			list= hpq.listPage(_page.getStart_index(), _page.getPageSize());
		} else {
			list= hpq.list();
		}
		if(null==_page){
			_page=new Page();
		}
		_page.setTotalRecord(count);
		ActHisProcessListPage ahp=new ActHisProcessListPage();
		ahp.setActHisProcessListVOs(pottingList(ActHisProcessListVO.class,list));
		ahp.setPage(_page);
		return ahp;
	}

	public Long conutHisList(String userId, java.util.Map<String, Object> _map) throws BpmException {
		long count = historyService.createHistoricProcessInstanceQuery().involvedUser(userId).count();
		return count;
	}

	
	/**
	 * 根据历史流程实例id查询历史任务实例
	 * @param _HprocessId
	 * @param _map
	 * @param _page
	 * @return
	 * @throws BpmException
	 */
	public ActHisActInstListPage queryHisTaskList(String _HprocessId,  java.util.Map<String, Object> _map,Page _page)
			throws BpmException {
		
		HistoricProcessInstance hpi=historyService.createHistoricProcessInstanceQuery().processInstanceId(_HprocessId).singleResult();
		
		HistoricActivityInstanceQuery ahiq=historyService.createHistoricActivityInstanceQuery().processInstanceId(_HprocessId).orderByHistoricActivityInstanceId().desc();
		
		List<ActHisActInstListVO> actList=new ArrayList<ActHisActInstListVO>();
		
		List<HistoricActivityInstance>	list= ahiq.list();
		
		ActHisActInstListPage atl=new ActHisActInstListPage();
		List<ActHisActInstListVO> ls=pottingList(ActHisActInstListVO.class,list);
		
		for(ActHisActInstListVO l :ls){
			if(l.getActivityType().equals("exclusiveGateway") || l.getActivityType().equals("parallelgateway")|| l.getActivityType().equals("inclusivegateway") || l.getActivityType().equals("eventgateway")){
				
			}else{
				if(StringUtils.isNotBlank(l.getTaskId())){
					l.setOpinion(taskService_.findCommentsForTask(l.getTaskId()));
				}
				actList.add(l);
			}
			
		}
		atl.setParentProcessId(hpi.getSuperProcessInstanceId());
		atl.setActHisActInstListVOs(actList);
		atl.setPage(_page);
		return atl;

	}
	
	public List<Task> queryRunTaskList(String _processId,  java.util.Map<String, Object> _map,Page _page)
			throws BpmException {
		TaskQuery tq=taskService.createTaskQuery().executionId(_processId).orderByTaskDueDate().asc();
		if(null!=_page){
			return tq.listPage(_page.getStart_index(), _page.getPageSize());
		}else{
			return  tq.list();
		}

	}

	public Long conutHisTaskList(String _HprocessId,  java.util.Map<String, Object> _map)
			throws BpmException {
		HistoricTaskInstanceQuery htq=historyService.createHistoricTaskInstanceQuery().executionId(_HprocessId).orderByTaskDueDate().asc();
		return htq.count();
	}
	
	
	public Map<String,Long> countWorkForTemplate(String userId) { 
		
		List<Map<String,Object>> list=sqlQueryMapper.countWorkByTemplate(userId);
		
//		List<ActTaskCountVO> atcs =new ArrayList<ActTaskCountVO>();
		Map<String,Long> rmap=new HashMap<String,Long>();
		
		for(Map<String,Object> map:list ){
			String tNmae=(String)map.get("pdi");
			Long con=(Long)map.get("con");
			tNmae=tNmae.substring(0, tNmae.indexOf(":"));
			if(rmap.containsKey(tNmae)){
				Long count=(Long)rmap.get(tNmae);
				count=count+con;
				rmap.put(tNmae, count);
			}else{
				rmap.put(tNmae, con);
			}
			
		}
		
		log.debug("rmap===={}",rmap);
		return rmap;
	}
	
	public Map<String,Long> countCandiWorkByTemplate(String userId) {
		
		
		List<Group> gs=identityService.createGroupQuery().groupMember(userId).list();
		
		List<String> ll=new ArrayList<String>();
		for(Group g:gs){
			ll.add(g.getId());
		}
		String[] ids=new String[ll.size()];
		ll.toArray(ids);
		List<Map<String,Object>> list=sqlQueryMapper.countCandiWorkByTemplate(userId,ids);
		
//		List<ActTaskCountVO> atcs =new ArrayList<ActTaskCountVO>();
		Map<String,Long> rmap=new HashMap<String,Long>();
		
		for(Map<String,Object> map:list ){
			String tNmae=(String)map.get("pdi");
			Long con=(Long)map.get("con");
			tNmae=tNmae.substring(0, tNmae.indexOf(":"));
			if(rmap.containsKey(tNmae)){
				Long count=(Long)rmap.get(tNmae);
				count=count+con;
				rmap.put(tNmae, count);
			}else{
				rmap.put(tNmae, con);
			}
			
		}
		
		log.debug("rmap===={}",rmap);
		return rmap;
	}

	@Override
	public ActHisTaskListPage queryHisMeetingTaskList(String _HprocessId, Map<String, Object> _map, Page _page)
			throws BpmException {

		
		List<HistoricTaskInstance> hts=historyService.createHistoricTaskInstanceQuery().processInstanceId(_HprocessId).orderByTaskCreateTime().desc().list();
		List<HistoricTaskInstance> htm=new ArrayList<HistoricTaskInstance>();
		
		String taskKey="";
		boolean flag=true;
		
		for(int i=0;i<hts.size()-1&&flag==true;i++){
			if(StringUtils.isBlank(taskKey)){
				if(hts.get(i).getTaskDefinitionKey().equals(hts.get(i+1).getTaskDefinitionKey())){
					htm.add(hts.get(i));
					htm.add(hts.get(i+1));
					taskKey=hts.get(i).getTaskDefinitionKey();
					i++;
				}
			}else{
				if(hts.get(i).getTaskDefinitionKey().equals(taskKey)){
					htm.add(hts.get(i));
				}else if(!hts.get(i).getTaskDefinitionKey().equals(taskKey)){
					flag=false;
				}
			}
		}
		
		
		ActHisTaskListPage atl=new ActHisTaskListPage();
		List<ActHisTaskListVO> ls=pottingList(ActHisTaskListVO.class,htm);
		
		for(ActHisTaskListVO l :ls){
			l.setOpinion(taskService_.findCommentsForTask(l.getId()));
		}
		
		atl.setActHisTaskListVOs(ls);
		atl.setPage(_page);
		return atl;
	}
	
	

//	private Map checkQueryMap(String userId, String orgCd, String roleCd, java.util.Map<String, Object> _map)
//			throws BpmException {
//		if (null != userId && !"".equals(userId)) {
//			_map.put("userId", userId);
//		} else {
//			throw new BpmException("查询时userId不能为：【" + userId + "】");
//		}
//		if (null != orgCd && !"".equals(orgCd)) {
//			_map.put("userId", orgCd);
//		}
//		if (null != roleCd && !"".equals(roleCd)) {
//			_map.put("userId", roleCd);
//		}
//		return _map;
//	}
}