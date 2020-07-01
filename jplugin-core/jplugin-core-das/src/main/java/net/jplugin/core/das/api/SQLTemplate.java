package net.jplugin.core.das.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.ObjectRef;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.common.kits.tuple.Tuple3;

/**
 * 
 * @author: LiuHang
 * @version 创建时间：2015-2-24 上午09:02:50
 **/

public class SQLTemplate {

	public static boolean printSQL=true;
	/**
	 * @param conn
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int executeUpdateSql(Connection conn, String sql,
			Object[] param) {
		if (printSQL){
			print(sql,param);
		}
		
		return executeAndReturnCount(conn,sql,param,"UPDATE");
	}
	
	private static void print(String sql, Object[] param) {
		System.out.print("SQL:"+sql+" params="+JsonKit.object2Json(param));
		System.out.println();
	}


	/**
	 * @param connection
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int executeDeleteSql(Connection conn, String sql,
			Object[] param) {
		if (printSQL){
			print(sql,param);
		}
		return executeAndReturnCount(conn,sql,param,"DELETE");
	}

	
	static int executeAndReturnCount(Connection conn, String sql,
			Object[] param,String prefixIgnorecase){
		String leftTenChar = sql.substring(0, 10);
		if (!leftTenChar.trim().toUpperCase().startsWith(prefixIgnorecase)) {
			throw new DataException("Not a valid sql with "+prefixIgnorecase);
		}
		Statement stmt = null;
		try {
			if (param == null || param.length == 0) {
				stmt = conn.createStatement();
				return stmt.executeUpdate(sql);
			} else {
				stmt = conn.prepareStatement(sql);
				for (int i = 0; i < param.length; i++) {
					((PreparedStatement) stmt).setObject(i + 1, param[i]);
				}
				int ret = ((PreparedStatement) stmt).executeUpdate();
				return ret;
			}
		} catch (Exception e) {
			throw new DataException(e.getMessage()+"SQL执行失败。 SQL="+sql, e);
		}finally{
			closeStmtQuiretly(stmt);
		}
	}

	/**
	 * @param stmt
	 */
	private static void closeStmtQuiretly(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param connection
	 * @param sql
	 * @return
	 */
	public static void executeDropSql(Connection connection, String sql) {
		if (printSQL){
			print(sql,null);
		}
		executeAndReturnCount(connection,sql,null,"DROP");
	}
	/**
	 * @param connection
	 * @param sql
	 * @return
	 */
	public static void executeCreateSql(Connection connection, String sql) {
		if (printSQL){
			print(sql,null);
		}
		executeAndReturnCount(connection,sql,null,"CREATE");
	}
	
	public static List<Long> executeInsertReturnGenKey(Connection conn,String sql,Object[] param) {
		if (!sql.trim().toUpperCase().startsWith("INSERT")) {
			throw new DataException("Not a insert sql");
		}
		Statement stmt = null;
		try {
			if (param == null || param.length == 0) {
				stmt = conn.createStatement();
				stmt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
				return getGenKey(stmt);
			} else {
				stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < param.length; i++) {
					((PreparedStatement) stmt).setObject(i + 1, param[i]);
				}
				int ret = ((PreparedStatement) stmt).executeUpdate();
				return getGenKey(stmt);
			}
		} catch (Exception e) {
			throw new DataException(e.getMessage()+"SQL执行失败。 SQL="+sql, e);
		}finally{
			closeStmtQuiretly(stmt);
		}
	}
	

	private static List<Long> getGenKey(Statement stmt) throws SQLException {
		ResultSet genkeys = stmt.getGeneratedKeys();
		List<Long> ret = new ArrayList<Long>();
		while(genkeys.next()) {
			long key = genkeys.getLong(1);
			ret.add(key);
		}
		return ret;
	}

	/**
	 * @param connection
	 * @param sql
	 * @param param
	 * @return
	 */
	public static int executeInsertSql(Connection connection, String sql,
			Object[] param) {
		if (printSQL){
			print(sql,param);
		}
		return executeAndReturnCount(connection,sql,param,"INSERT");
	}

	public static List<Map<String,String>> executeSelect(Connection conn,String sql,Object[] p){
		return executeSelectWithMeta(conn,sql,p,false).first;
	}
	
	public static Tuple3<List<Map<String,String>>,List<String>,List<Integer>> executeSelectWithMeta(Connection conn,String sql,Object[] p,boolean needMeta){
		List<Map<String,String>> ret = new ArrayList<>();

		final ObjectRef<List<String>> columnsRef = new ObjectRef<>();
		final ObjectRef<List<Integer>> typesRef =  new ObjectRef<>();
		
		executeSelect(conn, sql,new IResultDisposer() {
			List<String> columns=null; //总是获取，不一定返回
			List<Integer> types=null;//不一定获取
			@Override
			public void readRow(ResultSet rs) throws SQLException {
//				rs.getMetaData().getColumnCount();
				if (columns==null){
					initcolumns(rs);
				}
				Map<String,String>  map = new HashMap<>();
				for (String s:columns){
					map.put(s, rs.getString(s));
				}
				ret.add(map);
			}
			private void initcolumns(ResultSet rs) throws SQLException {
				ResultSetMetaData m = rs.getMetaData();
				int cnt = m.getColumnCount();
				columns = new ArrayList<String>();
				for (int i=1;i<=cnt;i++){
					columns.add(m.getColumnLabel(i));
				}
				columnsRef.set(columns);
				
				if (needMeta){
					types = new ArrayList<Integer>();
					for (int i=1;i<=cnt;i++){
						types.add(m.getColumnType(i));
					}
					typesRef.set(types);
				}
			}
		},p);
		if (needMeta)
			return Tuple3.with(ret, columnsRef.get(), typesRef.get());
		else
			return Tuple3.with(ret, null, null);
	}
	/**
	 * @param sql
	 * @param rd
	 * @param param
	 * @return
	 */
	public static void executeSelect(Connection conn,String sql, IResultDisposer rd,
			Object[] param) {
		if (printSQL){
			print(sql,param);
		}
		
		String leftTenChar = sql.substring(0, 10);
		if (!leftTenChar.trim().toUpperCase().startsWith("SELECT")) {
			throw new DataException("Not a valid sql with SELECT");
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (param == null || param.length == 0) {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				fetchRs(rs,rd);
			} else {
				stmt = conn.prepareStatement(sql);
				for (int i = 0; i < param.length; i++) {
					((PreparedStatement) stmt).setObject(i + 1, param[i]);
				}
				rs = ((PreparedStatement) stmt).executeQuery();
				fetchRs(rs,rd);
			}
		} catch (Exception e) {
			throw new DataException(e.getMessage()+" SQL="+sql, e);
		}finally{
			closeResultSetQuiertly(rs);
			closeStmtQuiretly(stmt);
		}
	}

	/**
	 * @param rs
	 */
	private static void closeResultSetQuiertly(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param rs
	 * @param rd
	 * @throws SQLException 
	 */
	private static void fetchRs(ResultSet rs, IResultDisposer rd) throws SQLException {
		while(rs.next()){
			rd.readRow(rs);
		}
	}


}
