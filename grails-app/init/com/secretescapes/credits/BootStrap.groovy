package com.secretescapes.credits

class BootStrap {

	def init = { servletContext ->

		User.withTransaction {
			(1..100).each {
				User user = User.read(it)
				if (!user) {
					user = User.create().save(flush: true)
					user.addToCredits(new Credit(user: user, amount: 500))
				}
			}

		}

	}
	def destroy = {
	}
}
