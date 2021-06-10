package com.secretescapes.credits


import grails.rest.*
import grails.converters.*
import org.springframework.http.HttpStatus

class CreditController {
	static responseFormats = ['json']

	CreditService creditService

	def index() {
		try {

			String userId = request.getHeader("X-userId")
			User user = User.read(userId)
			if (!user) {
				render([status: HttpStatus.FORBIDDEN])
				return
			}

			Currency currency = params.currency ?: Currency.getInstance("GBP")
			BigDecimal amount = creditService.sumAmountOfAvailableCreditsByUserAndCurrency(user, currency)
			[amount: amount, currency: currency]
		} catch (ex) {
			log.warn "${controllerName}.${actionName}: throw ${ex.message}", ex
			render([status: HttpStatus.INTERNAL_SERVER_ERROR])
			return
		}

	}
}
