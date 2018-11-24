package xyz.nietongxue.engineering.io.jenkins

import xyz.nietongxue.engineering.model.Building
import xyz.nietongxue.engineering.model.BuildingIssues
import xyz.nietongxue.engineering.model.JenkinsJobSystemMapping
import java.util.*

class Pusher {
    fun push() {
        //FIXME 处理重复push的问题
        //Elastic search

    }
}

data class BuildingEvent(
    val systemName: String,
    val team: String,
    val buildingUnitName: String,
    val buildingNumber: Int,
    val date: Date,
    val result: String
) {
    companion object {
        fun fromBuilding(building: Building): BuildingEvent {
            val system = JenkinsJobSystemMapping.buildUnitNameToSystem(building.buildingId.buildUnitName)
            return with(building) {
                BuildingEvent(
                    system.name,
                    system.team.name,
                    buildingId.buildUnitName,
                    buildingId.number,
                    time,
                    result
                )
            }
        }
    }
}

data class BuildingIssuesEvent(
    val systemName: String,
    val team: String,
    val buildingUnitName: String,
    val buildingNumber: Int,
    val date: Date,
    val type: String,
    val high: Int,
    val normal: Int
) {
    companion object {
        fun fromBuilding(building: Building, buildingIssues: BuildingIssues): BuildingIssuesEvent {
            val system = JenkinsJobSystemMapping.buildUnitNameToSystem(building.buildingId.buildUnitName)
            return with(building) {
                BuildingIssuesEvent(
                    system.name,
                    system.team.name,
                    buildingId.buildUnitName,
                    buildingId.number,
                    time,
                    buildingIssues.name,
                    buildingIssues.high,
                    buildingIssues.normal
                )
            }
        }

    }
}
