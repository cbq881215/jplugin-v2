package net.jplugin.core.das.dds.impl;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
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

import net.jplugin.core.das.api.DataSourceFactory;
import net.jplugin.core.das.dds.api.AbstractRouterDataSource;
import net.jplugin.core.das.dds.api.RouterExecutionContext;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;

public class DummyConnection implements java.sql.Connection {

	AbstractRouterDataSource dataSrouce;

	public static DummyConnection create(AbstractRouterDataSource ds) {
		return new DummyConnection(ds);
	}

	private DummyConnection(AbstractRouterDataSource ds) {
		this.dataSrouce = ds;
	}

	public AbstractRouterDataSource getDataSource() {
		return this.dataSrouce;
	}

	/**
	 * 对于Statement来说，需要在BeforeExecute才才能定夺。
	 */
	@Override
	public Statement createStatement() throws SQLException {
		return DummyStatement.create(this);
	}

	/**
	 * 对于PreparedStatement来说,SqlKnown时机和 BeforeExecute时机是不一样的。
	 */
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return (PreparedStatement) RouterExecutionContext.call(() -> {
			PreparedStatement preparedStatement = this.dataSrouce.getTargetPreparedStmtBefCreate(this.getDataSource(),
					sql);
			if (preparedStatement != null) {
				Util.trySetConnection(preparedStatement, this);
				return preparedStatement;
//				return new DummyPreparedStatementWrapper(this,preparedStatement);
			} else {
				return DummyPreparedStatement.create(sql, this);
			}
		});
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return (PreparedStatement) RouterExecutionContext.call(() -> {
			PreparedStatement preparedStatement = this.dataSrouce.getTargetPreparedStmtBefCreate(this.getDataSource(),
					sql ,autoGeneratedKeys);
			if (preparedStatement != null) {
				Util.trySetConnection(preparedStatement, this);
				return preparedStatement;
//				return new DummyPreparedStatementWrapper(this,preparedStatement);
			} else {
				return DummyPreparedStatement.create(sql, this);
			}
		});
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return false;
	}

	@Override
	public void commit() throws SQLException {
		
	}

	@Override
	public void rollback() throws SQLException {
	}

	@Override
	public void close() throws SQLException {
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new RuntimeException("not support");
	}



	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (DummyConnection.class.equals(iface))
			return (T) this;
		else
			throw new SQLException("can't unwarp for :" + iface.getName());
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		if (iface.equals(this.getClass()))
			return true;
		else
			return false;
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		throw new RuntimeException("not support");
	}

	static DatabaseMetaData md;
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return DummyDatabaseMetaData.INSTANCE;
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public String getCatalog() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public int getHoldability() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Clob createClob() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Blob createBlob() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public NClob createNClob() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		throw new RuntimeException("not support");
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public String getSchema() throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		throw new RuntimeException("not support");
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		throw new RuntimeException("not support");
	}

}
