package org.example.service

import org.example.dto.CrossRateRequest
import org.example.importer.WooparooIdCache
import org.example.repository.CrossRateRepository
import org.springframework.stereotype.Service

@Service
class CrossRateService(
    private val crossRateRepository: CrossRateRepository,
    private val wooparooIdCache: WooparooIdCache
) {

    fun query(request: CrossRateRequest) =
        crossRateRepository.findTop50(
            leftId = wooparooIdCache.getId(request.left),
            rightId = wooparooIdCache.getId(request.right),
            crossType = request.crossType,
            luckUpEvent = request.luckUpEvent
        )
}
