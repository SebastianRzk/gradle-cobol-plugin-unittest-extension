package de.sebastianruziczka.cobolunit.coverage

class UnitTestLineFixer {


	public void fix(String inputFilePath, String outputFilePath) {
		def lines = []
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
					println 'skipping dot'
					lastLine = lastLine[0..-2] + '.'
					output << lastLine + System.getProperty('line.separator')
					lastLine = null
					continue
				}
				println 'skipping dot'
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
