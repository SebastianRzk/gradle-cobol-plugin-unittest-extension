package de.sebastianruziczka.cobolunit

import static de.sebastianruziczka.api.CobolCodeType.source
import static de.sebastianruziczka.api.CobolCodeType.unit_test
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.ABSOLUTE_FIXED_UNITTEST_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXECFILE_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXEC_LOG_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_PRECOMPILER_LOG_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_SOURCEFILE_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.RELATIVE_FIXED_UNITTEST_PATH
import static de.sebastianruziczka.compiler.api.CompileStandard.ibm

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.api.CobolTestFramework
import de.sebastianruziczka.api.CobolUnitFrameworkProvider
import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.cobolunit.coverage.ComputeTestCoverageTask
import de.sebastianruziczka.cobolunit.coverage.FixedFileConverter
import de.sebastianruziczka.cobolunit.coverage.OutputParserTestCoverageDecorator
import de.sebastianruziczka.cobolunit.coverage.UnitTestLineFixer
import de.sebastianruziczka.compiler.api.CompileJob
import de.sebastianruziczka.metainf.MetaInfPropertyResolver
import de.sebastianruziczka.process.ProcessWrapper

@CobolUnitFrameworkProvider
class CobolUnit implements CobolTestFramework{
	Logger logger = LoggerFactory.getLogger('cobolUnit')

	private CobolExtension configuration
	private Project project
	private def defaultConf = ["ZUTZCWS", "SAMPLET"]
	private final static MAIN_FRAMEWORK_PROGRAMM =  'ZUTZCPC.CBL'
	private final static DEFAULT_CONF_NAME = 'DEFAULT.CONF'
	private String pluginName = null
	private OutputParserTestCoverageDecorator testCoverageProvider;

	@Override
	void configure(CobolExtension configuration, Project project) {
		this.configuration = configuration
		this.project = project

		this.project.task('computeTestCoverage', type:ComputeTestCoverageTask){

			group: 'COBOL Development'
			description: 'Generates a testcoverage xml (cobertura-style)'

			doFirst{
				testOuput = this.testCoverageProvider
				conf = this.configuration
			}
		}
	}

	@Override
	int prepare() {
		logger.info('Copy framework into build directory')
		this.copyFrameworkIntoBuildDirectory()

		logger.info('Create default test.conf')
		this.createTestConf()

		logger.info('Start compiling cobol-unit test framework')
		return this.compileTestFramework(this.frameworkBin() + '/' + MAIN_FRAMEWORK_PROGRAMM)
	}

	private void copyFrameworkIntoBuildDirectory() {
		def files = [
			MAIN_FRAMEWORK_PROGRAMM,
			'ZUTZCPD.CPY',
			'ZUTZCWS.CPY'
		]
		String binFrameworkPath = this.frameworkBin()+ '/'
		new File(binFrameworkPath).mkdirs()
		logger.info('Moving sources of framwork into build')
		files.each{
			copy('res/' + it, binFrameworkPath + it )
		}
	}

	private int compileTestFramework(String mainfile) {
		return this.configuration.compiler
				.buildExecutable(this.configuration)
				.setCompileStandard(ibm)
				.setTargetAndBuild(mainfile)
				.execute('FrameworkCompile')
	}

	private void createTestConf() {
		String path = this.defaultConfPath()
		logger.info('Using Path: '+path)
		def defaultConfFile = new File(path)
		defaultConfFile.delete()

		defaultConfFile.withWriter { out ->
			this.defaultConf.each { out.println it }
		}
	}

