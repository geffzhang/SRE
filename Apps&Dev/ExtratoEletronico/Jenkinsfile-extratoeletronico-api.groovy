/*@NonCPS
def slackNotifier(String buildResult) {
  if ( buildResult == "SUCCESS" ) {
    slackSend ( color: "good",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#financeiro_contabil-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Sucesso \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "FAILURE" ) {
    slackSend( color: "danger",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#financeiro_contabil-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Falha \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "UNSTABLE" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#financeiro_contabil-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Unstable \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "ABORTED" ) {
    slackSend( teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#financeiro_contabil-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Abortado \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "null" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#financeiro_contabil-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Desconhecido \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
}

def startMsg() {
  commit_desc = sh(script: "git log -1 --format='format:%s'", returnStdout: true).trim()
  slackSend(teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#financeiro_contabil-build',
    message: "Job: ${env.JOB_NAME} \nBuild: <${env.BUILD_URL}|build ${env.BUILD_DISPLAY_NAME}> \nIniciado por: ${env.BUILD_USER}\n Commit: ${commit_desc}\nBranch: ${env.BRANCH_NAME}")
}
*/
@NonCPS
def configFromBranch(branch) {
    def env_dev = [
        rancherContext: 'c-lh4tc:p-58j5f',
        deploymentFile: 'deployment.yml',
        namespace: 'extrato-eletronico-dev'
    ]
    def env_staging = [
        rancherContext: 'c-lh4tc:p-58j5f',
        deploymentFile: 'deployment.yml',
        namespace: 'extrato-eletronico-stg'
    ]
    def env_prd = [
        rancherContext: '',
        deploymentFile: '',
        namespace: ''
    ]
    if (branch ==~ /(develop)/) { 
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-SNAPSHOT',
            env: 'develop',
            tag: 'aws-dev',
            deployments: [env_dev]
        ]
    }
    
    
    else if (branch ==~ /(staging)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-RC',
            env: 'staging',
            tag: 'aws-stg',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(master)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: false,
            nexusUpload: false,
            jarSuffix: '',
            env: 'production',
            tag: 'aws-prd',
            deployments: [env_prd]
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
        SHOULD_BUILD_IMAGE = "${CONFIG.shouldBuildImage}"
        SHOULD_PUSH_IMAGE = "${CONFIG.shouldPushImage}"
        SHOULD_DEPLOY = "${CONFIG.shouldDeploy}"
        NEXUS_UPLOAD = "${CONFIG.nexusUpload}"
        JAR_SUFFIX = "${CONFIG.jarSuffix}"
        ENV = "${CONFIG.env}"
        NAME ="${CONFIG.namespace}"

        GIT_URL="git@bitbucket.org:machina/configuracao-extratoeletronico-api.git"
        PROJECT_NAME="conf-extratoeletronico-api"

        IMAGE_TAG = "${CONFIG.tag}-${env.BUILD_ID}"
        IMAGE_NAME = "${PROJECT_NAME}:${IMAGE_TAG}"
        IMAGE_URL = "awsuserid.dkr.ecr.sa-east-1.amazonaws.com/${IMAGE_NAME}"
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
                        credentialsId: "bitb-machina",
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
                stage ('Deploy') {
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
                        stage ('Docker - Build') {
                            when {
                              expression { SHOULD_BUILD_IMAGE == 'true' }
                            }                  
                            steps {
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "export AWS_DEFAULT_REGION=sa-east-1"
                                    sh "\$(/usr/local/bin/aws ecr get-login --no-include-email --region sa-east-1)"
                                    sh "docker build --pull -t ${IMAGE_URL} --no-cache ."
                                }
                            }
                        }
                        stage('Docker - Push'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "docker push ${IMAGE_URL}"
                                    echo "Image push complete"
                                }
                            }
                        }
                        stage('Sysdig Analysis - Scan'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                sh "echo ${IMAGE_URL} > sysdig_secure_images"
                                sysdig engineCredentialsId: 'sysdig-token', name: 'sysdig_secure_images', inlineScanning: true, bailOnFail: false, bailOnPluginFail: false
                            }
                        }
                        stage('Kubernetes Deploy'){
                            when {
                              expression { SHOULD_DEPLOY == 'true' }
                            }                          
                            steps {
                                script {
                                    for (deployment in configFromBranch(BRANCH_NAME).deployments) {
                                        sh "echo yes | /usr/bin/rancher login --token token-lxxmp:cznmfmm5k9fh9whdzkbmqhqbbswnpmcwznzvnwc2ljmsp5hfxq5qw9  --context ${deployment.rancherContext} --name rancher2 https://ec2-18-228-89-100.sa-east-1.compute.amazonaws.com/v3"
                                        sh """
                                            sed '
                                              s,{{IMAGE_URL}},${IMAGE_URL},g;
                                              s,{{NAMESPACE}},${deployment.namespace},g;
                                              s,{{PROJECT_NAME}},${PROJECT_NAME},g;
                                            ' ${deployment.deploymentFile} | /usr/bin/rancher kubectl apply -f -
                                            """
                                     }
                                }
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


$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$


#2 - flx-api-autorizador/
Jenkinsfile

/*@NonCPS
def slackNotifier(String buildResult) {
  if ( buildResult == "SUCCESS" ) {
    slackSend ( color: "good",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#team_machinaflex-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Sucesso \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "FAILURE" ) {
    slackSend( color: "danger",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#team_machinaflex-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Falha \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "UNSTABLE" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#team_machinaflex-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Unstable \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "ABORTED" ) {
    slackSend( teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#team_machinaflex-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Abortado \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "null" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#team_machinaflex-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Desconhecido \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
}

def startMsg() {
  commit_desc = sh(script: "git log -1 --format='format:%s'", returnStdout: true).trim()
  slackSend(teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#team_machinaflex-build',
    message: "Job: ${env.JOB_NAME} \nBuild: <${env.BUILD_URL}|build ${env.BUILD_DISPLAY_NAME}> \nIniciado por: ${env.BUILD_USER}\n Commit: ${commit_desc}\nBranch: ${env.BRANCH_NAME}")
}
*/
@NonCPS
def configFromBranch(branch) {
    def env_qa = [
        rancherContext: 'c-ttwvl:p-57qs6',
        deploymentFile: 'deployment.yml',
        namespace: 'machinaflex-qa'
    ]
    def env_staging = [
        rancherContext: 'c-ttwvl:p-57qs6',
        deploymentFile: 'deployment.yml',
        namespace: 'machinaflex-stg'
    ]
    def env_prd = [
        rancherContext: '',
        deploymentFile: '',
        namespace: ''
    ]
    if (branch ==~ /(develop)/) { 
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-SNAPSHOT',
            env: 'develop',
            tag: 'aws-dev',
            deployments: [env_dev]
        ]
    }
    
    
    else if (branch ==~ /(staging)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-RC',
            env: 'staging',
            tag: 'aws-stg',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(master)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: false,
            nexusUpload: false,
            jarSuffix: '',
            env: 'production',
            tag: 'aws-prd',
            deployments: [env_prd]
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
        SHOULD_BUILD_IMAGE = "${CONFIG.shouldBuildImage}"
        SHOULD_PUSH_IMAGE = "${CONFIG.shouldPushImage}"
        SHOULD_DEPLOY = "${CONFIG.shouldDeploy}"
        NEXUS_UPLOAD = "${CONFIG.nexusUpload}"
        JAR_SUFFIX = "${CONFIG.jarSuffix}"
        ENV = "${CONFIG.env}"
        NAME ="${CONFIG.namespace}"

        GIT_URL="git@bitbucket.org:machina/flx-api-autorizador.git"
        PROJECT_NAME="flx-api-autorizador"

        IMAGE_TAG = "${CONFIG.tag}-${env.BUILD_ID}"
        IMAGE_NAME = "${PROJECT_NAME}:${IMAGE_TAG}"
        IMAGE_URL = "awsuserid.dkr.ecr.sa-east-1.amazonaws.com/${IMAGE_NAME}"
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
                        credentialsId: "bitb-machina",
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
                stage ('Deploy') {
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
                        stage ('Docker - Build') {
                            when {
                              expression { SHOULD_BUILD_IMAGE == 'true' }
                            }                  
                            steps {
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "export AWS_DEFAULT_REGION=sa-east-1"
                                    sh "\$(/usr/local/bin/aws ecr get-login --no-include-email --region sa-east-1)"
                                    sh "docker build --pull -t ${IMAGE_URL} --no-cache ."
                                }
                            }
                        }
                        stage('Docker - Push'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "docker push ${IMAGE_URL}"
                                    echo "Image push complete"
                                }
                            }
                        }
                        stage('Kubernetes Deploy'){
                            when {
                              expression { SHOULD_DEPLOY == 'true' }
                            }                          
                            steps {
                                script {
                                    for (deployment in configFromBranch(BRANCH_NAME).deployments) {
                                        sh "echo yes | /usr/bin/rancher login --token token-lxxmp:cznmfmm5k9fh9whdzkbmqhqbbswnpmcwznzvnwc2ljmsp5hfxq5qw9  --context ${deployment.rancherContext} --name rancher2 https://ec2-18-228-89-100.sa-east-1.compute.amazonaws.com/v3"
                                        sh """
                                            sed '
                                              s,{{IMAGE_URL}},${IMAGE_URL},g;
                                              s,{{NAMESPACE}},${deployment.namespace},g;
                                              s,{{PROJECT_NAME}},${PROJECT_NAME},g;
                                            ' ${deployment.deploymentFile} | /usr/bin/rancher kubectl apply -f -
                                            """
                                     }
                                }
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




$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$


#3 - extrato-extracao-worker/Jenkinsfile


/*@NonCPS
def slackNotifier(String buildResult) {
  if ( buildResult == "SUCCESS" ) {
    slackSend ( color: "good",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#financeiro_contabil-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Sucesso \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "FAILURE" ) {
    slackSend( color: "danger",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#financeiro_contabil-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Falha \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "UNSTABLE" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#financeiro_contabil-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Unstable \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "ABORTED" ) {
    slackSend( teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#financeiro_contabil-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Abortado \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "null" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#financeiro_contabil-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Desconhecido \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
}

def startMsg() {
  commit_desc = sh(script: "git log -1 --format='format:%s'", returnStdout: true).trim()
  slackSend(teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#financeiro_contabil-build',
    message: "Job: ${env.JOB_NAME} \nBuild: <${env.BUILD_URL}|build ${env.BUILD_DISPLAY_NAME}> \nIniciado por: ${env.BUILD_USER}\n Commit: ${commit_desc}\nBranch: ${env.BRANCH_NAME}")
}
*/
@NonCPS
def configFromBranch(branch) {
    def env_dev = [
        rancherContext: 'c-lh4tc:p-58j5f',
        deploymentFile: 'deployment.yml',
        namespace: 'extrato-eletronico-dev'
    ]
    def env_staging = [
        rancherContext: 'c-lh4tc:p-58j5f',
        deploymentFile: 'deployment.yml',
        namespace: 'extrato-eletronico-stg'
    ]
    def env_prd = [
        rancherContext: '',
        deploymentFile: '',
        namespace: ''
    ]
    if (branch ==~ /(develop)/) { 
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: true,
            jarSuffix: '-SNAPSHOT',
            env: 'develop',
            tag: 'aws-dev',
            deployments: [env_dev]
        ]
    }
    
    
    else if (branch ==~ /(staging)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-RC',
            env: 'staging',
            tag: 'aws-stg',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(master)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: false,
            nexusUpload: true,
            jarSuffix: '',
            env: 'production',
            tag: 'aws-prd',
            deployments: [env_prd]
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
        SHOULD_BUILD_IMAGE = "${CONFIG.shouldBuildImage}"
        SHOULD_PUSH_IMAGE = "${CONFIG.shouldPushImage}"
        SHOULD_DEPLOY = "${CONFIG.shouldDeploy}"
        NEXUS_UPLOAD = "${CONFIG.nexusUpload}"
        JAR_SUFFIX = "${CONFIG.jarSuffix}"
        ENV = "${CONFIG.env}"
        NAME ="${CONFIG.namespace}"

        GIT_URL="git@bitbucket.org:machina/extrato-extracao-worker.git"
        PROJECT_NAME="worker-extrato-extracao"

        IMAGE_TAG = "${CONFIG.tag}-${env.BUILD_ID}"
        IMAGE_NAME = "${PROJECT_NAME}:${IMAGE_TAG}"
        IMAGE_URL = "awsuserid.dkr.ecr.sa-east-1.amazonaws.com/${IMAGE_NAME}"
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
                        credentialsId: "bitb-machina",
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
                stage ('Deploy') {
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
                        stage ('Build - Docker') {
                            when {
                              expression { SHOULD_BUILD_IMAGE == 'true' }
                            }
                            steps {
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "export AWS_DEFAULT_REGION=sa-east-1"
                                    sh "\$(/usr/local/bin/aws ecr get-login --no-include-email --region sa-east-1)"
                                    sh "docker build --pull -t ${IMAGE_URL} --no-cache ."
                                }
                            }
                        }
                        stage('Push Image - Docker'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "docker push ${IMAGE_URL}"
                                    echo "Image push complete"
                                }
                            }
                        }
                        stage('Sysdig Analysis - Scan'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                sh "echo ${IMAGE_URL} > sysdig_secure_images"
                                sysdig engineCredentialsId: 'sysdig-token', name: 'sysdig_secure_images', inlineScanning: true, bailOnFail: false, bailOnPluginFail: false
                            }
                        }
                        stage('Kubernetes Deploy'){
                            when {
                              expression { SHOULD_DEPLOY == 'true' }
                            }                          
                            steps {
                                script {
                                    for (deployment in configFromBranch(BRANCH_NAME).deployments) {
                                        sh "echo yes | /usr/bin/rancher login --token token-lxxmp:cznmfmm5k9fh9whdzkbmqhqbbswnpmcwznzvnwc2ljmsp5hfxq5qw9  --context ${deployment.rancherContext} --name rancher2 https://ec2-18-228-89-100.sa-east-1.compute.amazonaws.com/v3"
                                        sh """
                                            sed '
                                              s,{{IMAGE_URL}},${IMAGE_URL},g;
                                              s,{{NAMESPACE}},${deployment.namespace},g;
                                              s,{{PROJECT_NAME}},${PROJECT_NAME},g;
                                            ' ${deployment.deploymentFile} | /usr/bin/rancher kubectl apply -f -
                                            """
                                     }
                                }
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








$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$


#4 - flx-api-autorizador

/*@NonCPS
def slackNotifier(String buildResult) {
  if ( buildResult == "SUCCESS" ) {
    slackSend ( color: "good",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#team_machinaflex-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Sucesso \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "FAILURE" ) {
    slackSend( color: "danger",
    teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#team_machinaflex-build',
    message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Falha \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "UNSTABLE" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#team_machinaflex-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Unstable \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "ABORTED" ) {
    slackSend( teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#team_machinaflex-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Abortado \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
  else if( buildResult == "null" ) {
    slackSend( color: "warning",
      teamDomain: 'machinaSlackChanell',
      tokenCredentialId: 'machina-slack',
      channel: '#team_machinaflex-build',
      message: "Job: ${env.JOB_NAME} \nBuild: ${env.BUILD_NUMBER} \nStatus: Desconhecido \nTempo: ${currentBuild.durationString.minus(' and counting')}")
  }
}

def startMsg() {
  commit_desc = sh(script: "git log -1 --format='format:%s'", returnStdout: true).trim()
  slackSend(teamDomain: 'machinaSlackChanell',
    tokenCredentialId: 'machina-slack',
    channel: '#team_machinaflex-build',
    message: "Job: ${env.JOB_NAME} \nBuild: <${env.BUILD_URL}|build ${env.BUILD_DISPLAY_NAME}> \nIniciado por: ${env.BUILD_USER}\n Commit: ${commit_desc}\nBranch: ${env.BRANCH_NAME}")
}
*/
@NonCPS
def configFromBranch(branch) {
    def env_qa = [
        rancherContext: 'c-ttwvl:p-57qs6',
        deploymentFile: 'deployment.yml',
        namespace: 'machinaflex-qa'
    ]
    def env_staging = [
        rancherContext: 'c-ttwvl:p-57qs6',
        deploymentFile: 'deployment.yml',
        namespace: 'machinaflex-stg'
    ]
    def env_prd = [
        rancherContext: '',
        deploymentFile: '',
        namespace: ''
    ]
    if (branch ==~ /(develop)/) { 
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-SNAPSHOT',
            env: 'develop',
            tag: 'aws-dev',
            deployments: [env_dev]
        ]
    }
    
    
    else if (branch ==~ /(staging)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: true,
            nexusUpload: false,
            jarSuffix: '-RC',
            env: 'staging',
            tag: 'aws-stg',
            deployments: [env_staging]
        ]
    }
    else if (branch ==~ /(master)/) {
        return [
            shouldTest: false,
            shouldAnalyze: false,
            shouldBuildImage: true,
            shouldPushImage: true,
            shouldDeploy: false,
            nexusUpload: false,
            jarSuffix: '',
            env: 'production',
            tag: 'aws-prd',
            deployments: [env_prd]
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
        SHOULD_BUILD_IMAGE = "${CONFIG.shouldBuildImage}"
        SHOULD_PUSH_IMAGE = "${CONFIG.shouldPushImage}"
        SHOULD_DEPLOY = "${CONFIG.shouldDeploy}"
        NEXUS_UPLOAD = "${CONFIG.nexusUpload}"
        JAR_SUFFIX = "${CONFIG.jarSuffix}"
        ENV = "${CONFIG.env}"
        NAME ="${CONFIG.namespace}"

        GIT_URL="git@bitbucket.org:machina/flx-api-autorizador.git"
        PROJECT_NAME="flx-api-autorizador"

        IMAGE_TAG = "${CONFIG.tag}-${env.BUILD_ID}"
        IMAGE_NAME = "${PROJECT_NAME}:${IMAGE_TAG}"
        IMAGE_URL = "awsuserid.dkr.ecr.sa-east-1.amazonaws.com/${IMAGE_NAME}"
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
                        credentialsId: "bitb-machina",
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
                stage ('Deploy') {
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
                        stage ('Docker - Build') {
                            when {
                              expression { SHOULD_BUILD_IMAGE == 'true' }
                            }                  
                            steps {
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "export AWS_DEFAULT_REGION=sa-east-1"
                                    sh "\$(/usr/local/bin/aws ecr get-login --no-include-email --region sa-east-1)"
                                    sh "docker build --pull -t ${IMAGE_URL} --no-cache ."
                                }
                            }
                        }
                        stage('Docker - Push'){
                            when {
                              expression { SHOULD_PUSH_IMAGE == 'true' }
                            }
                            steps{
                                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'machina-ecr', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                                    sh "docker push ${IMAGE_URL}"
                                    echo "Image push complete"
                                }
                            }
                        }
                        stage('Kubernetes Deploy'){
                            when {
                              expression { SHOULD_DEPLOY == 'true' }
                            }                          
                            steps {
                                script {
                                    for (deployment in configFromBranch(BRANCH_NAME).deployments) {
                                        sh "echo yes | /usr/bin/rancher login --token token-lxxmp:cznmfmm5k9fh9whdzkbmqhqbbswnpmcwznzvnwc2ljmsp5hfxq5qw9  --context ${deployment.rancherContext} --name rancher2 https://ec2-18-228-89-100.sa-east-1.compute.amazonaws.com/v3"
                                        sh """
                                            sed '
                                              s,{{IMAGE_URL}},${IMAGE_URL},g;
                                              s,{{NAMESPACE}},${deployment.namespace},g;
                                              s,{{PROJECT_NAME}},${PROJECT_NAME},g;
                                            ' ${deployment.deploymentFile} | /usr/bin/rancher kubectl apply -f -
                                            """
                                     }
                                }
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
