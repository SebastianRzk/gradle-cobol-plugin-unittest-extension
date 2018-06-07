package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.cobolunit.OutputParser

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

	public Collection<String> testCoverageFiles(){
		return this.coverageOutput.keySet()
	}

	public List<String> getCoverageOutput(String fileName){
		return this.coverageOutput.get(fileName)
	}
}
