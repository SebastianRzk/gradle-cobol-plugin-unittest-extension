package de.sebastianruziczka.cobolunit;

import de.sebastianruziczka.api.CobolCodeType;
import de.sebastianruziczka.api.CobolSourceFile;

public class CobolUnitSourceFile {

	private CobolSourceFile sourcefile;
	private String frameworkpath;
	private String originalTestFile;
	private String fixedTestFilePath = null;

	public CobolUnitSourceFile(CobolSourceFile sourcefile, String frameworkpath, String originalTestFile) {
		this.sourcefile = sourcefile;
		this.frameworkpath = frameworkpath;
		this.originalTestFile = originalTestFile;
	}

	public String actualTestfilePath() {
		if (this.fixedTestFilePath != null) {
			return this.fixedTestFilePath;
		}
		return this.originalTestFile;
	}

	public String framworkPath() {
		return this.frameworkpath;
	}

	public String originalTestFilePath() {
		return this.originalTestFile;
	}

	public void modifyTestModulePath(String newPath) {
		this.fixedTestFilePath = newPath;
	}

	public String getRelativePath(CobolCodeType type) {
		return this.sourcefile.getRelativePath(type);
	}

	public String getAbsoluteModulePath(CobolCodeType type) {
		return this.sourcefile.getAbsoluteModulePath(type);
	}

	public String getAbsolutePath(CobolCodeType type) {
		return this.sourcefile.getAbsolutePath(type);
	}

	public void setMeta(String key, String value) {
		this.sourcefile.setMeta(key, value);
	}

	public String getMeta(String key) {
		return this.sourcefile.getMeta(key);
	}

	public String baseFileName() {
		return this.sourcefile.baseFileName();
	}

}
