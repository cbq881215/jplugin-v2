package test.net.jplugin.core.ctx;

import net.jplugin.common.kits.AssertKit;
import net.jplugin.core.ctx.ExtensionCtxHelper;
import net.jplugin.core.ctx.api.RuleServiceFactory;
import net.jplugin.core.kernel.api.AbstractPluginForTest;
import net.jplugin.core.kernel.api.CoreServicePriority;
import net.jplugin.core.kernel.api.ExtensionKernelHelper;
import net.jplugin.core.kernel.api.ExtensionPoint;
import net.jplugin.core.kernel.api.PluginAnnotation;
import test.net.jplugin.core.ctx.bind.BindTest;
import test.net.jplugin.core.ctx.bindextension.IService1;
import test.net.jplugin.core.ctx.bindextension.TestBindExtension;
import test.net.jplugin.core.ctx.bindrulemethodfilter.TestFroRuleMethodFilter;

public class Plugin extends AbstractPluginForTest {
	
	public Plugin(){
		ExtensionCtxHelper.addRuleExtension(this,IRuleTest.class, RuleTestImpl.class);
		
		ExtensionCtxHelper.addTxMgrListenerExtension(this, TxManagerListenerTest.class);
		
		ExtensionCtxHelper.autoBindRuleServiceExtension(this, "");
		
		ExtensionCtxHelper.autoBindRuleMethodInterceptor(this, "");
		
		ExtensionKernelHelper.autoBindExtension(this, "");
		
		this.addExtensionPoint(ExtensionPoint.create("testExtensionPoint", IService1.class));
	}

	@Override
	public int getPrivority() {
		return CoreServicePriority.CTX+1;
	}
	
	@Override
	public void test() throws Throwable {
		AssertKit.assertNotNull(RuleServiceFactory.getRuleService(IRuleTest.class),"rulesvc");
		RuleServiceFactory.getRuleService(IRuleTest.class).testNoMeta();
		RuleServiceFactory.getRuleService(IRuleTest.class).testNoMeta("a");
		RuleServiceFactory.getRuleService(IRuleTest.class).testNoMeta2();
		
		try {
			RuleServiceFactory.getRuleService(IRuleTest.class).testNoMetaWithException();
			throw new RuntimeException("can't come here");
		} catch (Exception e) {
			AssertKit.assertEqual(e.getMessage(), "HAHAHA");
		}
		
		new BindTest().test();
		
		new TestFroRuleMethodFilter().test();
		
		new TestBindExtension().test();
	}

	@Override
	public boolean searchClazzForExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
