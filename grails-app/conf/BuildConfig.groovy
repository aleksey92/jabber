grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        mavenLocal()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        compile 'jivesoftware:smack:3.0.4'
        compile 'jivesoftware:smackx:3.0.4'
    }

    plugins {
        build ':release:3.0.1', ':rest-client-builder:2.0.3', {
            export = false
        }
    }
}
