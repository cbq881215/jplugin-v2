package net.jplugin.ext.webasic.impl.restm.invoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.ReflactKit;
import net.jplugin.common.kits.SerializKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.config.api.RefConfig;
import net.jplugin.core.ctx.api.JsonResult;
import net.jplugin.core.ctx.api.RuleServiceFactory;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.kernel.api.RefAnnotationSupport;
import net.jplugin.core.kernel.api.ctx.RequesterInfo;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;
import net.jplugin.core.log.api.ILogService;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import net.jplugin.core.rclient.handler.RestHandler;
import net.jplugin.core.service.api.ServiceFactory;
import net.jplugin.ext.webasic.api.IDynamicService;
import net.jplugin.ext.webasic.api.InvocationContext;
import net.jplugin.ext.webasic.api.ObjectDefine;
import net.jplugin.ext.webasic.api.Para;
import net.jplugin.ext.webasic.impl.RemoteExceptionKits;
import net.jplugin.ext.webasic.impl.RemoteExceptionKits.RemoteExceptionInfo;
import net.jplugin.ext.webasic.impl.filter.IMethodCallback;
import net.jplugin.ext.webasic.impl.filter.MethodIllegleAccessException;
import net.jplugin.ext.webasic.impl.filter.service.ServiceFilterManager;
import net.jplugin.ext.webasic.impl.helper.ObjectCallHelper;
import net.jplugin.ext.webasic.impl.helper.ObjectCallHelper.ObjectAndMethod;
import net.jplugin.ext.webasic.impl.restm.RestMethodState;
import net.jplugin.ext.webasic.impl.restm.RestMethodState.State;

/**
 *
 * @author: LiuHang
 * @version 创建时间：2015-2-3 下午05:51:42
 **/

public class ServiceInvoker extends RefAnnotationSupport implements IServiceInvoker{
	ObjectCallHelper helper;
	
	/**
	 * @param value
	 */
	public ServiceInvoker(ObjectDefine d) {
		this.helper = new ObjectCallHelper(d);
		validate(this.helper);
	}

	private void validate(ObjectCallHelper helper) {
		Object o = helper.getObject();
		Method[] methods = o.getClass().getMethods();
		for (Method m:methods){
			
			Annotation[][] annos = m.getParameterAnnotations();
			for (Annotation[] arr:annos){
				for (Annotation a:arr){
					if (a instanceof Para){
						Para p = (Para) a;
						if (JsonResult.JSONP_FUNCTION_PARAM.equals(p.name())){
							throw new RuntimeException("Param annotation can't use same name with Jsonp callback param. "+ o.getClass().getName()+" "+m.getName());
						}
						
						if (Para.FULL_MATCH.equals(p.name()) && m.getParameterTypes().length!=1){
							//一定只有一个参数
							throw new RuntimeException("_FULL_MATCH_ can only be used for method with ONE parameter. "+ o.getClass().getName()+" "+m.getName());
						}
					}
				}
			}
		}
		
	}

	/**
	 * @param req
	 * @param paraAnootation
	 * @param parameterTypes
	 * @return
	 */
	private Object[] getParaValueFromRequest(CallParam req,
			Annotation[][] paraAnootation, Class<?>[] parameterTypes) {
		if (paraAnootation.length != parameterTypes.length){
			throw new RuntimeException("Method mismatch!");
		}
		Object[] ret = new Object[parameterTypes.length];
		
		//尝试处理FULL_MATCH模式
		if (tryDuleFullMatch(req,ret,paraAnootation,parameterTypes)){
			return ret;
		}
		
		Map paraMap = req.getParamMap();
		for (int i=0;i<parameterTypes.length;i++){
			ParaInfo paraInfo = getParaInfo(paraAnootation[i],i);
//			ParaInfo pi = getParaInfo(paraAnootation[i],i);
			ret[i] =  getFromRequest(paraMap,paraInfo,parameterTypes[i],i);
		}
		return ret;
	}

