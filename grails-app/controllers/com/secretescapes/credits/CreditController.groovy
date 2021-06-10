package com.secretescapes.credits

import com.secretescapes.credits.commands.RedeemCreditCommand
import com.secretescapes.credits.commands.exceptions.InsufficientAvailableCredit
import org.springframework.http.HttpStatus

class CreditController {
	static responseFormats = ['json']
	static allowedMethods = [redeemCredit: 'POST']

	CreditService creditService

	def index() {
		User user
		try {
			user = getAuhenticatedUser()
			if (!user) {
				log.info "${controllerName}.${actionName}: was't authenticated"
				render([status: HttpStatus.FORBIDDEN])
				return
			}

			Currency currency = params.currency ? Currency.getInstance(params.currency) : Currency.getInstance("GBP")
			BigDecimal amount = creditService.sumAmountOfAvailableCreditsByUserAndCurrency(user, currency)
			return [amount: amount, currency: currency]
		} catch (ex) {
			log.warn "${controllerName}.${actionName}: throw ${ex.message}", ex
			render([status: HttpStatus.INTERNAL_SERVER_ERROR])
			return
		}
	}

	def redeemCredit(RedeemCreditCommand command) {
		User user
		try {
			user = getAuhenticatedUser()
			if (!user) {
				log.info "${controllerName}.${actionName}: was't authenticated"
				render([status: HttpStatus.FORBIDDEN])
				return
			}
			if (!command.validate()) {
				log.info "${controllerName}.${actionName}: was't validated"
				render([status: HttpStatus.BAD_REQUEST])
				return
			}
			CreditOperation operation = creditService.redeemCredit(user, command)
			return [operation: operation]
		} catch (InsufficientAvailableCredit e) {
			log.info "${controllerName}.${actionName}: throw InsufficientAvailableCredit for User-${user?.id} and ${command.currency}:${command.requestedCreditAmount}"
			render([status: HttpStatus.CONFLICT])
		} catch (e) {
			log.warn "${controllerName}.${actionName}: throw ${ex.message}", ex
			render([status: HttpStatus.INTERNAL_SERVER_ERROR])
		}
	}

	private User getAuhenticatedUser(){
		String userId = request.getHeader("X-userId")
		return User.read(userId)
	}
}
