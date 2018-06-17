package de.sebastianruziczka.cobolunit.steps

import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXECFILE_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXEC_LOG_PATH

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.process.ProcessWrapper

class TestExectuableExecutor {

	private CobolExtension configuration

	public TestExectuableExecutor(CobolExtension configuration) {
		this.configuration = configuration
	}

	public String executeTest(CobolUnitSourceFile file) {
		file.setMeta(BUILD_TEST_EXEC_LOG_PATH, file.getMeta(BUILD_TEST_EXECFILE_PATH) + '_TESTEXEC.LOG')
		File executableDir = new File(file.getMeta(BUILD_TEST_EXECFILE_PATH)).getParentFile()
		ProcessWrapper processWrapper = new ProcessWrapper(
				[
					file.getMeta(BUILD_TEST_EXECFILE_PATH)
				],
				executableDir,
				'Execute Unittest '+ file.getMeta(BUILD_TEST_EXECFILE_PATH),
				file.getMeta(BUILD_TEST_EXEC_LOG_PATH))
		if (this.configuration.unittestCodeCoverage) {
			processWrapper.setEnvironmentVariable('COB_SET_TRACE', 'Y')
		}

		processWrapper.exec(true)
		return processWrapper.processOutput()
	}
}
