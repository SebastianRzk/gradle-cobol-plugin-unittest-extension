package de.sebastianruziczka.cobolunit.coverage
import de.sebastianruziczka.CobolExtension

class FixedFileConverter {

	static final String FIXED_FILENAME_POSTFIX = '_fixed'

	private CobolExtension configuration

	public FixedFileConverter(CobolExtension configuration) {
		this.configuration = configuration
	}

	String fromOriginalToFixed(String fileName) {
		return fileName.replaceAll(this.configuration.srcFileType, '') + FIXED_FILENAME_POSTFIX + this.configuration.srcFileType
	}

	String fromFixedToOriginal(String fileName)  {
		return configuration.absoluteSrcMainPath(fileName).replaceAll(FIXED_FILENAME_POSTFIX, '');
	}

	String fromFixedToRelative(String fileName) {
		return fileName.replaceAll(FIXED_FILENAME_POSTFIX, '')
	}
}