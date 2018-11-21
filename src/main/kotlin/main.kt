package xyz.nietongxue.anyExporter

import xyz.nietongxue.engineering.io.Context
import xyz.nietongxue.engineering.io.MainPage


fun main(args: Array<String>) {

        MainPage().run()
        Thread.sleep(20*1000)
        Context.getBuildingUnits().forEach{
            println(it)
        }

    }

