package com.secretescapes.credits

import com.secretescapes.credits.commands.RedeemCreditCommand
import com.secretescapes.credits.commands.exceptions.InsufficientAvailableCredit
import org.springframework.http.HttpStatus

class CreditController {
	static responseFormats = ['json']
	static allowedMethods = [index: 'GET', redeemCredit: 'POST']

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

	def redeemCredit(RedeemCreditCommand command) {
		try {
			String userId = request.getHeader("X-userId")
			User user = User.read(userId)
			if (!user) {
				render([status: HttpStatus.FORBIDDEN])
				return
			}
			if (!command.validate()) {
				render([status: HttpStatus.BAD_REQUEST])
				return
			}
			render([status: HttpStatus.OK])
		} catch (InsufficientAvailableCredit e) {
			render([status: HttpStatus.CONFLICT])
		} catch (e) {
			log.warn "${controllerName}.${actionName}: throw ${ex.message}", ex
			render([status: HttpStatus.INTERNAL_SERVER_ERROR])
		}
	}
}
