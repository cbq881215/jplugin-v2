package net.jplugin.core.das.monitor;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionWrapper implements Connection{
	Connection inner;
	String dsName;

	public ConnectionWrapper(Connection c,String ds){
		this.inner = c;
		this.dsName = ds;
	}
	public String getDataSourceName() {
		return dsName;
	}
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return inner.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return inner.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		return (Statement) SqlMonitor.createStatement(this,()->inner.createStatement());
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return  (PreparedStatement) SqlMonitor.prepareStatement(this,sql,()->inner.prepareStatement(sql));
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return inner.prepareCall(sql);
	}

	public String nativeSQL(String sql) throws SQLException {
		return inner.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		inner.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		return inner.getAutoCommit();
	}

	public void commit() throws SQLException {
		inner.commit();
	}

	public void rollback() throws SQLException {
		inner.rollback();
	}

	public void close() throws SQLException {
		inner.close();
	}

	public boolean isClosed() throws SQLException {
		return inner.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return inner.getMetaData();
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		inner.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		return inner.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		inner.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return inner.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		inner.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return inner.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return inner.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		inner.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return (Statement) SqlMonitor.createStatement(this,()->inner.createStatement(resultSetType, resultSetConcurrency));
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return (PreparedStatement) SqlMonitor.prepareStatement(this,sql,()->inner.prepareStatement(sql, resultSetType, resultSetConcurrency));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return inner.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return inner.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		inner.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		inner.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		return inner.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		return inner.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return inner.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		inner.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		inner.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return (Statement) SqlMonitor.createStatement(this,()->inner.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return (PreparedStatement) SqlMonitor.prepareStatement(this,sql,()->inner.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return inner.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return (PreparedStatement) SqlMonitor.prepareStatement(this,sql,()->inner.prepareStatement(sql, autoGeneratedKeys));
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return (PreparedStatement) SqlMonitor.prepareStatement(this,sql,()->inner.prepareStatement(sql, columnIndexes));
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return (PreparedStatement) SqlMonitor.prepareStatement(this,sql,()->inner.prepareStatement(sql, columnNames));
	}

	public Clob createClob() throws SQLException {
		return inner.createClob();
	}

	public Blob createBlob() throws SQLException {
		return inner.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return inner.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return inner.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return inner.isValid(timeout);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		inner.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		inner.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return inner.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return inner.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return inner.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return inner.createStruct(typeName, attributes);
	}

	public void setSchema(String schema) throws SQLException {
		inner.setSchema(schema);
	}

	public String getSchema() throws SQLException {
		return inner.getSchema();
	}

	public void abort(Executor executor) throws SQLException {
		inner.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		inner.setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException {
		return inner.getNetworkTimeout();
	}
	
}
