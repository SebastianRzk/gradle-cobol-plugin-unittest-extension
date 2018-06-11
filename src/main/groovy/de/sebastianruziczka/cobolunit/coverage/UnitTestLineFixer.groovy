package de.sebastianruziczka.cobolunit.coverage

import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.ABSOLUTE_FIXED_UNITTEST_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_SOURCEFILE_PATH

import de.sebastianruziczka.api.CobolSourceFile

class UnitTestLineFixer {


	public void fix(CobolSourceFile file) {
		def lines = []
		String inputFilePath = file.getMeta(BUILD_TEST_SOURCEFILE_PATH)
		String outputFilePath = file.getMeta(ABSOLUTE_FIXED_UNITTEST_PATH)

		new File(inputFilePath).eachLine { line ->
			lines << line + System.getProperty('line.separator')
		}
		File output = new File(outputFilePath)
		if (output.exists()) {
			output.delete()
		}
		output = new File(outputFilePath)

		String lastLine = lines[0]
		for (int i = 1; i < lines.size(); i ++) {
			String actualLine = lines[i]
			if (actualLine.trim().equals('.') && lastLine != null ) {
				if ( lastLine.length() > 4) {
					lastLine = lastLine[0..-2] + '.'
					output << lastLine + System.getProperty('line.separator')
					lastLine = null
					continue
				}
				output << actualLine + System.getProperty('line.separator')
				lastLine = null
				continue
			}

			if(lastLine != null) {
				output << lastLine
			}

			lastLine = actualLine
		}
		if(lastLine != null) {
			output << lastLine
		}
	}
}
