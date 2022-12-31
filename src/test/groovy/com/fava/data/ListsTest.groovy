package com.fava.data

import spock.lang.Specification

class ListsTest extends Specification {

	def <T> "test append #element to #list"() {
		expect:
		Lists.append(element as T, list as List<T>) == expected

		where:
		element | list   || expected
		1       | []     || [1]
		1       | [2]    || [2, 1]
		1       | [2, 3] || [2, 3, 1]
		_       | []     || [_]
	}

	def "test sort"() {
		given:
		def languages = ["Java", "Haskell", "C++", "Python", "Scala", "Groovy"]
		when:
		def result = Lists.sort(Strings.compareIgnoreCase()).apply(languages)
		then:
		result == languages.sort { it.toLowerCase() }
	}

	def "test map"() {
		expect:
		Lists.map(arg -> arg * arg).apply([1, 2, 3]) == [1, 4, 9]
	}

	def <T> "test #list unique element"(List<T> list, List<T> expect) {
		expect:
		Lists.unique().apply(list) == expect
		Lists.unique(list) == expect

		where:
		list               || expect
		[1, 2, 3, 1, 2, 3] || [1, 2, 3]
		[3, 1, 2]          || [3, 1, 2]
		[1, 2, 3]          || [1, 2, 3]
		[]                 || []
	}
}
