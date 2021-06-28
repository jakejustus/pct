// Syntax check with this command line
// curl -k -X POST -F "jenkinsfile=<Jenkinsfile" https://ci.rssw.eu/pipeline-model-converter/validate

pipeline {
  agent none
  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 30, unit: 'MINUTES')
    skipDefaultCheckout()
  }

  stages {
    stage('Class documentation build') {
      agent { label 'windows' }
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        script {
          def antHome = tool name: 'Ant 1.9', type: 'ant'
          def dlc11 = tool name: 'OpenEdge-11.7', type: 'openedge'
          def dlc12 = tool name: 'OpenEdge-12.2', type: 'openedge'
          def jdk = tool name: 'Corretto 11', type: 'jdk'

          withEnv(["JAVA_HOME=${jdk}"]) {
            bat "${antHome}\\bin\\ant -DDLC11=${dlc11} -DDLC12=${dlc12} classDoc"
          }
        }
        stash name: 'classdoc', includes: 'dist/classDoc.zip'
      }
    }

    stage('Standard build') {
      agent { label 'linux' }
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        unstash name: 'classdoc'
        script {
          sh 'git rev-parse HEAD > head-rev'
          def commit = readFile('head-rev').trim()
          def jdk = tool name: 'Corretto 11', type: 'jdk'
          def antHome = tool name: 'Ant 1.9', type: 'ant'
          def dlc11 = tool name: 'OpenEdge-11.7', type: 'openedge'
          def dlc12 = tool name: 'OpenEdge-12.2', type: 'openedge'
          withEnv(["TERM=xterm", "JAVA_HOME=${jdk}"]) {
            sh "${antHome}/bin/ant -DDLC11=${dlc11} -DDLC12=${dlc12} -DGIT_COMMIT=${commit} dist"
          }
        }
        stash name: 'tests', includes: 'dist/PCT.jar,dist/testcases.zip,tests.xml'
        archiveArtifacts 'dist/PCT.jar,dist/PCT-javadoc.jar,dist/PCT-sources.jar'
      }
    }

    stage('Unit tests') {
      steps {
        parallel branch1: { testBranch('windows', 'JDK8', 'Ant 1.10', 'OpenEdge-11.7', true, '11.7-Win') },
                 branch3: { testBranch('linux', 'JDK8', 'Ant 1.10', 'OpenEdge-11.7', false, '11.7-Linux') },
                 branch5: { testBranch('windows', 'Corretto 11', 'Ant 1.10', 'OpenEdge-12.2', true, '12.2-Win') },
                 branch8: { testBranch('linux', 'Corretto 11', 'Ant 1.10', 'OpenEdge-12.2', false, '12.2-Linux') },
                 failFast: false
      }
    }

    stage('Unit tests reports') {
      agent { label 'linux' }
      steps {
        // Wildcards not accepted in unstash...
        unstash name: 'junit-11.7-Win'
        unstash name: 'junit-11.7-Linux'
        unstash name: 'junit-12.2-Win'
        unstash name: 'junit-12.2-Linux'

        sh "mkdir junitreports"
        unzip zipFile: 'junitreports-11.7-Win.zip', dir: 'junitreports'
        unzip zipFile: 'junitreports-11.7-Linux.zip', dir: 'junitreports'
        unzip zipFile: 'junitreports-12.2-Win.zip', dir: 'junitreports'
        unzip zipFile: 'junitreports-12.2-Linux.zip', dir: 'junitreports'
        junit 'junitreports/**/*.xml'
      }
    }

    stage('Sonar') {
      agent { label 'linux' }
      steps {
        unstash name: 'coverage-11.7-Win'
        unstash name: 'coverage-12.2-Win'
        script {
          def antHome = tool name: 'Ant 1.9', type: 'ant'
          def jdk = tool name: 'Corretto 11', type: 'jdk'
          def dlc = tool name: 'OpenEdge-11.7', type: 'openedge'
          withEnv(["JAVA_HOME=${jdk}"]) {
            sh "${antHome}/bin/ant -lib lib/jacocoant-0.8.4.jar -file sonar.xml -DDLC=${dlc} init-sonar"
          }
          withEnv(["PATH+SCAN=${tool name: 'SQScanner4', type: 'hudson.plugins.sonar.SonarRunnerInstallation'}/bin"]) {
            withSonarQubeEnv('RSSW') {
              if ('master' == env.BRANCH_NAME) {
                sh "sonar-scanner -Dsonar.oe.dlc=${dlc} -Dsonar.branch.name=$BRANCH_NAME"
              } else {
                sh "sonar-scanner -Dsonar.oe.dlc=${dlc} -Dsonar.pullrequest.branch=$BRANCH_NAME -Dsonar.pullrequest.base=master -Dsonar.pullrequest.key=1"
              }
            }
          }
        }
      }
    }
  }
}

def testBranch(nodeName, jdkVersion, antVersion, dlcVersion, stashCoverage, label) {
  node(nodeName) {
    ws {
      deleteDir()
      def dlc = tool name: dlcVersion, type: 'openedge'
      def jdk = tool name: jdkVersion, type: 'jdk'
      def antHome = tool name: antVersion, type: 'ant'
      unstash name: 'tests'
      withEnv(["TERM=xterm", "JAVA_HOME=${jdk}"]) {
        if (isUnix())
          sh "${antHome}/bin/ant -lib dist/PCT.jar -DDLC=${dlc} -DPROFILER=true -DTESTENV=${label} -f tests.xml init dist"
        else
          bat "${antHome}/bin/ant -lib dist/PCT.jar -DDLC=${dlc} -DPROFILER=true -DTESTENV=${label} -f tests.xml init dist"
      }
      stash name: "junit-${label}", includes: 'junitreports-*.zip'
      archiveArtifacts 'emailable-report-*.html'
      if (stashCoverage) {
        stash name: "coverage-${label}", includes: "profiler/jacoco-${label}.exec,oe-profiler-data-${label}.zip"
      }
    }
  }
}
