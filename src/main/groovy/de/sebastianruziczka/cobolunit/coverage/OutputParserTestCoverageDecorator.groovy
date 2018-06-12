package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.cobolunit.OutputParser

class OutputParserTestCoverageDecorator {
	private OutputParser parser;
	private Map<CobolUnitSourceFile, List<String>> coverageOutput = new HashMap()

	public OutputParserTestCoverageDecorator(OutputParser parser) {
		this.parser = parser
	}


	public TestFile parse(CobolUnitSourceFile file, List<String> lines) {
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

	public Collection<CobolUnitSourceFile> testCoverageFiles(){
		return this.coverageOutput.keySet()
	}

	public List<String> getCoverageOutput(CobolUnitSourceFile fileName){
		return this.coverageOutput.get(fileName)
	}
}
