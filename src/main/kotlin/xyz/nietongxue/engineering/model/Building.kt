package xyz.nietongxue.engineering.model

import java.util.*

object jobSystemMapping {
    val csvString: List<String> by lazy {
        jobSystemMapping::class.java.getResource("/jobNameToSystemName.csv").readText().lines().drop(1)
    }
    val mapping: Map<String, System> by lazy {
        this.csvString.map {
            val (un, sys, team) = it.split(',')
            un to System(sys, Team(team))
        }.toMap()
    }

    fun jobToSystem(jbname: String): System {
        return mapping.get(jbname) ?: System("UNKNOWN", Team("UNKNOWN"))
    }
}

data class System(val name: String, val team: Team)
data class Team(val name: String)


data class BuildingUnit(val system: System, val buildings: Array<Building>)
data class Building(val buildingUnitName: String, val number: Int, val time: Date, val result: String)

