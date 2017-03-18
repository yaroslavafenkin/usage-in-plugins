pipeline {
   agent {
      label 'java'
   }

   triggers {
      cron('H H * * *')
   }

   tools {
      tool 'jdk8'
      tool 'mvn'
   }

   options {
      timeout(time: 1, unit: 'HOURS')
   }

   stages {
      stage ('Checkout') {
         steps {
            git 'https://github.com/jenkins-infra/deprecated-usage-in-plugins.git'
         }
      }

      stage ('Build') {
         steps {
            sh 'mvn clean package exec:java'
         }
      }

      stage ('Archive') {
         steps {
            archive 'target/*.html'
         }
      }
   }
}
