package xyz.nietongxue.anyExporter

import xyz.nietongxue.engineering.io.Context
import xyz.nietongxue.engineering.io.MainPage
import java.util.*
import kotlin.concurrent.schedule


fun main(args: Array<String>) {
    //TODO 定时拉取
    Timer().schedule(1000, 10000) {
        MainPage().run()
    }
    //TODO push to pushGateway here
    Timer().schedule(10000, 30000) {
        Context.getBuildingUnits().forEach { buildingUnit ->
            println(buildingUnit.name)
            buildingUnit.buildings.forEach {
                println("         " + it.number)
            }
        }
    }


}

