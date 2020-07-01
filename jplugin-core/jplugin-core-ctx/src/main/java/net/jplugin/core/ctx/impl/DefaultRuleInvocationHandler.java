package net.jplugin.core.ctx.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.jplugin.core.ctx.api.Rule;
import net.jplugin.core.ctx.api.TransactionManager;
import net.jplugin.core.ctx.api.Rule.TxType;
import net.jplugin.core.ctx.api.RuleServiceFilterContext;
import net.jplugin.core.ctx.api.TransactionManager.Status;
import net.jplugin.core.kernel.api.PluginFilterManager;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContext;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;
import net.jplugin.core.service.api.ServiceFactory;

/**
 * 
 * @author: LiuHang
 * @version 创建时间：2015-2-12 上午09:08:37
 **/

public class DefaultRuleInvocationHandler implements RuleInvocationHandler {

	static private TransactionManagerAdaptor txm;
	private static PluginFilterManager<RuleServiceFilterContext> filterManager = new PluginFilterManager<>(
			net.jplugin.core.ctx.Plugin.EP_RULE_SERVICE_FILTER, (fc, ctx) -> {
				return invokeWithTx(ctx.getProxyObject(), ctx.getObject(), ctx.getMethod(), ctx.getArgs(), ctx.getAnnotation());
			});

	
	public static void init(){
		filterManager.init();
		initTx();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.luis.plugin.ctx.impl.RuleInvocationHandler#invoke(java.lang.Object,
	 * java.lang.Object, java.lang.reflect.Method, java.lang.Object[],
	 * net.luis.plugin.ctx.impl.Rule)
	 */
	public static Object invokeWithLog(Object proxyObj, Object oldService, Method method,
			Object[] args, Rule meta) throws Throwable {
		Throwable throwable = null;
		RuleInvocationContext invokCtx = new RuleInvocationContext();
		invokCtx.begin(method, args, meta);
		try {
			return method.invoke(oldService, args);
		} catch (Throwable th) {
			throwable = getRethrow(th);
			throw throwable;
		} finally {
			invokCtx.end(throwable);
		}
	}
	public Object invoke(Object proxyObj, Object oldService, Method method,
			Object[] args, Rule meta) throws Throwable {
		boolean isCreate=false;
		try{
			if (ThreadLocalContextManager.instance.getContext() == null){
				ThreadLocalContextManager.instance.createContext();
				isCreate = true;
			}
			return filterManager.filter(RuleServiceFilterContext.create(proxyObj, oldService, method, args, meta));
//			return invokeWithTx(proxyObj,oldService,method,args,meta);
		}finally{
			if (isCreate){
				ThreadLocalContextManager.instance.releaseContext();
			}
		}
	}

	/**
	 * @param oldService
	 * @param args
	 * @param meta
	 */
	public static Object invokeWithTx(Object proxyObj, Object oldService, Method method,
			Object[] args, Rule meta) throws Throwable {
		initTx();
		TxType type = meta.methodType();
		if (type == TxType.REQUIRED) {
			Status initTxStatus = txm.getStatus();

			if (initTxStatus == TransactionManager.Status.NOTX) {
				txm.begin(method.getName());
			}else{
				if (TransactionManagerAdaptor.isLogTx)
					RuleLoggerHelper.dolog("tx no need begin -"+method.getName());
			}
			try {
				Object ret = invokeWithLog(proxyObj, oldService, method, args, meta);

				if (initTxStatus == TransactionManager.Status.NOTX) {
					txm.commit(method.getName());
				}else{
					if (TransactionManagerAdaptor.isLogTx)
						RuleLoggerHelper.dolog("tx no need commit -"+method.getName());
				}

				return ret;
			} catch (Throwable th) {
				if (initTxStatus == TransactionManager.Status.NOTX) {
					txm.rollback(method.getName());
				}else{
					if (TransactionManagerAdaptor.isLogTx)
						RuleLoggerHelper.dolog("tx no need rollback -"+method.getName());
				}
				throw getRethrow(th);
			}
		} else {
			return invokeWithLog(proxyObj, oldService, method, args, meta);
		}
	}

	/**
	 * @param th
	 * @return 
	 * @throws Throwable 
	 */
	private static Throwable getRethrow(Throwable th) throws Throwable {
		if (th instanceof InvocationTargetException){
			return ((InvocationTargetException)th).getTargetException();
		}else{
			return th;
		}
	}

	/**
	 * 
	 */
	private static void initTx() {
	
				if (txm==null){
					txm = (TransactionManagerAdaptor) ServiceFactory.getService(TransactionManager.class);
				}
	}
}
