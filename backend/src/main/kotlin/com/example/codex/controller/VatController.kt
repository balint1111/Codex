package com.example.codex.controller

import com.example.codex.domain.VatCheckResult
import com.example.codex.service.VatService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vat")
@CrossOrigin(origins = ["*"])
class VatController(private val vatService: VatService) {

    @GetMapping("/check")
    fun check(@RequestParam countryCode: String, @RequestParam vatNumber: String): VatCheckResult {
        return vatService.check(countryCode, vatNumber)
    }
}
