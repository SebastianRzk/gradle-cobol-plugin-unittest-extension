package de.sebastianruziczka.cobolunit

import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.buildcycle.test.TestMethod
import de.sebastianruziczka.buildcycle.test.TestMethodResult

public class OutputParser {

	public TestFile parse(String testFileName, List<String> lines){
		if (!lines.get(1).equals('TEST SUITE:')){
			throw new IllegalArgumentException('Could not parse cobol unit test output')
		}

		TestFile result = new TestFile()
		result.addName(testFileName + '(' + lines.get(2).trim() + ')')

		for (int lineNumber = 3; lineNumber < lines.size(); lineNumber ++) {

			String actualLine = lines.get(lineNumber)

			if (actualLine.startsWith('     PASS:   ')) {
				String name = actualLine.substring('     PASS:   '.length()).trim()
				result.addTestMethod(new TestMethod(name, TestMethodResult.SUCCESSFUL, ''))
			}
			else if (actualLine.startsWith('**** FAIL:   ')) {
				String name = actualLine.substring('**** FAIL:   '.length()).trim()
				String compareResult = lines.get(lineNumber + 1)
				result.addTestMethod(new TestMethod(name, TestMethodResult.FAILED, compareResult.trim()))
			}
		}
		return result
	}
}
