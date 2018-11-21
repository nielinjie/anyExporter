package xyz.nietongxue.engineering.model

import java.util.*

data class System(val name:String,val team:Team)
data class Team(val name:String)


data class BuildingUnit(val system:System,val buildings:Array<Building>)
data class Building(val buildingUnitName:String,val number:Int,val time:Date,val result:String)

