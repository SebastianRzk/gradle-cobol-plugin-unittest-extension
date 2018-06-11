package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.cobolunit.OutputParser

class OutputParserTestCoverageDecorator {
	private OutputParser parser;
	private Map<CobolSourceFile, List<String>> coverageOutput = new HashMap()

	public OutputParserTestCoverageDecorator(OutputParser parser) {
		this.parser = parser
	}


	public TestFile parse(CobolSourceFile file, List<String> lines) {
		List<String> outputLines = new LinkedList<>()
		List<String> testcoverageLines = new LinkedList<>()
		for (String line : lines) {
			if(line.startsWith('Program-Id: ') || line.startsWith('PROGRAM-ID:')) {
				testcoverageLines << line
			}else {
				outputLines << line
			}
		}
		this.coverageOutput.put(file, testcoverageLines)
		return this.parser.parse(file, outputLines)
	}

	public Collection<CobolSourceFile> testCoverageFiles(){
		return this.coverageOutput.keySet()
	}

	public List<String> getCoverageOutput(CobolSourceFile fileName){
		return this.coverageOutput.get(fileName)
	}
}
