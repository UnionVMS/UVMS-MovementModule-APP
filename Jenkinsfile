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
    stage('Results') {
      steps {
        archive 'target/*.ear'
      }
    }
    stage('SonarQube analysis') {
      steps{ 
        withSonarQubeEnv('Sonarqube.com') {
          sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.dynamicAnalysis=reuseReports -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_AUTH_TOKEN $SONAR_EXTRA_PROPS'
        }
      }
    }
  }
}

