package com.fava.data

import spock.lang.Specification

class MaybeTest extends Specification {

	def "test just"() {
		given:
		Maybe<Integer> maybe = Maybe.just(1)
		expect:
		maybe.getValue() == 1
		maybe.hasValue()
	}

	def "test nothing"() {
		given:
		Maybe<?> maybe = Maybe.nothing()
		when:
		def value = maybe.getValue()
		def hasValue = maybe.hasValue()
		then:
		!hasValue
		value == null
	}

	def "test intToStr fmap"(int element, String expected) {
		when:
		def result = Maybe.just(element).fmap(String::valueOf).getValue()

		then:
		result == expected

		where:
		element || expected
		1       || "1"
	}

	def "test intToStr fmap with nothing"() {
		when:
		def res = Maybe.nothing().fmap(String::valueOf)

		then:
		!res.hasValue()
		res.getValue() == null
	}
}
