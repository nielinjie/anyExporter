package xyz.nietongxue.anyExporter

import com.cv4j.netdiscovery.core.domain.Page
import com.cv4j.netdiscovery.core.parser.Parser
import java.io.UnsupportedEncodingException


class ExporterParser : Parser {
    override fun process(page: Page) {
        val resultItems = page.resultItems
        resultItems.put("ua", page.html.xpath("//div[@id='liste']/ul/li/a/text()").all())

        val parser = page.request.urlParser

        try {
            resultItems.put("fileName", parser.getParam("name"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

}