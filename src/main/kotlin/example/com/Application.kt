package example.com

import example.com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()

    val mongoDatabase = configureDatabases()
    val employeeService = EmployeeService(mongoDatabase)
    val categoryService = RequestCategoryService(mongoDatabase)
    val timeOffService = TimeOffRequestService(mongoDatabase)

    install(Routing) {
        route("/api") {
            employeesRoutes(employeeService)
            requestCategoriesRoutes(categoryService)
            timeOffRequestsRoutes(timeOffService)
        }
    }
}