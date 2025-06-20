package com.example.codex.service

import com.example.codex.domain.VatCheckResult
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

@Service
class VatService {
    private val rest = RestTemplate()
    private val url = "https://ec.europa.eu/taxation_customs/vies/services/checkVatService"

    fun check(countryCode: String, vatNumber: String): VatCheckResult {
        val body = """
            <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">
               <soapenv:Header/>
               <soapenv:Body>
                  <tns:checkVat>
                     <tns:countryCode>${countryCode}</tns:countryCode>
                     <tns:vatNumber>${vatNumber}</tns:vatNumber>
                  </tns:checkVat>
               </soapenv:Body>
            </soapenv:Envelope>
        """.trimIndent()
        val headers = HttpHeaders().apply { contentType = MediaType.TEXT_XML }
        val response = rest.postForObject(url, HttpEntity(body, headers), String::class.java)
        val doc = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }.newDocumentBuilder().parse(InputSource(StringReader(response)))
        val valid = doc.getElementsByTagName("valid").item(0).textContent.toBoolean()
        val name = doc.getElementsByTagName("name").item(0).textContent
        val address = doc.getElementsByTagName("address").item(0).textContent
        return VatCheckResult(valid, name.ifBlank { null }, address.ifBlank { null })
    }
}
