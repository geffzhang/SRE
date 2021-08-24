@NonCPS
def slackNotifier(String buildResult) {

  if ( buildResult == "SUCCESS" ) {
    slackSend ( color: "good", 
    teamDomain: 'machina', 
    tokenCredentialId: 'machina-slack',
    channel: '#team_ec-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Sucesso \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "FAILURE" ) { 
    slackSend( color: "danger", 
    teamDomain: 'machina', 
    tokenCredentialId: 'machina-slack',
    channel: '#team_ec-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Falha \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "UNSTABLE" ) { 
    slackSend( color: "warning", 
      teamDomain: 'machina', 
      tokenCredentialId: 'machina-slack',
      channel: '#team_ec-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Unstable \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "ABORTED" ) { 
    slackSend( teamDomain: 'machina', 
      tokenCredentialId: 'machina-slack',
      channel: '#team_ec-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Abortado \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "null" ) { 
    slackSend( color: "warning", 
      teamDomain: 'machina', 
      tokenCredentialId: 'machina-slack',
      channel: '#team_ec-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Desconhecido \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
}

@NonCPS
def startMsg() {
  commit_desc = sh(script: "git log -1 --format='format:%s'", returnStdout: true).trim()
  slackSend(teamDomain: 'machina', 
    tokenCredentialId: 'machina-slack',
    channel: '#team_ec-build',
    message: "Job: ${env.JOB_NAME} \nBuild: <${env.BUILD_URL}|build ${env.BUILD_DISPLAY_NAME}> \nIniciado por: ${env.BUILD_USER}\n Commit: ${commit_desc}\nBranch: ${env.BRANCH_NAME}")
}

@NonCPS
def configFromBranch(branch) {
    def env_qa = [
        rancherContext: 'c-lh4tc:p-bcs6n',
        deploymentFile: 'deployment.yml',
        namespace: 'ec-staging'
    ]
    def env_staging = [
        rancherContext: 'c-lh4tc:p-bcs6n',
        deploymentFile: 'deployment.yml',
        namespace: 'ec-staging'
    ]
    def env_prd = [
        filePath: 'prod',
        projectName: 'stack-habilitacao-ec-api-prod'
    ]
    def env_dft = [
        rancherContext: 'c-lh4tc:p-bcs6n',
        deploymentFile: 'deployment.yml',
        namespace: 'ec-staging'
    ]
	if (branch ==~ /(qa)/) {
        return [
            shouldUnitTests: true,
            shouldIntegrationTests: true,
            shouldAnalyze: true,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldStoreArtifact: true,
            shouldDeploy: true,
            shouldGenerateStable: true,
            rancherCred: 'rancher2-machina-qa-token',
            rancherURL: 'rancher2-machina-qa-url',
            dockerfile: 'Dockerfile',
            testAgent: 'TestContainer',
            buildAgent: 'Build',
            env: 'staging',
            profile: 'staging',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(hotfix-qa)/) {
        return [
            shouldUnitTests: true,
            shouldIntegrationTests: true,
            shouldAnalyze: true,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldStoreArtifact: true,
            shouldDeploy: true,
            shouldGenerateStable: true,
            rancherCred: 'rancher2-machina-qa-token',
            rancherURL: 'rancher2-machina-qa-url',
            dockerfile: 'Dockerfile',
            testAgent: 'TestContainer',
            buildAgent: 'Build',
            env: 'staging',
            profile: 'hotfix-staging',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(staging)/) {
        return [
            shouldUnitTests: false,
            shouldIntegrationTests: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldStoreArtifact: false,
            shouldDeploy: true,
            shouldGenerateStable: false,
            rancherCred: 'rancher2-machina-qa-token',
            rancherURL: 'rancher2-machina-qa-url',
            dockerfile: 'Dockerfile',
            testAgent: 'TestContainer',
            buildAgent: 'Build',
            env: 'staging',
            profile: 'staging',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(hotfix-staging)/) {
        return [
            shouldUnitTests: true,
            shouldIntegrationTests: true,
            shouldAnalyze: true,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldStoreArtifact: false,
            shouldDeploy: true,
            shouldGenerateStable: false,
            rancherCred: 'rancher2-machina-qa-token',
            rancherURL: 'rancher2-machina-qa-url',
            dockerfile: 'Dockerfile',
            testAgent: 'TestContainer',
            buildAgent: 'Build',
            env: 'staging',
            profile: 'hotfix-staging',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(master)/) {
        return [
            shouldUnitTests: true,
            shouldIntegrationTests: true,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldStoreArtifact: true,
            shouldDeploy: false,
            shouldGenerateStable: true,
            dockerfile: 'Dockerfile',
            testAgent: 'TestContainer',
            buildAgent: 'Build',
            env: 'prod',
            profile: 'production',
            deployments: [env_prd]
        ]
    }
    else {
        return [
            shouldUnitTests: false,
            shouldIntegrationTests: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: false,
            shouldStoreArtifact: false,
            shouldDeploy: false,
            shouldGenerateStable: false,
            rancherCred: 'rancher2-machina-qa-token',
            rancherURL: 'rancher2-machina-qa-url',
            dockerfile: 'Dockerfile',
            testAgent: 'TestContainer',
            buildAgent: 'Build',
            env: 'staging',
            profile: 'staging',
            deployments: [env_staging]
        ]
    }
}
pipeline {
    agent none

    environment {
        CONFIG = configFromBranch(BRANCH_NAME)

        SHOULD_UNIT_TEST = "${CONFIG.shouldUnitTests}"
        SHOULD_INT_TEST = "${CONFIG.shouldIntegrationTests}"
        SHOULD_BUILD_IMAGE = "${CONFIG.shouldBuildImage}"
        SHOULD_PUSH_IMAGE = "${CONFIG.shouldPushImage}"
        SHOUD_STORE_ARTIFACT = "${CONFIG.shouldStoreArtifact}"
        SHOULD_ANALYZE = "${CONFIG.shouldAnalyze}"
        SHOULD_DEPLOY = "${CONFIG.shouldDeploy}"
        SHOULD_GENERATE_STABLE = "${CONFIG.shouldGenerateStable}"

        DOCKERFILE = "${CONFIG.dockerfile}"
        ENV = "${CONFIG.env}"

        RANCHER_CRED = "${CONFIG.rancherCred}"
        RANCHER_URL = "${CONFIG.rancherURL}"

        PROFILE = "${CONFIG.profile}"

        TEST_AGENT = "${CONFIG.testAgent}"
        BUILD_AGENT = "${CONFIG.buildAgent}"

        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "ec2-18-229-139-6.sa-east-1.compute.amazonaws.com:8081"
        NEXUS_REPOSITORY = "nexusrepo"
        NEXUS_CREDENTIAL_ID = "NexusRepo"

        GIT_URL = "git@bitbucket.org:machina/habilitacao-ec-api.git"
        PROJECT_NAME = "habilitacao-ec-api"

        IMAGE_NAME = "${PROJECT_NAME}"
        IMAGE_URL = "957723433972.dkr.ecr.sa-east-1.amazonaws.com/${IMAGE_NAME}"
    }
    stages {
        stage ('CI') {
            agent {
                label "${TEST_AGENT}"
            }
            stages {
                stage('SCM - Checkout') {
                    steps{
                        cleanWs()
                        git branch: BRANCH_NAME,
                        url: GIT_URL
                        echo 'SCM Checkout'
                        echo "Profile de config: ${PROFILE}"
                    }
                }
                stage ('Tests'){
                    stages {
                        stage('Unit & Integration Tests - Gradle'){
                            when {
                                expression { SHOULD_UNIT_TEST == 'true' }
                            }
                            steps {
                                withGradle wrap([$class: 'BuildScanBuildWrapper']){
                                    sh './gradlew -i clean test'
                                }
                            }
                        }
                        stage('Integration-Jacoco Tests - Gradle'){
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
                stage("Publish Artifact - Nexus") {
                    when {
                      expression { SHOUD_STORE_ARTIFACT == 'true' }
                    }
                    steps {
                        withMaven( maven: 'maven-3', jdk: 'jdk11-open'){
                            sh "./mvnw clean install -DskipTests"
                        }
                        script {
                            pom = readMavenPom file: "pom.xml";
                            filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                            echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                            artifactPath = filesByGlob[0].path;
                            artifactExists = fileExists artifactPath;
                            if(artifactExists) {
                                echo "*** Arquivo: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                                nexusArtifactUploader(
                                    nexusVersion: NEXUS_VERSION,
                                    protocol: NEXUS_PROTOCOL,
                                    nexusUrl: NEXUS_URL,
                                    groupId: pom.groupId,
                                    version: pom.version,
                                    repository: NEXUS_REPOSITORY,
                                    credentialsId: NEXUS_CREDENTIAL_ID,
                                   artifacts: [
                                        [artifactId: pom.artifactId, classifier: '', file: artifactPath,type: 'jar']
                                    ]
                                );
                            } else {
                                error "*** Arquivo: ${artifactPath}, não encontrado";
                            }
                        }
                    }
                }
            }
        }
        stage ('CD') {
            stages {
                stage ('Deploy') {
                    agent {
                       label "${BUILD_AGENT}"
                    }
                    stages{
                        stage('Build - Gradle'){
                            steps{
                                withGradle wrap([$class: 'BuildScanBuildWrapper']){
                                    sh "./gradlew -i assemble"
                                }
                            }
                        }
                        stage ('Docker - Build') {
                            when {
                              expression { SHOULD_BUILD_IMAGE == 'true' }
                            }                  
                            steps {
                                script{VERSION = sh(script: 'printf \'VER\t${project.version}\' | mvn help:evaluate | grep \'^VER\' | cut -f2', , returnStdout: true).trim()}
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "export AWS_DEFAULT_REGION=sa-east-1"
                                    sh "\$(/usr/local/bin/aws ecr get-login --no-include-email --region sa-east-1)"
                                    sh "docker build --pull -t ${IMAGE_URL}:${ENV}-${VERSION} --no-cache ."
                                }
                            }
                        }
                        stage('Docker - Push'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "docker push ${IMAGE_URL}:${ENV}-${VERSION}"
                                    echo "Image push complete"
                                }
                            }
                        }
                        stage('Sysdig Analysis - Scan'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                sh "echo ${IMAGE_URL}:${ENV}-${VERSION} > sysdig_secure_images"
                                sysdig engineCredentialsId: 'sysdig-token', name: 'sysdig_secure_images', inlineScanning: true, bailOnFail: false, bailOnPluginFail: false
                            }
                        }
                        stage('Rancher - Deploy'){
                            when {
                              expression { SHOULD_DEPLOY == 'true' }
                            }
                            steps {
                                script { 
                                    for (deployment in configFromBranch(BRANCH_NAME).deployments) {
                                        withCredentials([
                                            string(credentialsId: "${RANCHER_CRED}", variable: 'rancherToken'),
                                            string(credentialsId: "${RANCHER_URL}", variable: 'rancherURL')
                                            ]){
                                                sh "echo yes | /usr/bin/rancher login --token token-lxxmp:ChucrutesSecreto --context ${deployment.rancherContext} --name rancher2 https://ec2-18-228-89-100.sa-east-1.compute.amazonaws.com/v3"
	                                            //sh "echo yes | /usr/bin/rancher login --token ${rancherToken} --context ${deployment.rancherContext} --name rancher2 ${rancherURL}" 
                                            }  
                                                sh """
                                                sed '
                                                  s,{{IMAGE_URL}},${IMAGE_URL}:${ENV}-${VERSION},g;
                                                  s,{{NAMESPACE}},${deployment.namespace},g;
                                                  s,{{PROJECT_NAME}},${PROJECT_NAME},g;
                                                ' ${deployment.deploymentFile} | /usr/bin/rancher kubectl apply -f -
                                                """
                                    }
                                }
                             }
                         }
                         stage('Generate Stable'){
                             when {
                              expression { SHOULD_GENERATE_STABLE == 'true' }
                            }
                            steps{
                                sh "git checkout master &&  git tag v${VERSION} && git push --tags"
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            script{
                slackNotifier('SUCCESS')
            }
        }
        failure{
            script{
                slackNotifier('FAILURE')
            }
        }
        unstable{
            script{
                slackNotifier('UNSTABLE')
            }
        }
        aborted{
            script{
                slackNotifier('ABORTED')
            }
        }
    }
}