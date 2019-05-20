package com.networkcontacts.servicelayer.dao;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.networkcontacts.servicelayer.core.utils.Utils;
import com.networkcontacts.utils.DB.MONGO.MongoUtility;

public class MongoConnector {
	
	private MongoUtility mongo;
	
	public MongoConnector(String hostname, int hostport, String database) {
		mongo = new MongoUtility(hostname, hostport, database, Utils.getJsonUtility());
	}
	
	public List<String> getCollectionList(String collectionTAG) {
		return mongo.getCollectionList(collectionTAG);
	}
	
	public void destroyMongoConnection() {
		mongo.closeConnection();
	}
	
	public void createIndex(String collection, String[] fields) {
		try {
			mongo.createIndex(collection, fields);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public boolean collectionExists(String collectionName) {
	     return mongo.collectionExists(collectionName);
	}
	
	public void dropCollections(String... collectionsName) {
		mongo.dropCollections(collectionsName);
	}
	
	public Document findDocument(String collection, String column, String value) {
		return mongo.findDocument(collection, column, value);
	}
	
	public FindIterable<Document> findDocuments(String collection, String column, String value) {
		return mongo.findDocuments(collection, column, value);
	}
	
	public void insertDocument(String collection, Document document) {
		mongo.insertDocument(collection, document);
	}
	
	public long deleteDocumentByID(String collection,String id) {
		return mongo.deleteDocumentByID(collection, id);
	}
	
	public void deleteDocuments(String collection, String column, String value) {
		mongo.deleteDocuments(collection, column, value);
	}
	
	public void updateDocument(String collection,Map<String,Object> filterFields, Map<String, Object> updateFields, String operation) {
		mongo.updateDocument(collection, filterFields, updateFields, operation);
	}
	
	public void updateDocumentByID(String collection,String id, Document updateFields) {
		mongo.updateDocumentByID(collection, id, updateFields);
	}
	
	public int countDocument(FindIterable<Document> documents) {
		return mongo.countFindIterableDocument(documents);
	}
	
	public FindIterable<Document> findDocumentByID(String collection, String id) {
		return mongo.findDocumentByID(collection, id);
	}
	
	public void renameCollections(String oldTag, String newTag, String... collectionsName) {
		mongo.renameCollections(oldTag, newTag, collectionsName);
	}
	
	public FindIterable<Document> findDocuments(String nameCollection) {
		return mongo.findDocuments(nameCollection);
	}
	
}
