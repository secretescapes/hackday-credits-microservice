package com.secretescapes.credits

class BootStrap {

	def init = { servletContext ->

		User.withTransaction {
			User user = User.read(1)
			if (!user) {
				user = User.create().save(flush: true)

				Credit credit = new Credit(user: user, amount: 100)
				credit.save()
				user.addToCredits(credit)
			}
		}

	}
	def destroy = {
	}
}
