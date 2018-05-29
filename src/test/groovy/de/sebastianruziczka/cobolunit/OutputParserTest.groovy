package de.sebastianruziczka.cobolunit;

import static org.assertj.core.api.Assertions.assertThat

import org.junit.Test

import de.sebastianruziczka.buildcycle.test.TestFile

class OutputParserTest {


	List<String> outputFail(){
		return [
			" ",
			"TEST SUITE:",
			"GREETING AND FAREWELL ",
			"",
			"HELLO Hans",
			"     PASS:   1. IT RETURNS HELLO, WORLD! AS GREETING                                            ",
			" ",
			"  1 TEST CASES WERE EXECUTED",
			"  1 PASSED",
			"  0 FAILED",
			"================================================="
		]
	}


	@Test
	void test_parseFailed() {
		OutputParser component_under_test = new OutputParser()

		TestFile result = component_under_test.parse("TESTFAIL", this.outputFail())

		assertThat(result.name()).isEqualTo('TESTFAIL(GREETING AND FAREWELL)')
	}
}
