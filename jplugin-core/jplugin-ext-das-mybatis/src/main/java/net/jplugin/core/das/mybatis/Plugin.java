package net.jplugin.core.das.mybatis;

import net.jplugin.core.ctx.ExtensionCtxHelper;
import net.jplugin.core.das.mybatis.api.ExtensionDefinition4Incept;
import net.jplugin.core.das.mybatis.api.ExtensionDefinition4Mapping;
import net.jplugin.core.das.mybatis.api.ExtensionMybatisDasHelper;
import net.jplugin.core.das.mybatis.api.MyBatisServiceFactory;
import net.jplugin.core.das.mybatis.impl.DefaultMybaticsService4JianRong;
import net.jplugin.core.das.mybatis.impl.IMybatisService;
import net.jplugin.core.das.mybatis.impl.MybatisMapperAnnoHandler;
import net.jplugin.core.das.mybatis.impl.MybatisServiceAnnoHandler;
import net.jplugin.core.das.mybatis.impl.sess.MybatisTransactionManagerListener;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.AutoBindExtensionManager;
import net.jplugin.core.kernel.api.CoreServicePriority;
import net.jplugin.core.kernel.api.ExtensionKernelHelper;
import net.jplugin.core.kernel.api.ExtensionPoint;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.service.ExtensionServiceHelper;

public class Plugin extends AbstractPlugin{
	
	public static final String EP_MYBATIS_MAPPER = "EP_MYBATIS_MAPPER";
	public static final String EP_MYBATIS_INCEPT = "EP_MYBATIS_INCEPT";
	
	public static boolean enabled;

	static{
		AutoBindExtensionManager.INSTANCE.addBindExtensionHandler((p)->{
			ExtensionMybatisDasHelper.autoBindMapperExtension(p, "");
		});
	}
	
	public Plugin(){
		if (noMybatis()){
			PluginEnvirement.INSTANCE.getStartLogger().log("Mybatis env not found,skipped!");
			enabled = false;
			return;
		}
		enabled = true;
		this.addExtensionPoint(ExtensionPoint.create(EP_MYBATIS_MAPPER, ExtensionDefinition4Mapping.class));
		this.addExtensionPoint(ExtensionPoint.create(EP_MYBATIS_INCEPT,ExtensionDefinition4Incept.class));
		ExtensionServiceHelper.addServiceExtension(this, IMybatisService.class.getName(), DefaultMybaticsService4JianRong.class);
		ExtensionCtxHelper.addTxMgrListenerExtension(this, MybatisTransactionManagerListener.class);
		ExtensionKernelHelper.addAnnoAttrHandlerExtension(this,MybatisServiceAnnoHandler.class );
		ExtensionKernelHelper.addAnnoAttrHandlerExtension(this,MybatisMapperAnnoHandler.class );
	}
	
	private boolean noMybatis() {
		try {
			Class.forName("org.apache.ibatis.session.SqlSession");
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	public void onCreateServices() {
		if (noMybatis()){
			PluginEnvirement.INSTANCE.getStartLogger().log("Mybatis env not found,not init!");
			return;
		}else{
			PluginEnvirement.INSTANCE.getStartLogger().log("now to init mybatis");
		}
		
		MyBatisServiceFactory.init();
		
	}

	@Override
	public int getPrivority() {
		return CoreServicePriority.DAS_IBATIS;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean searchClazzForExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
