# Gradle-Cobol-Plugin-UnitTest-Extension
[![Build Status](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-unittest-extension.svg?branch=master)](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-unittest-extension)


This plugin extends the existing [gradle-cobol-plugin](https://gradle-cobol.sebastianruziczka.de) for unittests.

This gradle plugin is based on the existing github project [cobol-unit-test](https://github.com/neopragma/cobol-unit-test).

The new source code if fully backward compatible, but enables new features:

* No redundant configuration (the plugin provides a default-configuration for all tests)
* Fast and simple test execution by gradle with the task _:cobolUnit_
* Simple testcoverage computation with task _cobolUnit_ _computeTestCoverage_ . This task generates a cobertura-testcoverage xml file.
* Integration tests (full application + ressource build)
* Integration tests with specific ressource files


## Gradle-cobol environment

* [![Build Status](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin.svg?branch=master)](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin)  [gradle-cobol-plugin](https://gradle-cobol.sebastianruziczka.de) Package of the *core* and the *unittest* plugin.
* [![Build Status](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-core.svg?branch=master)](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-core) The base gradle plugin (compile, run)
* [![Build Status](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-unittest-extension.svg?branch=master)](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-unittest-extension)[gradle-cobol-plugin-unittest-extension](https://gradle-cobol-unit.sebastianruziczka.de) Adds unittests and testcoverage to the base plugin
* [![Build Status](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-example.svg?branch=master)](https://travis-ci.org/SebastianRzk/gradle-cobol-plugin-example)[![codecov](https://codecov.io/gh/SebastianRzk/gradle-cobol-plugin-example/branch/master/graph/badge.svg)](https://codecov.io/gh/SebastianRzk/gradle-cobol-plugin-example)[gradle-cobol-plugin-example](https://github.com/SebastianRzk/gradle-cobol-plugin-example) 
This Project contains many gradle-cobol example projects

Further Reading:

* [GNUCobol-compiler](https://open-cobol.sourceforge.io/) The cobol compiler
* [cobol-unit-test](https://github.com/neopragma/cobol-unit-test) The documentation of the unit-test feature

## Supported versions

| Compiler | Code format | unit test | unit test testcoverage | integration test | integration test coverage | 
| -------- | ----------- | --------- | ---------------------- | ---------------- | --------------------------|
| GnuCobol / Open Cobol 1.1 | fixed | full support | full support | --- | --- |
| GnuCobol 2 | fixed | full support | full support | full support | full support |
| GnuCobol 3rc | fixed | full support | full support | full support | full support |




## Installation

Add the plugin to the [gradle-cobol-plugin](https://github.com/SebastianRzk/gradle-cobol-plugin) classpath:

build.gradle:

    plugins {
    	id 'de.sebastianruziczka.Cobol' version 'latest'
    }


Or hardcode specific versions in your build.gradle (not preferred):

    plugins {
    	id 'de.sebastianruziczka.Cobol' version '0.0.40'
    }

settings.gradle:

    pluginManagement {
    	repositories {
    		maven {
    			url 'https://repo.sebastianruziczka.de/mvn/'
    		}
    	}
    }

## Comparison unit <-> integration tests

|   | unit test | integration test |
| - | ---------| ---------------- |
| gradle command | `testUnit` | `testIntegration` |
| testcoverage | available 1) | available 2) |
| compile strategy | compile test and target | compile all cobol sourcefiles |
| ressource strategy | no ressources available | use sources in `/res/main/cobol` and `res/integrationtest/cobol/<testname>` |
| additional env variables | COBC_INTERACTIVE='N' | COBC_INTERACTIVE='N' |
| failing test kills `check` | yes | yes |


1)  set `unittestCodeCoverage = true` in your build gradle and execute tasks `testUnit computeTestcoverage`

2) set `integrationtestCodeCoverage = true` in your build gradle and execute tasks `testIntegration computeIntegrationTestCoverage`

## Unit tests

File conventions:

* Source file (target): `<module>/<filename>.<cobol.srcFileType>` e.g. `CRTDB/MAIN.cbl`
* Test file : `<module>/<filename><cobol.unittestPostfix>.<cobol.srcFileType>` e.g. `CRTDB/MAINUT.cbl`
* Note: properties starting with `cobol.` are configured in your `cobol`-block in your `build.gradle`

## Integration tests
    
* Source file (target): `<module>/<filename>.<cobol.srcFileType>` e.g. `CRTDB/MAIN.cbl`
* Test file : `<module>/<filename><cobol.integrationtestPostfix>.<cobol.srcFileType>` e.g. `CRTDB/MAINIT.cbl`
* Test ressources: `<cobol.resIntegrationTest>/<module>/<filename>/` e.g. `res/integrationtest/cobol/CRTDB/MAIN/`. All items in this folder will be copied in your build/exectution-directory of our integration test. Ressources from `res/main/cobol` will be replaced.
* Note: properties starting with `cobol.` are configured in your `cobol`-block in your `build.gradle`

### Example with default settings:

	Source Directory:
		- src/main/cobol/CRTDB/MAIN.cbl
		- src/main/cobol/CRTDB/OTHER.cbl
	
	Test Directory:
		- src/test/cobol/CRTDB/MAINIT.cbl --> Integration Test
	
	Ressources:
		- res/main/cobol/CRTDB/database1.db --> ressource version
		- res/main/cobol/CRTDB/database2.db --> ressource version
	
	Integration test ressources
		- res/integrationtest/cobol/CRTDB/MAIN/CRTDB/database1.db --> integrationtest version
		

The cobol integration test for the file  `CRTDB/MAIN.cbl` will contain the following files:

	Integration test directory:
		- $dir/CRTDB/MainIT.so
		- $dir/CRTDB/OTHER.so
		- $dir/CRTDB/database1.db --> integrationtest version (replaced ressource version)
		- $dir/CRTDB/database2.db --> ressource version

