package com.fava

import spock.lang.Specification


class NumbersSpec extends Specification {
	def setupSpec() {
		println "setupSpec"
	}

	def setup() {
		println "setup"
	}

	def cleanup() {
		println "cleanup"
	}

	def cleanupSpec() {
		println "cleanupSpec"
	}

	def "spock test hello world"() {
		given:
		def a = 1
		def b = 2

		expect:
		a < b

		println "Spock test finished"
	}
}
