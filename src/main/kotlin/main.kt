package xyz.nietongxue.anyExporter

import xyz.nietongxue.engineering.io.Context
import xyz.nietongxue.engineering.io.MainPage
import java.util.*
import kotlin.concurrent.schedule
import io.prometheus.client.exporter.PushGateway
import io.prometheus.client.Gauge
import io.prometheus.client.CollectorRegistry




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
fun push (){
    val registry = CollectorRegistry()
    val duration = Gauge.build()
        .name("my_batch_job_duration_seconds").help("Duration of my batch job in seconds.").register(registry)
    val durationTimer = duration.startTimer()
    try {
        // Your code here.

        // This is only added to the registry after success,
        // so that a previous success in the Pushgateway isn't overwritten on failure.
        val lastSuccess = Gauge.build()
            .name("my_batch_job_last_success").help("Last time my batch job succeeded, in unixtime.").register(registry)
        lastSuccess.setToCurrentTime()
    } finally {
        durationTimer.setDuration()
        val pg = PushGateway("127.0.0.1:9091")
        pg.pushAdd(registry, "my_batch_job")
    }
}

