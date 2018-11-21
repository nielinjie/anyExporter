package xyz.nietongxue.engineering.io

import com.beust.klaxon.Klaxon
import com.cv4j.netdiscovery.core.Spider
import com.cv4j.netdiscovery.core.config.Constant.RESPONSE_JSON
import com.cv4j.netdiscovery.core.domain.Request
import com.cv4j.netdiscovery.core.parser.selector.Json
import com.cv4j.netdiscovery.core.parser.selector.JsonPathSelector
import xyz.nietongxue.engineering.model.Building
import xyz.nietongxue.engineering.model.BuildingUnit
import xyz.nietongxue.engineering.model.JenkinsJobSystemMapping
import java.io.StringReader
import java.util.*

object Context {
    private var buildings: List<Building> = listOf()
    fun append(building: Building) {
        if (buildings.any {
                it.duplicated(building)
            }) {
            //FIXME 有重复的应该代替，而不是忽略
            return
        }
        this.buildings += (building)

    }

    fun getBuildingUnits(): List<BuildingUnit> {
        val byJobName = buildings.groupBy { it.buildingUnitName }
        return byJobName.map { (bn, building) ->
            BuildingUnit(JenkinsJobSystemMapping.buildUnitNameToSystem(bn), bn, building.toTypedArray())
        }
    }
}

class MainPage {
    private val buildUnits = "buildUnits"
    private val spider: Spider by lazy {
        val sp = Spider.create()
        sp.request(
            jenkinsRequest("http://10.101.0.205:8080/jenkins")
        )
        sp.parser { page ->
            val json: Json = page.getField(RESPONSE_JSON) as Json
            val jobs = json.selectList(JsonPathSelector("\$.jobs[*][\"name\",\"url\"]")).all()
            page.putField(buildUnits, jobs.map {
                Klaxon()
                    .parse<JobPage>(it)
            })
        }
        sp.pipeline {
            (it.get(buildUnits) as List<JobPage>).forEach { jobPage ->
                jobPage.run()
            }
        }
        return@lazy sp
    }

    fun run() {
        spider.run()
    }
}

private fun jenkinsRequest(path: String): Request {
    return Request("$path/api/json?pretty=true").header(
        "Authorization",
        "Basic YWRtaW46anVtYWF1dG90ZXN0"
    )
}

class JobPage(val name: String, val url: String) {
    private val buildingsKey = "buildingsKey"
    private val spider: Spider by lazy {
        val sp = Spider.create().request(
            jenkinsRequest(url).putExtra("jobName", name)
        )
        sp.parser { page ->
            val json: Json = page.getField(RESPONSE_JSON) as Json
            val buildings = json.selectList(JsonPathSelector("\$.builds[*][\"number\",\"url\"]")).all()
            page.putField(buildingsKey, buildings.map {
                val j = Klaxon()
                    .parseJsonObject(StringReader(it))
                return@map BuildingPage(name, j["number"] as Int, j["url"] as String)
            })
        }
        sp.pipeline {
            (it.get(buildingsKey) as List<BuildingPage>).forEach { buildPage ->
                buildPage.run()
            }
        }
        return@lazy sp
    }

    fun run() {
        spider.run()
    }

}

class BuildingPage(val jobName: String, val number: Int, val url: String) {
    private val buildKey = "buildKey"
    private val spider: Spider by lazy {
        val sp = Spider.create().request(jenkinsRequest(url))
        sp.parser { page ->
            val json: Json = page.getField(RESPONSE_JSON) as Json
            val result: String = json.select(JsonPathSelector("\$.result")).toString()
            val time: Long = json.select(JsonPathSelector("\$.timestamp")).toString().toLong()
            page.putField(buildKey, Building(jobName, number, Date(time), result))
        }
        sp.pipeline {
            Context.append(it.get<Building>(buildKey))
        }
        return@lazy sp

    }

    fun run() {
        spider.run()
    }
}