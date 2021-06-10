package com.secretescapes.credits

import com.secretescapes.credits.commands.RedeemCreditCommand
import com.secretescapes.credits.commands.exceptions.InsufficientAvailableCredit
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

@Transactional
class CreditService {

	@NotTransactional
	List<Credit> findAll(User user, Currency currency, CreditStatus creditStatus) {
	    List<Credit> credits = Credit.findAllByUserAndCurrencyAndStatus(user, currency, creditStatus)
	    return  credits
    }

	@NotTransactional
	BigDecimal sumAmountOfAvailableCreditsByUserAndCurrency(User user, Currency currency){
		// TODO Check expireOn
		List<Credit> credits = findAll(user, currency, CreditStatus.AVAILABLE)
		return credits ? credits.sum {it.amount} : BigDecimal.ZERO
	}

	CreditOperation redeemCredit(User user, RedeemCreditCommand command) {
		List<Credit> credits = findAll(user, command.currency, CreditStatus.AVAILABLE)
		BigDecimal availableAmount = credits ? credits.sum {it.amount} : BigDecimal.ZERO
		if (availableAmount < command.requestedCreditAmount) {
			throw new InsufficientAvailableCredit("User does not have enough available credit")
		}

		CreditOperation operation = new CreditOperation(type: OperationType.REDEEM).save(failOnError: true)

		credits.sort {it.amount}.each {
			it.status = CreditStatus.USED
			if (command.requestedCreditAmount < it.amount) {
				Credit newCredit = new Credit(amount: it.amount - command.requestedCreditAmount, expiresOn: it.expiresOn)
				it.amount = command.requestedCreditAmount
				user.addToCredits(newCredit)
				newCredit.save(failOnError: true)

			}
			operation.addToCredits(it)
		}

		return operation
	}


}
