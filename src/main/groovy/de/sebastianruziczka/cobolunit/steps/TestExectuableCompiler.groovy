package de.sebastianruziczka.cobolunit.steps

import static de.sebastianruziczka.api.CobolCodeType.source
import static de.sebastianruziczka.api.CobolCodeType.unit_test
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXECFILE_PATH

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.compiler.api.CompileJob

class TestExectuableCompiler {

	private CobolExtension configuration
	private String frameworkPath;

	public TestExectuableCompiler(CobolExtension configuration, String frameworkPath) {
		this.configuration = configuration
		this.frameworkPath = frameworkPath
	}

	public int compileTest(CobolUnitSourceFile file) {
		CompileJob job = this.configuration.compiler
				.buildExecutable(this.configuration)
				.addIncludePath(file.getAbsoluteModulePath(source))//
				.addIncludePath(file.getAbsoluteModulePath(unit_test))
				.addIncludePath(frameworkPath)
				.setTargetAndBuild(file.actualTestfilePath())

		if (this.configuration.unittestCodeCoverage) {
			job = job.addCodeCoverageOption()
		}
		file.setMeta(BUILD_TEST_EXECFILE_PATH, file.actualTestfilePath().replace(this.configuration.srcFileType, ''))
		return job.execute('Compile UnitTest {' + file.getRelativePath(unit_test) + '}')
	}
}