	private String defaultConfPath() {
		return this.frameworkBin() + '/' + DEFAULT_CONF_NAME
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

	@Override
	public TestFile test(CobolSourceFile file) {
		String testName = file.getRelativePath(unit_test)
		file.setMeta(BUILD_TEST_SOURCEFILE_PATH, this.frameworkBin() + '/' + file.getRelativePath(unit_test))

		String testBuildPath = this.frameworkBin() + '/' + testName
		File buildTestModule = new File(this.getParent(testBuildPath))
		if (!buildTestModule.exists()) {
			this.logger.info('Creating test directory ' + testBuildPath)
			buildTestModule.mkdirs()
		}

		logger.info('Preprocess Test: ' + testName)
		this.preprocessTest(file, null)

		if(this.configuration.unittestCodeCoverage) {
			file.setMeta(RELATIVE_FIXED_UNITTEST_PATH, this.frameworkBin() + '/' + new FixedFileConverter(this.configuration).fromOriginalToFixed(file.getRelativePath(unit_test)))
			file.setMeta(ABSOLUTE_FIXED_UNITTEST_PATH, this.configuration.projectFileResolver(file.getMeta(RELATIVE_FIXED_UNITTEST_PATH)).absolutePath)

			new UnitTestLineFixer().fix(file)
			file.setMeta(BUILD_TEST_SOURCEFILE_PATH, file.getMeta(RELATIVE_FIXED_UNITTEST_PATH))
		}

		logger.info('Compile Test: ' + testName)
		this.compileTest(file)
		logger.info('Run Test: ' + testName)
		String result = this.executeTest(file)

		return this.parseProcessOutput(result, file)
	}

	private TestFile parseProcessOutput(String processOutput, CobolSourceFile file) {
		List<String> lines = Arrays.asList(processOutput.split(System.getProperty('line.separator')))
		OutputParser parser = new OutputParser(this.configuration)
		if (this.configuration.unittestCodeCoverage) {
			if (this.testCoverageProvider == null) {
				this.testCoverageProvider = new OutputParserTestCoverageDecorator(parser)
			}
			this.testCoverageProvider.parse(file, lines)
		}
		return parser.parse(file, lines)
	}

	private String executeTest(CobolSourceFile file) {
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

	private String frameworkBinModuleOf(String relativePath) {
		String absolutePath = this.frameworkBin() + '/' + relativePath
		return this.configuration.projectFileResolver(absolutePath).getParent()
	}

	private int compileTest(CobolSourceFile file) {
		CompileJob job = this.configuration.compiler
				.buildExecutable(this.configuration)
				.addIncludePath(file.getAbsoluteModulePath(source))//
				.addIncludePath(file.getAbsoluteModulePath(unit_test))
				.addIncludePath(this.frameworkBin())
				.setTargetAndBuild(file.getMeta(BUILD_TEST_SOURCEFILE_PATH))

		if (this.configuration.unittestCodeCoverage) {
			job = job.addCodeCoverageOption()
		}
		file.setMeta(BUILD_TEST_EXECFILE_PATH, file.getMeta(BUILD_TEST_SOURCEFILE_PATH).replace(this.configuration.srcFileType, ''))
		return job.execute('Compile UnitTest {' + file.getRelativePath(unit_test) + '}')
	}

	private String frameworkBin() {
		return this.configuration.absoluteUnitTestFrameworkPath(this.getClass().getSimpleName())
	}

	private int preprocessTest(CobolSourceFile file, String testConfig) {
		ProcessBuilder processBuilder = new ProcessBuilder(this.ZUTZCPC())

		def env = processBuilder.environment()
		env.put('SRCPRG', file.getAbsolutePath(source))
		env.put('UTESTS', file.getAbsolutePath(unit_test))

		env.put('TESTPRG', file.getMeta(BUILD_TEST_SOURCEFILE_PATH))
		env.put('TESTNAME', this.getFileName(file.getRelativePath(source)))

		if (testConfig == null) {
			env.put('UTSTCFG', this.defaultConfPath())
		}else {
			env.put('UTSTCFG', testConfig)
		}

		logger.info('Environment: ' + env.dump())
		logger.info('Test precompile command args: ' + processBuilder.command().dump())

		file.setMeta(BUILD_TEST_PRECOMPILER_LOG_PATH, file.getMeta(BUILD_TEST_SOURCEFILE_PATH) + '_PRECOMPILER.LOG')
		ProcessWrapper processWrapper = new ProcessWrapper(processBuilder, 'Preprocess UnitTest '+ file.getRelativePath(unit_test), file.getMeta(BUILD_TEST_PRECOMPILER_LOG_PATH))
		return processWrapper.exec()
	}

	private String ZUTZCPC() {
		return this.frameworkBin() + '/' + this.getFileName(MAIN_FRAMEWORK_PROGRAMM)
	}

	private String getFileName(String path) {
		File file = new File(path)
		String name = file.getName()
		if (name.contains(".")) {
			name = name.split("\\.")[0]
		}
		return name
	}

	private String getParent(String path) {
		File file = new File(path)
		return file.getParent()
	}

	private String versionNumber() {
		MetaInfPropertyResolver resolver = new MetaInfPropertyResolver('gradle-cobol-plugin-unittest-extension')
		return resolver.get('Implementation-Version').orElse('No version found!') + ' (' + resolver.get('Build-Date').orElse('No date found') + ')'
	}


	@Override
	public String toString() {
		if (this.pluginName == null) {
			this.pluginName =  this.getClass().getSimpleName() + ' version: ' + versionNumber()
		}
		return this.pluginName
	}

	@Override
	public void clean() {
		this.testCoverageProvider = null
	}

}