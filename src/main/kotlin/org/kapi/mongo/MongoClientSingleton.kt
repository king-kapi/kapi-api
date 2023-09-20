package org.kapi.mongo

import com.mongodb.kotlin.client.coroutine.MongoClient

class MongoClientSingleton {
    companion object {
        @Volatile
        private var mongoClient: MongoClient? = null

        fun getClient(): MongoClient {
            if (mongoClient != null) {
                return mongoClient as MongoClient
            }

            mongoClient = MongoClient.create("mongodb://localhost:27017/kapi")

            return mongoClient as MongoClient
        }
    }
}