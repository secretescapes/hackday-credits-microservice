package com.secretescapes.credits

import com.secretescapes.credits.commands.RedeemCreditCommand
import com.secretescapes.credits.commands.exceptions.InsufficientAvailableCredit
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

@Transactional
class CreditService {

	@NotTransactional
	List<Credit> findAll(User user, Currency currency, CreditStatus creditStatus) {
	    return Credit.findAllByUserAndCurrencyAndStatus(user, currency, creditStatus)
    }

	@NotTransactional
	BigDecimal sumAmountOfAvailableCreditsByUserAndCurrency(User user, Currency currency){
		List<Credit> credits = findAll(user, currency, CreditStatus.AVAILABLE)
		Date now = new Date()
		return credits ? credits.findAll{ !it.expiresOn || it.expiresOn > now }.sum {it.amount} : BigDecimal.ZERO
	}

	CreditOperation redeemCredit(User user, RedeemCreditCommand command) {
		List<Credit> credits = findAll(user, command.currency, CreditStatus.AVAILABLE)
		BigDecimal availableAmount = credits ? credits.sum {it.amount} : BigDecimal.ZERO
		if (availableAmount < command.requestedCreditAmount) {
			throw new InsufficientAvailableCredit("User does not have enough available credit")
		}

		CreditOperation operation = new CreditOperation(type: OperationType.REDEEM).save(failOnError: true)
		BigDecimal appliedCredit = BigDecimal.ZERO

		credits.sort {it.amount}.each {
			if (appliedCredit < command.requestedCreditAmount) {
				if ((command.requestedCreditAmount - appliedCredit) < it.amount) {
					Credit newCredit = new Credit(amount: it.amount - command.requestedCreditAmount, expiresOn: it.expiresOn)
					it.amount = command.requestedCreditAmount
					user.addToCredits(newCredit)
					newCredit.save(failOnError: true)
				}
				it.status = CreditStatus.USED
				appliedCredit += it.amount
				operation.addToCredits(it)
			}
		}

		return operation
	}


}