	private boolean tryDuleFullMatch(CallParam req, Object[] ret, Annotation[][] annos, Class<?>[] parameterTypes) {
		if (ret.length!=1) 
			return false; 
		
		Annotation[] anno = annos[0];
		for (Annotation a:anno){
			if (a.annotationType()==Para.class){
				String name = ((Para)a).name().trim();
				if (Para.FULL_MATCH.equals(name)){
					String fullJson;
					if (req.paramMap!=null && !req.paramMap.isEmpty()){
						//如果参数不为NULL或EMPTY，则肯定无需判断jsonContent.(如果是json方式调用，json内容也已经同步到paramMap里面了）
						//这里先判断MIX标记，如果MIX标记存在，直接用arg0，否则用整个内容
						if (RestHandler.MIX_PARA_VALUE.equals(req.paramMap.get(RestHandler.MIX_PARA)))
							fullJson = req.paramMap.get("arg0");
						else{
//							fullJson = JsonKit.object2JsonEx(req.paramMap);
							String jsonContent = ThreadLocalContextManager.getRequestInfo().getContent().getJsonContent();
							if (!StringKit.isNull(jsonContent)){
								fullJson = jsonContent;
							}else{
								//这种情况下，如果Map的Value是复杂类型{}或者[]，将不能有效转化！！！！！
								fullJson = JsonKit.object2JsonEx(req.paramMap);
							}
						}
					}else{
						//Json方式调用，并且参数表不为map的情况才会执行到这里
						String jsonContent = ThreadLocalContextManager.getRequestInfo().getContent().getJsonContent();
						if (!StringKit.isNull(jsonContent)){
							fullJson = jsonContent;
						}else{
							fullJson = null;
						}
					}
					
					//对结果赋值
					if (fullJson!=null){
						ret[0] = JsonKit.json2ObjectEx(fullJson, parameterTypes[0]);		
					}else{
						ret[0] = null;
						//检查必需性
						if (((Para)a).required()){
							throw new RuntimeException("Required param is not exists!"+req.operation);
						}
					}
					//返回true
					return true;
				}
			}
		}
		return false;
	}

	static class ParaInfo{
		String name;
		boolean required;
	}

	static HashMap<Class,Object> nullDefaultMap = new HashMap();
	static{
		nullDefaultMap.put(int.class, new Integer(0));
		nullDefaultMap.put(double.class, new Double(0));
		nullDefaultMap.put(float.class, new Float(0));
		nullDefaultMap.put(long.class, new Long(0));
	}
	
	private Object getFromRequest(Map paraMap, ParaInfo paraInfo,
			Class<?> clz, int idx) {
		if (!paraMap.containsKey(paraInfo.name) && paraInfo.required){
			throw new RuntimeException("Can't find http param:"+paraInfo.name);
		}
		
		String val=(String) paraMap.get(paraInfo.name);
		if (val==null){
			//判断是否混合参数名称模式
			if (RestHandler.MIX_PARA_VALUE.equals(paraMap.get(RestHandler.MIX_PARA))){
				val = (String) paraMap.get("arg"+idx);
			}
		}
		
		if (StringKit.isNull(val)){
			Object o = nullDefaultMap.get(clz);
			return o;
		}else{
			return JsonKit.json2ObjectEx(val, clz);
		}
	}


	private ParaInfo getParaInfo(Annotation[] anno,int index) {
		ParaInfo pi = new ParaInfo();
		String paramName = null;
		for (Annotation a:anno){
			if (a.annotationType() == Para.class){
				paramName = ((Para)a).name().trim();
				pi.name = paramName;
				pi.required= ((Para)a).required();
				break;
			}
		}
		if (StringKit.isNull(paramName)){
			paramName = "arg"+index;
			pi.name = paramName;
			pi.required= false;
		}
		return pi;
	}

	public void call(CallParam cp) throws Throwable{
		int callType = cp.getCallType();
		if (callType==CallParam.CALLTYPE_STRING_PARAM)
			callStringParam(cp);
		else if (callType==CallParam.CALLTYPE_REMOTE_CALL)
			callRemteCall(cp);
		else if (callType==CallParam.CALLTYPE_JSON)
			callJson(cp);
		else throw new RuntimeException("known call type");
	}

	private void callJson(CallParam cp) throws Throwable {
		//目前转换为StringParam调用,因为初始设置的时候参数已经Merge，这里不需要转换了。2016-12-08
		JsonCallHelper.convertToHttp(cp);
		callStringParam(cp);
	}
	private void callStringParam(CallParam cp) throws Throwable{
		Object o = helper.getObject();
		if (o instanceof IDynamicService){
			callStringParamForDynamic(cp);
		}else
			callStringParamForConcreate(cp);
	}
	

