pipeline {
  agent any
  tools {
    maven 'Maven3'
    jdk 'JDK8'
  }
  stages {
    stage ('Build') {
      steps {
        lock('Docker') {
          sh 'mvn clean install -Pgenerate-rest-doc,docker,jacoco,postgres,publish-sql -U' 
        }
      }
    }
    stage('SonarQube analysis') {
      steps{ 
        withSonarQubeEnv('Sonarqube.com') {
          sh 'mvn $SONAR_MAVEN_GOAL -Dsonar.dynamicAnalysis=reuseReports -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_AUTH_TOKEN $SONAR_EXTRA_PROPS'
        }
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: '**/target/*.ear'
      junit '**/target/surefire-reports/*.xml'
    }
  }
}

