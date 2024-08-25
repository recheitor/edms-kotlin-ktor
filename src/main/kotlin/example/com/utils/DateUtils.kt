
package example.com.utils

import example.com.plugins.TimeOffRequest

fun addHoursToDates(request: TimeOffRequest, hours: Int): TimeOffRequest {
    val adjustedStartDate = request.start_date + hours * 60 * 60 * 1000
    val adjustedEndDate = request.end_date + hours * 60 * 60 * 1000

    return request.copy(
        start_date = adjustedStartDate,
        end_date = adjustedEndDate
    )
}