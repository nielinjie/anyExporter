package xyz.nietongxue.engineering.model

import java.util.*

object JenkinsJobSystemMapping {
    private val csvString: List<String> by lazy {
        JenkinsJobSystemMapping::class.java.getResource("/jobNameToSystemName.csv").readText().lines().drop(1)
    }
    private val mapping: Map<String, System> by lazy {
        this.csvString.map {
            val (un, sys, team) = it.split(',')
            un to System(sys, Team(team))
        }.toMap()
    }

    fun buildUnitNameToSystem(buildUnitName: String): System {
        return mapping[buildUnitName] ?: System("UNKNOWN", Team("UNKNOWN"))
    }
}

data class System(val name: String, val team: Team)
data class Team(val name: String)


data class BuildingUnit(val system: System,val name: String, val buildings: Array<Building>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuildingUnit

        if (system != other.system) return false
        if (name != other.name) return false
        if (!buildings.contentEquals(other.buildings)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = system.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + buildings.contentHashCode()
        return result
    }

}

data class Building(val buildingUnitName: String, val number: Int, val time: Date, val result: String){
    fun duplicated(b:Building) :Boolean{
        return b.buildingUnitName == buildingUnitName && b.number == number
    }
}

