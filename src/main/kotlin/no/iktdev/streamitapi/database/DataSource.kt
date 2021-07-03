package no.iktdev.streamitapi.database

import org.jetbrains.exposed.sql.Database

class DataSource
{
    var address: String = System.getenv("DATABASE_ADDRESS") ?: "192.168.2.252"
    var port: String = System.getenv("DATABASE_PORT") ?: "8082"  //"3306"
    var username: String = System.getenv("DATABASE_USERNAME") ?: "streamit"
    var password: String = System.getenv("DATABASE_PASSWORD") ?: "shFZ27eL2x2NoxyEDBMfDWkvFO"

    fun getConnection(): Database {
        if (!port.contains(":"))
        {
            port = ":$port"
        }
        val database = Database.connect(
            "jdbc:mysql://$address$port/streamit",
            driver = "com.mysql.jdbc.Driver",
            user = username,
            password = password
        );
        return database
    }

}