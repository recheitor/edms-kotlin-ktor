package example.com.plugins

import com.mongodb.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.github.cdimascio.dotenv.dotenv


fun Application.configureDatabases(): MongoDatabase {
    val dotenv = dotenv {
        directory = "./"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }

    val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "edms-ktor"
    val uri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017"

    val mongoClient = MongoClients.create(uri)
    val database = mongoClient.getDatabase(databaseName)

    environment.monitor.subscribe(ApplicationStopped) { mongoClient.close() }

    return database
}