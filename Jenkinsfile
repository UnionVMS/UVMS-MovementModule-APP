pipeline {
  agent any
  tools {
    maven 'Maven3'
    jdk 'JDK8'
  }
  stages {
    stage ('Build') {
      steps {
        sh 'mvn clean install -Pgenerate-rest-doc,docker,jacoco,postgres,publish-sql -U' 
      }
    }
    stage('Results') {
      steps {
        archive 'target/*.ear'
      }
    }
    stage('SonarQube analysis') {
      steps{ 
        withSonarQubeEnv('sonarqube') {
          sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar'
        }
      }
    }
  }
}

