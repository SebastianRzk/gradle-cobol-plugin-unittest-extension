package de.sebastianruziczka.cobolunit

import de.sebastianruziczka.buildcycle.test.TestFile

class OutputParserTestCoverageDecorator {
	private OutputParser parser;
	private Map<String, List<String>> coverageOutput = new HashMap()

	public OutputParserTestCoverageDecorator(OutputParser parser) {
		this.parser = parser
	}


	public TestFile parse(String testFileName, List<String> lines) {
		List<String> outputLines = new LinkedList<>()
		List<String> testcoverageLines = new LinkedList<>()
		for (String line : lines) {
			if(line.startsWith('Program-Id: ')) {
				testcoverageLines << line
			}else {
				outputLines << line
			}
		}
		this.coverageOutput.put(testFileName, testcoverageLines)
		return this.parser.parse(testFileName, outputLines)
	}
}
