package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod

class SourceFileReader {

	private CobolExtension configuration;

	public SourceFileReader(CobolExtension configuration) {
		this.configuration = configuration
	}

	protected String[] fileContent(String fileName) {
		File sourceFile = new File(new FixedFileConverter(this.configuration).fromFixedToOriginal(fileName))
		return sourceFile.text.split(System.getProperty('line.separator'))
	}

	public CobolCoverageFile read(String filename) {
		String sourceFileName = filename.replace(this.configuration.unittestPostfix, '')

		String[] srcFileContent = this.fileContent(sourceFileName)

		CobolCoverageFile cobolFile = new CobolCoverageFile(new FixedFileConverter(this.configuration).fromFixedToRelative(sourceFileName))
		CobolCoverageMethod actualMethod = null

		boolean procedureDivision = false
		int lineIndex = 0


		for (String line : srcFileContent) {
			lineIndex ++
			if (!procedureDivision) {
				if (line.startsWith('       PROCEDURE DIVISION')) {
					procedureDivision = true
				}
				continue
			}

			if (line.isAllWhitespace()) {
				continue
			}

			if (actualMethod == null) {
				actualMethod = new CobolCoverageMethod(line.trim()[0..-2], lineIndex + 1)
				continue
			}
			if (line.isAllWhitespace()) {
				actualMethod.addEmptyLine(lineIndex)
				continue
			}

			if (line.getAt(7) == '*') {
				actualMethod.addComment(lineIndex)
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
