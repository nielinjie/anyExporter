package xyz.nietongxue.engineering.io.jenkins

import com.beust.klaxon.Klaxon
import com.cv4j.netdiscovery.core.Spider
import com.cv4j.netdiscovery.core.config.Constant.RESPONSE_JSON
import com.cv4j.netdiscovery.core.domain.Page
import com.cv4j.netdiscovery.core.domain.Request
import com.cv4j.netdiscovery.core.parser.selector.Json
import com.cv4j.netdiscovery.core.parser.selector.JsonPathSelector
import xyz.nietongxue.engineering.model.*
import java.io.StringReader
import java.util.*

object Context {
    private var buildings: List<Building> = listOf()
    private var buildingIssuesList: List<BuildingIssues> = listOf()
    //    private var
    fun appendBuilding(building: Building) {
        if (buildings.any {
                it.buildingId == building.buildingId
            }) {
            //FIXME 有重复的应该代替，而不是忽略
            return
        }
        this.buildings += (building)

    }

    fun appendBuildingIssues(buildingIssues: BuildingIssues) {
        if (buildingIssuesList.any {
                it.buildingId == buildingIssues.buildingId
            }) {
            //FIXME 有重复的应该代替，而不是忽略
            return
        }
        this.buildingIssuesList += buildingIssues
    }

    fun buildingEvents(): List<BuildingEvent> {
        return buildings.map(BuildingEvent.Companion::fromBuilding)
    }

    fun buildingIssueEvents(): List<BuildingIssuesEvent> {
        return buildingIssuesList.map { bi: BuildingIssues ->
            val building = buildings.find { building -> building.buildingId == bi.buildingId }
            return@map building?.let { return@let BuildingIssuesEvent.fromBuilding(building, bi) }
        }.filterNotNull()

    }

}

private fun jenkinsRequest(path: String): Request {
    return Request("$path/api/json?pretty=true").header(
        "Authorization",
        "Basic YWRtaW46anVtYWF1dG90ZXN0"
    )
}

fun Page.json() = this.getField(RESPONSE_JSON) as Json

class MainPage {
    fun run() {
        Spider.create().request(
            jenkinsRequest("http://10.101.0.205:8080/jenkins")
        ).parser { page ->
            val jobs = page.json().jsonPath("""$.jobs[*]["name","url"]""").all()
            jobs.map {
                Klaxon()
                    .parse<JobPage>(it)
            }.forEach { jobPage ->
                jobPage?.run()
            }
        }.run()
    }
}


class JobPage(val name: String, val url: String) {
    fun run() {
        Spider.create().request(
            jenkinsRequest(url)
        ).parser { page ->
            val buildings = page.json().jsonPath("""$.builds[*]["number","url"]""").all()
            buildings.map {
                val j = Klaxon()
                    .parseJsonObject(StringReader(it))
                return@map BuildingPage(name, j["number"] as Int, j["url"] as String)
            }.forEach { buildPage ->
                buildPage.run()
            }
        }.run()
    }

}

class BuildingPage(val jobName: String, val number: Int, val url: String) {
    fun run() {
        Spider.create().request(jenkinsRequest(url))
            .parser { page ->
                val result: String = page.json().jsonPath("\$.result").get()
                val time: Long = page.json().jsonPath("\$.timestamp").get().toLong()
                Context.appendBuilding(Building(BuildingId(jobName, number), Date(time), result))
            }.run()
    }
}

//class BuildingIssuePage(val jobName:String,val number:Int,val typeName:String ,val url:String){
//    private val buildIssuesKey = "buildIssuesKey"
//    private val spider: Spider by lazy {
//        val sp = Spider.create().request(jenkinsRequest(url))
//        sp.parser { page ->
//            val json: Json = page.getField(RESPONSE_JSON) as Json
//            val result: String = json.select(JsonPathSelector("\$.result")).toString()
//            val time: Long = json.select(JsonPathSelector("\$.timestamp")).toString().toLong()
//            page.putField(buildIssuesKey, BuildingIssues(BuildingId(jobName, number), typeName, ))
//        }
//        sp.pipeline {
//            Context.appendBuilding(it.get<Building>(buildIssuesKey))
//        }
//        return@lazy sp
//
//    }
//
//    fun run() {
//        spider.run()
//    }
//}