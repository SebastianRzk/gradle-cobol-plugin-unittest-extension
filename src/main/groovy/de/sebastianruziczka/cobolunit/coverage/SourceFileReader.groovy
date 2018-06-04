package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod

class SourceFileReader {

	private CobolExtension configuration;

	public SourceFileReader(CobolExtension configuration) {
		this.configuration = configuration
	}

	public CobolCoverageFile read(String filename) {
		String sourceFileName = filename.replace(this.configuration.unittestPostfix, '')
		File sourceFile = new File(configuration.absoluteSrcMainPath(sourceFileName))

		CobolCoverageMethod actualMethod = null
		CobolCoverageFile cobolFile = new CobolCoverageFile(sourceFileName)

		boolean procedureDivision = false
		int lineIndex = 0


		for (String line : sourceFile.text.split(System.getProperty('line.separator'))) {
			lineIndex ++
			if (!procedureDivision) {
				if (line.startsWith('       PROCEDURE DIVISION.')) {
					procedureDivision = true
				}
				continue
			}

			if (actualMethod == null) {
				actualMethod = new CobolCoverageMethod(line.trim()[0..-2], lineIndex + 1)
				continue
			}
			println "lline: "+(lineIndex)+': '+line

			if (line.isAllWhitespace()) {
				actualMethod.addEmptyLine(lineIndex)
				println 'whitespace'
				continue
			}

			if (line.getAt(7) == '*') {
				actualMethod.addComment(lineIndex)
				println 'comment'
				continue
			}

			if (line.getAt(7) == '-') {
				actualMethod.addFollowLine(lineIndex)
				continue
			}

			if (line.getAt(7) != ' ') {
				actualMethod.setEnd(lineIndex-1)
				cobolFile.addMethod(actualMethod)
				if (line.startsWith('       END PROGRAM')) {
					break
				}
				actualMethod = new CobolCoverageMethod(line.trim()[0..-2], lineIndex + 1)
			}
		}
		return cobolFile
	}
}
