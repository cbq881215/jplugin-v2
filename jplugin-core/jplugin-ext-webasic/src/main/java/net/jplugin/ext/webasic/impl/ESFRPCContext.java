package net.jplugin.ext.webasic.impl;

import net.jplugin.common.kits.RequestIdKit;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.kernel.api.ctx.RequesterInfo;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;
import net.jplugin.core.kernel.kits.KernelKit;

public class ESFRPCContext {
	String operatorToken;
	String operatorId;
	String clientAppToken;
	String clientAppCode;
	String callerIpAddress;
	String requestUrl;
	String tenantId;
	String globalReqId;
	long msgReceiveTime;

	public long getMsgReceiveTime() {
		return msgReceiveTime;
	}
	public void setMsgReceiveTime(long msgReceiveTime) {
		this.msgReceiveTime = msgReceiveTime;
	}
	
	public String getOperatorToken() {
		return operatorToken;
	}
	public void setOperatorToken(String operatorToken) {
		this.operatorToken = operatorToken;
	}
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getClientAppToken() {
		return clientAppToken;
	}
	public void setClientAppToken(String clientAppToken) {
		this.clientAppToken = clientAppToken;
	}
	public String getClientAppCode() {
		return clientAppCode;
	}
	public void setClientAppCode(String clientAppCode) {
		this.clientAppCode = clientAppCode;
	}
	public String getCallerIpAddress() {
		return callerIpAddress;
	}
	public void setCallerIpAddress(String callerIpAddress) {
		this.callerIpAddress = callerIpAddress;
	}
	
	public String getRequestUrl() {
		return requestUrl;
	}
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	
	public String getTenantId() {
		return tenantId;
	}
	
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getGlobalReqId() {
		return globalReqId;
	}
	public void setGlobalReqId(String globalReqId) {
		this.globalReqId = globalReqId;
	}
	public static void fill(ESFRPCContext ctx) {
		RequesterInfo info = ThreadLocalContextManager.getRequestInfo();
		info.setCallerIpAddress(ctx.getCallerIpAddress());
		info.setClientAppCode(ctx.getClientAppCode());
		info.setClientAppToken(ctx.getClientAppToken());
		info.setOperatorId(ctx.getOperatorId());
		info.setOperatorToken(ctx.getOperatorToken());
		info.setRequestUrl(ctx.getRequestUrl());
		
		//tenantid
		String tid = ctx.getTenantId();
//		if ("".equals(tid)) 
//			tid = null;
//		info.setCurrentTenantId(tid);
		MtInvocationFilterHandler.instance.checkAndSet(info, tid);
		
		
//		//reqid
//		String greqid = ctx.getGlobalReqId();
//		KernelKit.setTraceAndSpan(info,greqid);
	}
	

	
}
