package net.jplugin.core.service;

import java.util.Map;

import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.AutoBindExtensionManager;
import net.jplugin.core.kernel.api.CoreServicePriority;
import net.jplugin.core.kernel.api.ExtensionKernelHelper;
import net.jplugin.core.kernel.api.ExtensionPoint;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.service.api.Constants;
import net.jplugin.core.service.api.ServiceFactory;
import net.jplugin.core.service.impl.ServiceAttrAnnoHandler;

/**
 *
 * @author: LiuHang
 * @version 创建时间：2015-2-7 下午09:59:02
 **/

public class Plugin extends AbstractPlugin{

	static{
		AutoBindExtensionManager.INSTANCE.addBindExtensionHandler((p)->{
			ExtensionServiceHelper.autoBindServiceExtension(p, "");
		});
	}
	
	public Plugin(){
		this.addExtensionPoint(ExtensionPoint.create(Constants.EP_SERVICE,Object.class,true));
		ExtensionKernelHelper.addAnnoAttrHandlerExtension(this, ServiceAttrAnnoHandler.class);
	}
	/* (non-Javadoc)
	 * @see net.luis.common.kernel.AbstractPlugin#getPrivority()
	 */
	@Override
	public int getPrivority() {
		return CoreServicePriority.SERVICE;
	}

	/* (non-Javadoc)
	 * @see net.luis.common.kernel.api.IPlugin#init()
	 */
	public void onCreateServices() {
		ExtensionPoint servicesPoint = PluginEnvirement.getInstance().getExtensionPoint(Constants.EP_SERVICE);
		
		Map<String, Object> map  = servicesPoint.getExtensionMap();
		ServiceFactory.init(map);
	}
	public void init() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean searchClazzForExtension() {
		// TODO Auto-generated method stub
		return false;
	}
	
//	@Override
//	public void init() {
//		ServiceFactory.initAnnotation();
//	}
	
}
