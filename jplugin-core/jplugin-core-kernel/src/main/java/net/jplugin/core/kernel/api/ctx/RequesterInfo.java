package net.jplugin.core.kernel.api.ctx;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;

/**
 *
 * @author: LiuHang
 * @version 创建时间：2015-2-5 上午10:07:28
 **/

public class RequesterInfo {
	public static final String CLIENT_BROWSER="b";
	public static final String CLIENT_MBROWSER="mb";

	String traceId;
	String parSpanId;
	String clientType;
	String operatorToken;
	String operatorId;
	String currentTenantId;
	String clientAppToken;
	String clientAppCode;
	String callerIpAddress;
	String requestUrl;
	Content content=new Content();
	Cookies cookies=new Cookies();
	Headers headers=new Headers();
	
	//following for request content
	
	public String getOperatorId() {
		return operatorId;
	}
	public String getTraceId() {
		return traceId;
	}
	public void setTraceId(String tId) {
		this.traceId = tId;
	}

	public String getParSpanId() {
		return parSpanId;
	}
	public void setParSpanId(String parSpanId) {
		this.parSpanId = parSpanId;
	}
	
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	public String getClientType() {
		return clientType;
	}
	public void setClientType(String ct) {
		this.clientType = ct;
	}
	public String getOperatorToken() {
		return operatorToken;
	}
	public void setOperatorToken(String token) {
		this.operatorToken = token;
	}
//	public String getRequestId() {
//		return requestId;
//	}
//	public void setRequestId(String requestId) {
//		this.requestId = requestId;
//	}
	public String getCurrentTenantId() {
		return currentTenantId;
	}
	public void setCurrentTenantId(String mtid) {
		this.currentTenantId = mtid;
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
	public Content getContent() {
		return content;
	}
	public Cookies getCookies(){
		return cookies;
	}
	

	public Headers getHeaders() {
		return headers;
	}


	/**
	 * following for request content
	 */
	public static class Content{
		String contentType;
		Map<String,String> paramContent;
		String jsonContent;
//		Map mapForJsonContent;
		
		
		public String getContentType() {
			return contentType;
		}
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}
		public Map<String, String> getParamContent() {
			return paramContent;
		}
		public void setParamContent(Map<String, String> paramContent) {
			this.paramContent = paramContent;
		}
		public String getJsonContent() {
			return jsonContent;
		}
		public void setJsonContent(String jsonContent) {
			this.jsonContent = jsonContent;
			
//			//只有在content是Map的情况下才fillItemsToParamContent
//			if (!StringKit.isNull(jsonContent) && jsonContent.trim().startsWith("{")){
			try {
				fillItemsToParamContent();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Encoding error,jsoncontent:"+jsonContent,e);
			}
//			}
		}
		
		private void fillItemsToParamContent() throws UnsupportedEncodingException {
			if (paramContent==null) 
				paramContent = new HashMap<String,String>();
			
			if (StringKit.isNull(this.jsonContent))
				return;//不做任何转换，认为没参数

			//尝试做一遍decode,为节省开销，判断是%开头才进行
			jsonContent = jsonContent.trim();
			if (jsonContent.startsWith("%"))
				jsonContent = URLDecoder.decode(jsonContent,"utf-8");
			//应对_FULL_MATCH的情况，参数不是map，忽略
			if (!jsonContent.trim().startsWith("{"))
				return;
			
			Map map = JsonKit.json2Map(this.jsonContent);
			for (Object key : map.keySet()){
				if (!(key instanceof String)){
					throw new RuntimeException("the first level key must be String type now. "+jsonContent);
				}
				paramContent.put((String)key,JsonKit.object2JsonEx(map.get(key)));
			}
		}
		
//2016-12-08下面内容被注掉，因为需要把json模式下参数直接加入 paramContent，以备服务实现中访问
//		private void parseAndCacheJsonContent() {
//			 this.mapForJsonContent = JsonKit.json2Map(jsonContent);
//			 //这里做一个容错
//			 if (this.mapForJsonContent==null) 
//				 this.mapForJsonContent = new HashMap<String,String>(0);
//		}
//		public Map getMapForJsonContent() {
//			return mapForJsonContent;
//		}
	}
}
