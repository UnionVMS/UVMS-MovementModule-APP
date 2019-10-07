node {
   stage('Clone') {
      checkout scm
   }
   
   stage('Build') {
      // Run the maven build
      withMaven(
        // Maven installation declared in the Jenkins "Global Tool Configuration"
        maven: 'M3',
        // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
        // We recommend to define Maven settings.xml globally at the folder level using
        // navigating to the folder configuration in the section "Pipeline Maven Configuration / Override global Maven configuration"
        // or globally to the entire master navigating to  "Manage Jenkins / Global Tools Configuration"
        mavenSettingsConfig: 'focus-maven-settings',
        mavenLocalRepo: '.repo') {
            sh 'mvn clean package'
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