	//2020-2-19修改为如下，支持过滤器触发，同时支持Rule
	static Class[] dynamicParamTypes = new Class[]{RequesterInfo.class,String.class};
	private void callStringParamForDynamic(CallParam cp) throws Throwable{
		ObjectAndMethod oam = helper.get("execute", dynamicParamTypes);
		
		try{
			RestMethodState.reset();
			
			Object result = null;

			result = invokeWithServiceFilter4Dynamic(oam,cp.getPath(),cp.getOperation());
			
			State state = RestMethodState.get();

//			RuleResult rr = RuleResult.create(RuleResult.OK);
			JsonResult jr = JsonResult.create();
			jr.setCode(state.code);
			jr.setMsg(state.message);
			jr.setSuccess(state.success);
			
			HashMap<String, Object> hm = new HashMap();
			if (restCompatibleReturn){
				hm.put("return", result);
			}
			hm.put("result", result);
			jr.setContent(hm);
//			rr.setContent("return",result);
			cp.setResult(jr.toJson(getJsonFormat(cp)));
		}catch(MethodIllegleAccessException e){
			disposeException(cp, e);
		}catch(Exception e){
			//有Rule标记的情况下，InvocationTargetException会被去掉，所以只能在这里处理了
			disposeException(cp, e);
		}
	}
	
	private void callStringParamForConcreate(CallParam cp) throws Throwable{
		ObjectAndMethod oam = helper.get(cp.getOperation(), null);
		
		//得到参数annotation
		Annotation[][] paraAnootation;
		if (ObjectDefine.OBJ_BIZLOGIC.equals(helper.getObjeceDefine().getObjType())){
			//从接口获取meta
			Class intf = RuleServiceFactory.getRuleInterface(helper.getObjeceDefine().getBlName());
			paraAnootation = ReflactKit.findSingeMethodExactly(intf,oam.method.getName()).getParameterAnnotations();
		}else{
			//从类获取meta
			paraAnootation = oam.method.getParameterAnnotations();
		}
		
		//获取参数的值
		Object[] paraValue = getParaValueFromRequest(cp,paraAnootation,oam.method.getParameterTypes());
		
		try{
			RestMethodState.reset();
			
//			Object result = oam.method.invoke(oam.object, paraValue);
			Object result = null;
			
//			result = helper.invokeWithRuleSupport(oam,paraValue);
			result = invokeWithServiceFilter(cp.getPath(),oam,paraValue);

			State state = RestMethodState.get();

//			RuleResult rr = RuleResult.create(RuleResult.OK);
			JsonResult jr = JsonResult.create();
			jr.setCode(state.code);
			jr.setMsg(state.message);
			jr.setSuccess(state.success);
			
			HashMap<String, Object> hm = new HashMap();
			if (restCompatibleReturn){
				hm.put("return", result);
			}
			hm.put("result", result);
			jr.setContent(hm);
//			rr.setContent("return",result);
			cp.setResult(jr.toJson(getJsonFormat(cp)));
		}catch(InvocationTargetException e){
			Throwable targetEx = e.getTargetException();
			disposeException(cp, targetEx);
		}catch(MethodIllegleAccessException e){
			disposeException(cp, e);
		}catch(Exception e){
			//有Rule标记的情况下，InvocationTargetException会被去掉，所以只能在这里处理了
			disposeException(cp, e);
		}
	}

	@RefConfig(path="platform.service-export-format",defaultValue="1")
	Integer service_export_format;
	private Tuple2<Integer,String> getJsonFormat(CallParam cp) {
		int format;
		
		//get format
		String o = cp.getParamMap().get(JsonResult.JSON_FORMAT_INDICATOR);
		if (o!=null){
			format = Integer.parseInt(o);
		}else{
			format = service_export_format;
		}
		
		//get jsonp
		String cb = cp.getParamMap().get(JsonResult.JSONP_FUNCTION_PARAM);
		return Tuple2.with(format,cb);
	}

	private void disposeException(CallParam cp, Throwable e) {
		RemoteExceptionInfo exInfo = RemoteExceptionKits.getExceptionInfo(e);

		JsonResult jr = JsonResult.create();
		jr.setSuccess(false);
		jr.setMsg(exInfo.getMsg());//get message
		jr.setCode(exInfo.getCode());//get code
		cp.setResult(jr.toJson(getJsonFormat(cp)));
		if (!
				(
						(e instanceof RemoteExecuteException) 
						&& 
						(!((RemoteExecuteException) e).isNeedLog())
				)
			)
			ServiceFactory.getService(ILogService.class).getLogger(this.getClass().getName()).error(e.getMessage(),e);
	}

