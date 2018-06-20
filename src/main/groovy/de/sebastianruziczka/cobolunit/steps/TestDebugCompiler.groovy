package de.sebastianruziczka.cobolunit.steps

import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXECFILE_PATH

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolCodeType
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.compiler.api.CompileJob

class TestDebugCompiler {

	private CobolExtension configuration
	private String frameworkPath;

	public TestDebugCompiler(CobolExtension configuration, String frameworkPath) {
		this.configuration = configuration
		this.frameworkPath = frameworkPath
	}

	public int compileTest(CobolUnitSourceFile file) {
		CompileJob job = this.configuration.compiler
				.buildDebug(this.configuration)
				.addIncludePath(file.getAbsoluteModulePath(CobolCodeType.source))
				.addIncludePath(file.getAbsoluteModulePath(CobolCodeType.integration_test))
				.addIncludePath(this.frameworkPath)
				.setTargetAndBuild(file.actualTestfilePath())

		if (this.configuration.unittestCodeCoverage) {
			job = job.addCodeCoverageOption()
		}
		file.setMeta(BUILD_TEST_EXECFILE_PATH, file.actualTestfilePath().replace(this.configuration.srcFileType, ''))
		return job.execute('Compile Debug Test {' + file.getRelativePath(CobolCodeType.integration_test) + '}')
	}
}
