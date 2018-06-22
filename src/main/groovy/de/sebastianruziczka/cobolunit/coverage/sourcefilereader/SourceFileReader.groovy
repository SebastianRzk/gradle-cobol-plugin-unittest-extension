package de.sebastianruziczka.cobolunit.coverage.sourcefilereader

import static de.sebastianruziczka.api.CobolCodeType.source

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod

class SourceFileReader {

	private CobolExtension configuration
	private static final Logger LOGGER = LoggerFactory.getLogger(SourceFileReader.class.getName())

	public SourceFileReader(CobolExtension configuration) {
		this.configuration = configuration
	}

	protected String[] fileContent(String fileName) {
		File sourceFile = new File(fileName)
		return sourceFile.text.split(System.getProperty('line.separator'))
	}

	public CobolCoverageFile read(CobolUnitSourceFile file) {

		String[] srcFileContent = this.fileContent(file.getAbsolutePath(source))

		CobolCoverageFile cobolFile = new CobolCoverageFile(file)
		CobolCoverageMethod actualMethod = null

		boolean procedureDivision = false
		int lineIndex = 0


		for (String line : srcFileContent) {
			lineIndex ++
			if (!procedureDivision) {
				if (line.trim().startsWith('PROCEDURE DIVISION')) {
					procedureDivision = true
				}
				continue
			}

			if (line.isAllWhitespace() && actualMethod == null) {
				continue
			}

			if (actualMethod == null) {
				actualMethod = new CobolCoverageMethod(this.getName(line), lineIndex + 1)
				continue
			}
			if (line.isAllWhitespace()) {
				actualMethod.addEmptyLine(lineIndex)
				continue
			}

			if (this.isComment(line)) {
				actualMethod.addComment(lineIndex)
				continue
			}

			if (this.isFollowing(line)) {
				actualMethod.addFollowLine(lineIndex)
				continue
			}

			if (IgnoredToken.matchAny(line)) {
				actualMethod.addIgnoredLine(lineIndex)
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
		LOGGER.debug("SourcefileReader read file:")
		LOGGER.debug(cobolFile.toString())
		return cobolFile
	}

	private boolean isComment(String line) {
		return line.getAt(7) == '*'
	}

	private boolean isFollowing(String line) {
		return line.getAt(7) == '-';
	}

	private boolean isNoName(String line) {
		return (this.isComment(line) || this.isFollowing(line) || line.getAt(7).equals(' '))
	}

	private String getName(String line) {
		if(this.isNoName(line)) {
			return '<no name set>'
		}
		return line.trim()[0..-2]
	}
}
