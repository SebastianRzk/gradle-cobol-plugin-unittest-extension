package de.sebastianruziczka.cobolunit.steps

import static de.sebastianruziczka.api.CobolCodeType.source
import static de.sebastianruziczka.api.CobolCodeType.unit_test
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_PRECOMPILER_LOG_PATH

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolCodeType
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.compiler.api.CompileStandard
import de.sebastianruziczka.process.ProcessWrapper

class ZUTZCPC {
	Logger logger = LoggerFactory.getLogger('ZUTZCPC Provider')

	private final static MAIN_FRAMEWORK_PROGRAMM =  'ZUTZCPC.CBL'
	private final static MAIN_FRAMEWORK_BIN = 'ZUTZCPC'
	private String path

	private CobolExtension configuration

	public ZUTZCPC(String path, CobolExtension configuration) {
		this.path = path
		this.configuration = configuration
	}

	public void setup() {
		if(new File(this.path + '/' + MAIN_FRAMEWORK_BIN).exists()) {
			logger.info('ZUTZCPC Exectuable detected!')
			logger.info('Using exiting one')
			return
		}
		logger.info('Copy framework into build directory')
		this.copyFrameworkIntoBuildDirectory()

		logger.info('Start compiling cobol-unit test framework')
		this.compileTestFramework()
	}

	private void copyFrameworkIntoBuildDirectory() {
		def files = [
			MAIN_FRAMEWORK_PROGRAMM,
			'ZUTZCPD.CPY',
			'ZUTZCWS.CPY'
		]
		new File(this.path).mkdirs()
		logger.info('Moving sources of framwork into build')
		files.each{
			copy('res/' + it, path + '/' + it )
		}
	}

	private String frameworkPath() {
		return this.path + '/' + MAIN_FRAMEWORK_PROGRAMM
	}

	private String frameworkBinPath() {
		return this.path + '/' + MAIN_FRAMEWORK_BIN
	}

	private int compileTestFramework() {
		return this.configuration.compiler
				.buildExecutable(this.configuration)
				.setCompileStandard(CompileStandard.ibm)
				.setTargetAndBuild(this.frameworkPath())
				.execute('FrameworkCompile')
	}

	private void copy(String source, String destination) {
		logger.info('COPY: '+ source + '>>' + destination)
		URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader()
		URL manifestUrl = urlClassLoader.findResource(source)
		InputStream is = manifestUrl.openStream()

		File targetFile = new File(destination)
		OutputStream outStream = new FileOutputStream(targetFile)

		byte[] buffer = new byte[1024]
		int length
		while ((length = is.read(buffer)) > 0) {
			outStream.write(buffer, 0, length)
		}

		outStream.close()
		is.close()
	}

	public int preprocessTest(CobolUnitSourceFile file, String testConfig, CobolCodeType testCodeType) {
		ProcessBuilder processBuilder = new ProcessBuilder(this.frameworkBinPath())

		def env = processBuilder.environment()
		env.put('SRCPRG', file.getAbsolutePath(source))
		env.put('UTESTS', file.getAbsolutePath(testCodeType))

		env.put('TESTPRG', file.actualTestfilePath())
		env.put('TESTNAME', file.baseFileName())

		env.put('UTSTCFG', testConfig)

		logger.info('Environment: ' + env.dump())
		logger.info('Test precompile command args: ' + processBuilder.command().dump())

		file.setMeta(BUILD_TEST_PRECOMPILER_LOG_PATH, file.actualTestfilePath()+ '_PRECOMPILER.LOG')
		ProcessWrapper processWrapper = new ProcessWrapper(processBuilder, 'Preprocess UnitTest '+ file.getRelativePath(unit_test), file.getMeta(BUILD_TEST_PRECOMPILER_LOG_PATH))
		return processWrapper.exec()
	}
}
