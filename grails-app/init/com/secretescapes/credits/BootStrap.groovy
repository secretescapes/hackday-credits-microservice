package com.secretescapes.credits

class BootStrap {

	def init = { servletContext ->

		User.withTransaction {
			User user = User.read(1)
			if (!user) {
				user = User.create().save(flush: true)
				user.addToCredits(new Credit(user: user, amount: 100))
				user.addToCredits(new Credit(user: user, amount: 50))
				user.addToCredits(new Credit(user: user, amount: 50, status: CreditStatus.USED))
				user.addToCredits(new Credit(user: user, amount: 150, status: CreditStatus.DELETED))
				user.addToCredits(new Credit(user: user, amount: 50, expiresOn: new Date("2019/01/01")))
			}
		}

	}
	def destroy = {
	}
}