	//为了兼容 return节点
	static Boolean restCompatibleReturn;
	public static void initCompatibleReturn(){
		String cfg = ConfigFactory.getStringConfig("platform.rest-compatible-return");
		if (cfg!=null) cfg = cfg.trim();
		if ("true".equalsIgnoreCase(cfg)){
			restCompatibleReturn = true;
		}else{
			restCompatibleReturn = false;
		}
		PluginEnvirement.INSTANCE.getStartLogger().log("platform.rest-compatible-return = "+restCompatibleReturn);
	}
//	
//	private boolean compatibleReturn() {
//		if (restCompatibleReturn==null){
//			synchronized (this) {
//				String cfg = ConfigFactory.getStringConfig("platform.rest-compatible-return");
//				if (cfg!=null) cfg = cfg.trim();
//				if ("true".equals(cfg)){
//					restCompatibleReturn = true;
//				}else{
//					restCompatibleReturn = false;
//				}
//				System.out.println("platform.rest-compatible-return = "+restCompatibleReturn);
//			}
//		}
//		return restCompatibleReturn;
//	}

	private Object invokeWithServiceFilter(String servicePath,final ObjectAndMethod oam, final Object[] paraValue) throws Throwable {
		InvocationContext ctx = new InvocationContext(servicePath,oam.object,oam.method,paraValue);

		return ServiceFilterManager.INSTANCE.executeWithFilter(ctx, new IMethodCallback() {
			public Object run() throws Throwable {
				return helper.invokeWithRuleSupport(oam,paraValue);
			}
		});
	}


	private Object invokeWithServiceFilter4Dynamic(final ObjectAndMethod oam,String servicePath,String dyncmicPath) throws Throwable {
		InvocationContext ctx = new InvocationContext(servicePath,oam.object,oam.method,dyncmicPath);
		
		Object[] paraValue = new Object[]{ctx.getRequestInfo(),ctx.getDynamicPath()};	
		return ServiceFilterManager.INSTANCE.executeWithFilter(ctx, new IMethodCallback() {
			public Object run() throws Throwable {
				return helper.invokeWithRuleSupport(oam,paraValue);
			}
		});
	}
	public ObjectCallHelper getObjectCallHelper() {
		return this.helper;
	}
	
	//<<<<<<<<<<<<<<<<<<<<<<以下调用RemoteCall
	private void callRemteCall(CallParam cp) throws Throwable {
		Class[] paraType = getParaTypes(cp);
		Object[] paraValue = getParaValues(cp);
		ObjectAndMethod oam = helper.get(cp.operation, paraType);
		
		try{
//			Object result = oam.method.invoke(oam.object, paraValue);
//			Object result = helper.invokeWithRuleSupport(oam,paraValue);
			Object result = invokeWithServiceFilter(cp.getPath(),oam,paraValue);
			cp.setResult(toResultString(result));
		}catch(InvocationTargetException ite){
			Throwable targetEx = ite.getTargetException();
			RemoteExceptionInfo exInfo = RemoteExceptionKits.getExceptionInfo(targetEx);

			Object result = CallParam.REMOTE_EXCEPTION_PREFIX + JsonKit.object2Json(exInfo);
			cp.setResult(toResultString(result));
			log(targetEx);
		}catch(MethodIllegleAccessException e){
			RemoteExceptionInfo exInfo = RemoteExceptionKits.getExceptionInfo(e);
			Object result = CallParam.REMOTE_EXCEPTION_PREFIX + JsonKit.object2Json(exInfo);
			cp.setResult(toResultString(result));
			log(e);
		}catch(Exception e){
			//有Rule标记的情况下，InvocationTargetException会被去掉，所以只能在这里处理了
			RemoteExceptionInfo exInfo = RemoteExceptionKits.getExceptionInfo(e);
			Object result = CallParam.REMOTE_EXCEPTION_PREFIX + JsonKit.object2Json(exInfo);
			cp.setResult(toResultString(result));
			log(e);
		}
	}

	/**
	 * @param cp
	 * @return
	 */
	private Object[] getParaValues(CallParam cp) {
		String s = cp.getParamMap().get(CallParam.REMOTE_PARAVALUES_KEY);
		if (StringKit.isNull(s)){
			throw new RuntimeException("para value is null.");
		}
		return (Object[]) SerializKit.deserialFromString(s);
	}
	/**
	 * @param cp
	 * @return
	 */
	private Class[] getParaTypes(CallParam cp) {
		String s = cp.getParamMap().get(CallParam.REMOTE_PARATYPES_KEY);
		if (StringKit.isNull(s)){
			throw new RuntimeException("para type is null.");
		}
		return (Class[]) SerializKit.deserialFromString(s);
	}
	/**
	 * @param result
	 * @return
	 */
	private String toResultString(Object result) {
		return SerializKit.encodeToString(result);
	}
	/**
	 * @param targetException
	 */
	private void log(Throwable targetException) {
		ServiceFactory.getService(ILogService.class).getLogger(this.getClass().getName()).error(targetException);
	}
	
	//以上调用RemoteCall>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
}
