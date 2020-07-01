package net.jplugin.core.kernel.kits;

import net.jplugin.common.kits.filter.FilterManager;
import net.jplugin.common.kits.filter.IFilter;
import net.jplugin.core.kernel.api.PluginEnvirement;

public class ExecutorKitFilterManager {
	static FilterManager<RunnableWrapper> httpClientFilterManager = new FilterManager<>();

	public static void init() {
		IFilter<RunnableWrapper>[] filters = PluginEnvirement.getInstance()
				.getExtensionObjects(net.jplugin.core.kernel.Plugin.EP_EXECUTOR_FILTER, IFilter.class);
		for (IFilter<RunnableWrapper> f : filters) {
			httpClientFilterManager.addFilter(f);
		}
		// 最后增加一个执行的
		httpClientFilterManager.addFilter((fc, ctx) -> {
			ctx.inner.run();
			return null;
		});
	}

	public static void filter(RunnableWrapper r) {
		httpClientFilterManager.filter(r);
	}
}
