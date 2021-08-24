@NonCPS
def configFromBranch(branch) {

    if (branch ==~ /(develop)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuild: false,
            nexusUpload: true,
            jarSuffix: '-SNAPSHOT',
            env: 'develop',

        ]
    }
    if (branch ==~ /(DEVOPS-81)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuild: false,
            nexusUpload: true,
            jarSuffix: '-SNAPSHOT',
            env: 'develop',

        ]
    }
        else if (branch ==~ /(staging)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            nexusUpload: false,
            jarSuffix: '-RC',
            env: 'staging',

        ]
    }
    else if (branch ==~ /(master)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            nexusUpload: true,
            jarSuffix: '',
            env: 'production',

        ]
    }
    else {
        return [
            shouldTest: true,
            shouldAnalyze: false,
            shouldBuildImage: false,
            shouldPushImage: false,
            shouldDeploy: false,
            nexusUpload: false,
            jarSuffix: "-${BRANCH_NAME}",
            tag: '-',
            deployments: []
        ]
    }
}

pipeline {
    agent none

    environment {
        CONFIG = configFromBranch(BRANCH_NAME)
        SHOULD_TEST = "${CONFIG.shouldTest}"
        SHOULD_ANALYZE = "${CONFIG.shouldAnalyze}"
        SHOULD_BUILD = "${CONFIG.shouldBuild}"
        NEXUS_UPLOAD = "${CONFIG.nexusUpload}"
        JAR_SUFFIX = "${CONFIG.jarSuffix}"
        ENV = "${CONFIG.env}"

        GIT_URL="git@bitbucket.org:machina/elasticsearch-adapter.git"
    }

    stages {
        stage ('CI') {
            agent {
                label 'TestContainer'
            }
            stages {
                stage('SCM - Checkout') {
                    steps{
                        cleanWs()
                        git branch: BRANCH_NAME,
                        url: GIT_URL
                        echo 'SCM Checkout'
                    }
                }
                stage ('Tests'){
                    when {
                        expression { SHOULD_TEST == 'true' }
                    }
                    stages {
                        stage('Integration Tests - Gradle'){
                            steps {
                                sh "./gradlew -i clean test jacocoTestReport jacocoTestCoverageVerification --stacktrace"
                            }
                        }
                        stage('Code coverage - Jacoco'){
                            steps{
                                jacoco(
                                    execPattern: 'target/*.exec',
                                    classPattern: 'target/classes',
                                    sourcePattern: 'src/main/java',
                                    exclusionPattern: 'src/test*'
                                )
                            }
                        }
                    }
                }
                stage ('SonarQube & Quality Gate'){
                    when {
                        expression { SHOULD_ANALYZE == 'true' }
                    }
                    stages{
                        stage('Analysis - SonarQube'){
                            steps {
                                withSonarQubeEnv('SONAR'){
                                    sh "./gradlew -i sonarqube --stacktrace \
                                    -Dsonar.projectKey=${PROJECT_NAME}-${BRANCH_NAME} \
                                    -Dsonar.projectName=${PROJECT_NAME}-${BRANCH_NAME}"
                                }
                            }
                        }
                        stage('Quality gate'){
                            steps {
                                timeout ( time: 1, unit: 'HOURS'){
                                    script{
                                        def qg = waitForQualityGate()
                                        if (qg.status != 'OK'){
                                            error "Pipeline aborted due to a quality gate failure: ${qg.status}"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        stage ('CD') {

            stages {
                stage ('Build') {
                    agent {
                        label 'Build'
                    }
                    stages{
                        stage ('Build Application'){
                            steps{
                             sh "./gradlew -i assemble"
                            }
                        }
                        stage ('Nexus upload'){
                            when {
                              expression { NEXUS_UPLOAD == 'true'}
                            }
                            steps{
                                sh "./gradlew -i uploadArchives"
                            }
                        }
                    }
                }
            }
        }
    }
}