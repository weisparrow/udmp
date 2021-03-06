<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="url" type="java.lang.String" required="true" description="树结构数据地址"%>
<%@ attribute name="checked" type="java.lang.Boolean" required="false" description="是否显示复选框，如果不需要返回父节点，请设置notAllowSelectParent为true"%>
<%@ attribute name="extId" type="java.lang.String" required="false" description="排除掉的编号（不能选择的编号）"%>
<%@ attribute name="isAll" type="java.lang.Boolean" required="false" description="是否列出全部数据，设置true则不进行数据权限过滤（目前仅对Office有效）"%>
<%@ attribute name="notAllowSelectRoot" type="java.lang.Boolean" required="false" description="不允许选择根节点"%>
<%@ attribute name="notAllowSelectParent" type="java.lang.Boolean" required="false" description="不允许选择父节点"%>
<%@ attribute name="module" type="java.lang.String" required="false" description="过滤栏目模型（只显示指定模型，仅针对CMS的Category树）"%>
<%@ attribute name="selectScopeModule" type="java.lang.Boolean" required="false" description="选择范围内的模型（控制不能选择公共模型，不能选择本栏目外的模型）（仅针对CMS的Category树）"%>
<%@ attribute name="allowClear" type="java.lang.Boolean" required="false" description="是否允许清除"%>
<%@ attribute name="allowInput" type="java.lang.Boolean" required="false" description="文本框可填写"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="smallBtn" type="java.lang.Boolean" required="false" description="缩小按钮显示"%>
<%@ attribute name="hideBtn" type="java.lang.Boolean" required="false" description="是否显示按钮"%>
<%@ attribute name="disabled" type="java.lang.String" required="false" description="是否限制选择，如果限制，设置为disabled"%>
<%@ attribute name="dataMsgRequired" type="java.lang.String" required="false" description=""%>
<%@ attribute name="returnField" type="java.lang.String" required="false" description="返回字段('id'/'code')，默认返回id"%>

<input id="${id}Id" name="${name}" type="hidden" value="${value}"/>
<div class="input-group">
	<input id="${id}Name" name="${labelName}" ${allowInput?'':'readonly="readonly"'} type="text" value="${labelValue}"
		<c:if test="${dataMsgRequired != null && dataMsgRequired != ''}">
		 data-bv-notempty="true" data-bv-trigger="blur" data-bv-notempty-message="${dataMsgRequired}"
		</c:if>
		style="${cssStyle}" placeholder="${title}" class="${cssClass}" onclick="showSelectModal_${id}();">
	<c:if test="${hideBtn != true}">
	<span class="input-group-btn">
		<button id="${id}Button" class="btn btn-default ${disabled ? 'disabled' : ''}"
		type="button" onclick="showSelectModal_${id}();"><span class="glyphicon glyphicon-search"></span></button>
	</span>
	</c:if>
</div>
<script type="text/javascript">
	function showSelectModal_${id}() {
		// 是否限制选择，如果限制，设置为disabled
		if ($("#${id}Button").hasClass("disabled")){
			return true;
		}
		top.showModal("${ctx}/tag/treeselect?url=" + encodeURIComponent("${url}")
			+ "&module=${module}&checked=${checked}&extId=${extId}&isAll=${isAll}&selectIds=" + $("#${id}Id").val()
			+ "&notAllowSelectParent=${notAllowSelectParent}&notAllowSelectRoot=${notAllowSelectRoot}"
			+ "&selectScopeModule=${selectScopeModule}&allowClear=${allowClear}", 
			"", {title:'选择${title}', width:350, height:280 , overflow:'auto' }, null, function() {
				
				if(${returnField != null && returnField != '' && returnField == 'code'}){
					$("#${id}Id").val(top.getModalData("codes"));
				} else {
					$("#${id}Id").val(top.getModalData("ids"));
				}
				$("#${id}Name").val(top.getModalData("names"));
				$("#${id}Name").blur();// 失去焦点, 校验合法性
			});
	}
</script>