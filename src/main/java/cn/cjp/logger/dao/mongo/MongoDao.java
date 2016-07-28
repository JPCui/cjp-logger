package cn.cjp.logger.dao.mongo;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import cn.cjp.logger.model.BeanInspectorModel;
import cn.cjp.logger.model.Log;
import cn.cjp.logger.service.LogService;
import cn.cjp.utils.PropertiesUtil;

/**
 * 
 * @author Jinpeng Cui
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MongoDao implements InitializingBean, Closeable {

	private static Logger logger = Logger.getLogger(MongoDao.class);

	private String host;
	private int port;
	private String username;
	private String password;
	private String database;

	private int connectTimeout;
	private int heartbeatConnectRetryFrequency;
	private int heartbeatConnectTimeout;
	private int heartbeatSocketTimeout;

	private MongoClient client;

	private boolean closed = false;

	public MongoDao() throws IOException {
		init();
		client = createMongoClient();
	}

	public MongoDao(String host, int port, String username, String password, String database, int connectTimeout,
			int heartbeatConnectRetryFrequency, int heartbeatConnectTimeout, int heartbeatSocketTimeout)
			throws IOException {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;

		this.connectTimeout = connectTimeout;
		this.heartbeatConnectRetryFrequency = heartbeatConnectRetryFrequency;
		this.heartbeatConnectTimeout = heartbeatConnectTimeout;
		this.heartbeatSocketTimeout = heartbeatSocketTimeout;
		client = createMongoClient();
	}

	public static void main(String[] args) throws IOException {
		MongoDao mongoDao = new MongoDao();
		System.out.println(mongoDao.getDB().getCollectionNames());
		logger.error(mongoDao.getDB().getCollection(LogService.collection("info")).insert(new Log().toDBObject()));
		logger.error(mongoDao.getDB().getCollection(LogService.collection("info")).count());
		mongoDao.close();
	}

	private void init() throws IOException {
		try {
			PropertiesUtil props = new PropertiesUtil("mongo.properties");
			host = props.getValue("mongo.host");
			port = Integer.parseInt(props.getValue("mongo.port"));
			username = props.getValue("mongo.username");
			password = props.getValue("mongo.password");
			database = props.getValue("mongo.database");

			connectTimeout = props.getInt("mongo.connectTimeout", 20_000);
			heartbeatConnectRetryFrequency = props.getInt("mongo.heartbeatConnectRetryFrequency", 10);
			heartbeatConnectTimeout = props.getInt("mongo.heartbeatConnectTimeout", 20_000);
			heartbeatSocketTimeout = props.getInt("mongo.heartbeatSocketTimeout", 20_000);
		} catch (IOException e) {
			logger.error("配置文件访问失败");
			throw e;
		}
	}

	private MongoClient createMongoClient() throws UnknownHostException {
		ServerAddress addr = new ServerAddress(host, port);

		MongoCredential credential = MongoCredential.createMongoCRCredential(username, database,
				password.toCharArray());
		List<MongoCredential> credentials = new ArrayList<>();
		credentials.add(credential);

		MongoClientOptions options = new Builder().connectTimeout(connectTimeout)
				.heartbeatConnectRetryFrequency(heartbeatConnectRetryFrequency)
				.heartbeatConnectTimeout(heartbeatConnectTimeout).heartbeatSocketTimeout(heartbeatSocketTimeout)
				.build();
		MongoClient mongoClient = new MongoClient(addr, credentials, options);
		return mongoClient;
	}

	public DB getDB() {
		return client.getDB(database);
	}

	public DB getDB(String databaseName) {
		return client.getDB(databaseName);
	}

	@Override
	public void close() throws IOException {
		if (closed) {
			return;
		}
		client.close();
		closed = true;
		logger.info("MongoDao closed.");
	}

	public boolean isClosed() {
		return closed;
	}

	public MongoClient getClient() {
		return client;
	}

	public void setClient(MongoClient client) {
		this.client = client;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		MongoDao.logger = logger;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getHeartbeatConnectRetryFrequency() {
		return heartbeatConnectRetryFrequency;
	}

	public void setHeartbeatConnectRetryFrequency(int heartbeatConnectRetryFrequency) {
		this.heartbeatConnectRetryFrequency = heartbeatConnectRetryFrequency;
	}

	public int getHeartbeatConnectTimeout() {
		return heartbeatConnectTimeout;
	}

	public void setHeartbeatConnectTimeout(int heartbeatConnectTimeout) {
		this.heartbeatConnectTimeout = heartbeatConnectTimeout;
	}

	public int getHeartbeatSocketTimeout() {
		return heartbeatSocketTimeout;
	}

	public void setHeartbeatSocketTimeout(int heartbeatSocketTimeout) {
		this.heartbeatSocketTimeout = heartbeatSocketTimeout;
	}

	private void buildIndex() {
		String[] collectionNames = new String[] { "info", "warn", "error" };
		logger.info("build index.");
		for (int i = 0; i < collectionNames.length; i++) {
			String collectionName = collectionNames[i];
			logger.info(collectionName + " - init index");

			DBCollection dbc = this.getDB().getCollection(collectionName);
			DBObject key = new BasicDBObject();
			key.put(BeanInspectorModel.TIME, -1);
			dbc.createIndex(key);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		buildIndex();
	}

}
