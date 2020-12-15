pipeline {
    agent any
    tools {
        maven 'Maven'
        jdk 'JDK11'
    }
    
    triggers {
        cron 'H/3 * * * *'
    }
    
    parameters {
        booleanParam defaultValue: false, description: 'Select to skip tests', name: 'SKIP_TESTS'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cleaning and checking out...'
                cleanWs()
                git 'https://github.com/Lukasmp3/flight-log.git'
            }
        }
        stage('Build') {
            steps {
                echo 'Building...'
                dir('.') {
                    sh "mvn --batch-mode clean install -DskipTests"
                }
            }
        }
        stage('Test') {
            when {
                not {
                    expression { params.SKIP_TESTS }
                }
            }
            steps {
                echo 'Testing...'
                dir('.') {
                    script {
                        try {
                            sh "mvn --batch-mode test -Dmaven.test.failure.ignore=true"
                        } finally {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
    }
}
