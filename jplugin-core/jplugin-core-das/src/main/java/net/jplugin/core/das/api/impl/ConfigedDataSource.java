package net.jplugin.core.das.api.impl;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import net.jplugin.common.kits.ReflactKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.das.api.DataSourceFactory;
import net.jplugin.core.das.api.dds.DDSManager;
import net.jplugin.core.das.dds.api.AbstractRouterDataSource;
import net.jplugin.core.das.dds.api.IRouterDataSource;
import net.jplugin.core.das.dds.api.RouterException;

public class ConfigedDataSource {
	/**
	 *  <bean id="dataSource" destroy-method="close" class="org.apache.commons.dbcp.BasicDataSource">
      <property name="driverClassName" value="${driverClassName}"/>
      <property name="url" value="${url}"/>
      <property name="username"><value>${dbuser}</value></property>
      <property name="password"><value>${dbpassword}</value></property>
      <!-- 最大激活连接数，表示同时最多有多少个数据库连接 -->
      <property name="maxActive"><value>${maxActive}</value></property> 
      <!-- 最大的空闲连接数，表示即使没有数据库连接时依然可以保持多少空闲的连接，而不被清除，随时处于待命状态 -->
      <property name="maxIdle"><value>${maxIdle}</value></property>
      <!-- 是最大等待秒钟数，取值-1，表示无限等待，直到超时为止，取值9000，表示9秒后超时 -->
      <property name="maxWait"><value>${maxWait}</value></property>
      <!-- 是否清理removeAbandonedTimeout秒没有使用的活动连接,清理后并没有放回连接池.（默认是false） -->
      <property name="removeAbandoned"><value>true</value></property>
      <!-- 设定连接在多少秒内被认为是放弃的连接，即可进行恢复利用 ,针对未被close的活动连接-->
      <property name="removeAbandonedTimeout"><value>600</value></property>
      <!-- 是否输出回收的日志，可以详细打印出异常从而发现是在那里发生了泄漏 -->
      <property name="logAbandoned"><value>false</value></property>
    </bean>
	 * @param groupName
	 * @return
	 */
	public static DataSource getDataSource(String group){
		
		Map<String, String> map = ConfigFactory.getStringConfigInGroup(group);
		String routeFlag = map.get("route-datasource-flag");
		if (routeFlag!=null) routeFlag.trim();
		
		if ("true".equalsIgnoreCase(routeFlag)){
			String routeDatasourceType = map.get("route-datasource-type");
			String routeDatasourceClass;
			if (StringKit.isNull(routeDatasourceType)) {
				routeDatasourceType = "db-table-split";
			}
			
			Class clazz = DDSManager.me.getDataSourceClassByType(routeDatasourceType);
			routeDatasourceClass = clazz.getName();
			
			AbstractRouterDataSource ds = makeRouteDataSource(routeDatasourceClass,group,map);
			return ds;
		}else{
			DataSource ds = createJdbcDataSource(group, map);
			return ds;
		}
	}

	private static AbstractRouterDataSource makeRouteDataSource(String routeDatasourceClass, String dsname,Map<String, String> map) {
		AbstractRouterDataSource o;
		try {
			o = (AbstractRouterDataSource) Class.forName(routeDatasourceClass).newInstance();
		} catch (Exception e) {
			throw new RouterException("create instance error:"+routeDatasourceClass,e);
		}
		o.init(dsname, map);
		return o;
	}

	private static DataSource createJdbcDataSource(String group, Map<String, String> map) {
		org.apache.commons.dbcp2.BasicDataSource ds = new BasicDataSource();
		//为了兼容以前的配置文件，支持dbuser、dbpassword两个参数
		if (map.containsKey("dbuser")){
			map.put("username", map.get("dbuser"));
			map.remove("dbuser");
		}
		if (map.containsKey("dbpassword")){
			map.put("password", map.get("dbpassword"));
			map.remove("dbpassword");
		}
		//以上为了更好的迁移以前的配置文件，只修改文件名即可
		
		compibleWithDBCP1(map);
		
		map.remove(DataSourceFactory.IS_TX_MANAGED);
		
		if (map.isEmpty()){
			throw new RuntimeException("Can't find config for database:"+group);
		}
		for (String k:map.keySet()){
			ReflactKit.setPropertyFromString(ds, k,map.get(k));
		}
		return ds;
	}

	private static void compibleWithDBCP1(Map<String, String> map) {
		if (map.containsKey("maxWait")){
			String o = map.get("maxWait");
			map.remove("maxWait");
			map.put("maxWaitMillis", o);
		}
		if (map.containsKey("maxActive")){
			String o = map.get("maxActive");
			map.remove("maxActive");
			map.put("maxTotal", o);
		}
		
	}
}
