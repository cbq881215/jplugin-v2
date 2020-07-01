package test.net.jplugin.core.ctx.bindrulemethodfilter;

import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.ctx.api.AbstractRuleMethodInterceptor;
import net.jplugin.core.ctx.api.BindRuleMethodInterceptor;
import net.jplugin.core.ctx.api.RuleServiceFilterContext;

//@BindRuleMethodInterceptor(applyTo="test.net.jplugin.core.ctx.bindrulemethodfilter.RuleService123:set*",sequence=2)

@BindRuleMethodInterceptor(applyTo="${platform.applyTo}",sequence=2)

public class RuleMethodFilter2 extends AbstractRuleMethodInterceptor {
	public static int cnt = 0;
	@Override
	public Object filterRuleMethod(FilterChain fc, RuleServiceFilterContext ctx) throws Throwable {
		System.out.println("!!!!!!!!!!!!RuleMethodFilter2" +ctx.getMethod().getName());
		cnt++;
		return fc.next(ctx);
	}

}
