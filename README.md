# Gradle-Cobol-Plugin-UnitTest-Extension
[![Build 
Status](https://travis-ci.org/RosesTheN00b/gradle-cobol-plugin-unittest-extension.svg?branch=master)](https://travis-ci.org/RosesTheN00b/gradle-cobol-plugin-unittest-extension)

This plugin extends the existing [gradle-cobol-plugin](https://github.com/RosesTheN00b/gradle-cobol-plugin) for unittests.

This gradle plugin is based on the existing github project [cobol-unit-test](https://github.com/neopragma/cobol-unit-test).

The new source code if fully backward compatible, but enables new features:

* No redundant configuration (the plugin provides a default-configuration for all tests)
* Fast and simple test exectuion by gradle with the task _:cobolUnit_


## Gradle-cobol environment

* [gradle-cobol-plugin](https://github.com/RosesTheN00b/gradle-cobol-plugin) The base gradle plugin (compile, run)
* [gradle-cobol-plugin-unittest-extension](https://github.com/RosesTheN00b/gradle-cobol-plugin-unittest-extension) Adds unittests to the base plugin
* [gradle-cobol-plugin-example](https://github.com/RosesTheN00b/gradle-cobol-plugin-example) This Project contains many gradle-cobol example projects

Further Reading:

* [GNUCobol-compiler](https://open-cobol.sourceforge.io/) The cobol compiler
* [cobol-unit-test](https://github.com/neopragma/cobol-unit-test) The documentation of the unit-test feature

## Installation

Add the plugin to the [gradle-cobol-plugin](https://github.com/RosesTheN00b/gradle-cobol-plugin) classpath:

build.gradle:

    buildscript {
    	 dependencies {
    		classpath group: 'de.sebastianruziczka', name: 'gradle-cobol-plugin-unittest-extension', version: '0.0.3'
    	}
    }

    plugins {
    	id 'de.sebastianruziczka.Cobol' version '0.0.12' apply false
    }


settings.gradle:

    pluginManagement {
    	repositories {
    		maven {
    			url 'https://sebastianruziczka.de/repo/mvn/'
    		}
    	}
    }
