package de.sebastianruziczka.cobolunit

import static de.sebastianruziczka.compiler.api.CompileStandard.ibm

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolTestFramework
import de.sebastianruziczka.api.CobolUnitFrameworkProvider
import de.sebastianruziczka.buildcycle.test.TestFile
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

	@Override
	void configure(CobolExtension configuration, Project project) {
		this.configuration = configuration
		this.project = project
	}

	@Override
	int prepare() {
		logger.info('Copy framework into build directory')
		String binFramworkPath = this.copyFrameworkIntoBuildDirectory()

		logger.info('Create default test.conf')
		this.createTestConf()

		logger.info('Start compiling cobol-unit test framework')
		return this.compileTestFramework(binFramworkPath, MAIN_FRAMEWORK_PROGRAMM)
	}

	private String copyFrameworkIntoBuildDirectory() {
		def files = [
			MAIN_FRAMEWORK_PROGRAMM,
			'ZUTZCPD.CPY',
			'ZUTZCWS.CPY'
		]
		String binFramworkPath = this.frameworkBin()+ '/'
		new File(binFramworkPath).mkdirs()
		logger.info('Moving sources of framwork into build')
		files.each{
			copy('res/' + it, binFramworkPath + it )
		}
		return binFramworkPath
	}

	private int compileTestFramework(String frameworkPath,String mainfile) {
		return this.configuration.compiler
				.buildExecutable(this.configuration)
				.setCompileStandard(ibm)
				.setTargetAndBuild(this.frameworkBin() + '/' + MAIN_FRAMEWORK_PROGRAMM)
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
	public TestFile test(String srcName, String testName) {
		String srcModulePath = this.configuration.absoluteSrcMainModulePath(srcName)
		String testModulePath = this.testModuleOf(testName)

		String testBuildPath = this.frameworkBin() + '/' + testName
		File buildTestModule = new File(this.getParent(testBuildPath))
		if (!buildTestModule.exists()) {
			this.logger.info('Creating test directory ' + testBuildPath)
			buildTestModule.mkdirs()
		}

		logger.info('Preprocess Test: ' + testName)
		this.preprocessTest(srcName, testName, null)
		logger.info('Compile Test: ' + testName)
		this.compileTest(srcModulePath, testModulePath, testName)
		logger.info('Run Test: ' + testName)
		String result = this.executeTest(this.frameworkBinModuleOf(testName), this.getFileName(testName))

		return this.parseProcessOutput(result, testName)
	}

	private TestFile parseProcessOutput(String processOutput, String testFileName) {
		List<String> lines = Arrays.asList(processOutput.split(System.getProperty('line.separator')))
		OutputParser parser = new OutputParser()
		return parser.parse(testFileName, lines)
	}

	private String executeTest(String binModulePath, String execName) {
		def logFilePath = binModulePath + '/' + execName + '_TESTEXEC.LOG'

		ProcessWrapper processWrapper = new ProcessWrapper([
			binModulePath + '/' + execName
		], new File(binModulePath),  'Execute Unittest '+ execName, logFilePath)

		if (this.configuration.unittestCodeCoverage) {
			processWrapper.setEnvironmentVariable('COB_SET_TRACE', 'Y')
		}

		processWrapper.exec(true)
		return processWrapper.processOutput()
	}

	private String testModuleOf(String relativePath) {
		String absolutePath = this.configuration.absoluteSrcTestPath() + '/' + relativePath
		return this.configuration.projectFileResolver(absolutePath).getParent()
	}

	private String frameworkBinModuleOf(String relativePath) {
		String absolutePath = this.frameworkBin() + '/' + relativePath
		return this.configuration.projectFileResolver(absolutePath).getParent()
	}

	private int compileTest(String srcModulePath, String testModulePath, String testName) {
		String precompiledTestPath = this.frameworkBin() + '/' + testName
		CompileJob job = this.configuration.compiler
				.buildExecutable(this.configuration)
				.addIncludePath(srcModulePath)//
				.addIncludePath(testModulePath)
				.addIncludePath(this.frameworkBin())
				.setTargetAndBuild(precompiledTestPath)
		if (this.configuration.unittestCodeCoverage) {
			job = job.addCodeCoverageOption()
		}
		return job.execute('Compile UnitTest {' + testName + '}')
	}

	private String frameworkBin() {
		return this.configuration.absoluteUnitTestFrameworkPath(this.getClass().getSimpleName())
	}

	private int preprocessTest(String mainFile, String testFile, String testConfig) {
		String zutzcpcPath = this.frameworkBin() + '/' + this.getFileName(MAIN_FRAMEWORK_PROGRAMM)
		ProcessBuilder processBuilder = new ProcessBuilder(zutzcpcPath)

		def env = processBuilder.environment()
		env.put('SRCPRG', this.configuration.absoluteSrcMainPath(mainFile))
		env.put('TESTPRG', this.frameworkBin() + '/' + testFile)
		env.put('TESTNAME', this.getFileName(testFile))
		if (testConfig == null) {
			env.put('UTSTCFG', this.defaultConfPath())
		}else {
			env.put('UTSTCFG', testConfig)
		}
		env.put('UTESTS', this.configuration.absoluteSrcTestPath() + '/' + testFile)

		logger.info('Environment: ' + env.dump())
		logger.info('Test precompile command args: ' + processBuilder.command().dump())

		def logPath = this.frameworkBinModuleOf(mainFile) + '/' + this.getFileName(testFile) + '_' + 'PRECOMPILER.LOG'
		ProcessWrapper processWrapper = new ProcessWrapper(processBuilder, 'Preprocess UnitTest '+ testFile, logPath)
		return processWrapper.exec()
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
}