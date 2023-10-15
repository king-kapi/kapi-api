package org.kapi.mongo

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*

class MongoClientSingleton {
    companion object {
        @Volatile
        private var mongoClient: MongoClient? = null

        fun getClient(environment: ApplicationEnvironment): MongoClient {
            if (mongoClient != null) {
                return mongoClient as MongoClient
            }

            mongoClient =
                MongoClient.create(environment.config.property("ktor.deployment.mongo_connection_uri").getString())

            return mongoClient as MongoClient
        }

        fun getKapiDatabase(environment: ApplicationEnvironment): MongoDatabase {
            return getClient(environment).getDatabase("kapi")
        }
    }
}