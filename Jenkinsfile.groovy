firstTimeInit()

podTemplate(label: 'jenkins-pipeline' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true , privileged: true)],
        volumes: [hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')]) {

    node('jenkins-pipeline') {

        stage('Cleanup') {
            cleanWs()
        }

        stage('Docker build') {
            withCredentials([usernamePassword(credentialsId: 'artifactorypass', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                container('docker') {
                    sh("docker run --rm -e 'ARTIFACTORY_URL=$ARTIFACTORY_URL' \
                                        -e 'ARTIFACTORY_USER=$USER' \
                                        -e 'ARTIFACTORY_PASSWORD=$PASSWORD' \
                                        -e 'ARTIFACTORY_REPO=$REPO_NAME' \
                                        -e 'PACKAGE_CLONE_MAX_LEVEL=$PACKAGE_CLONE_MAX_LEVEL' \
                                        -e 'PACKAGES_DUPLICATION_RATE=$PACKAGES_DUPLICATION_RATE' \
                                        -e 'PACKAGE_NUMBER=$NUM_OF_ARTIFACTS' \
                                        -e 'PACKAGE_SIZE_MIN=$PACKAGE_SIZE_MIN' \
                                        -e 'PACKAGE_SIZE_MAX=$PACKAGE_SIZE_MAX'  eladhr/$PACKAGE_TYPE-generator:1.0")
                }
            }
        }
    }
}

void firstTimeInit() {
    if  (params.ARTIFACTORY_URL == null) {
        properties([
                parameters([
                        string(name: 'ARTIFACTORY_URL', defaultValue: '' ,description: 'please select artifactory url',),
                        string(name: 'PACKAGE_TYPE', defaultValue: '' ,description: 'please select - maven/npm/generic',),
                        string(name: 'REPO_NAME', defaultValue: '' ,description: 'Please select target repo name',),
                        string(name: 'PACKAGE_SIZE_MIN', defaultValue: '' ,description: 'Please select min size',),
                        string(name: 'PACKAGE_SIZE_MAX', defaultValue: '' ,description: 'Please select max size',),
                        string(name: 'PACKAGE_CLONE_MAX_LEVEL', defaultValue: '' ,description: 'Please select max clone level',),
                        string(name: 'PACKAGES_DUPLICATION_RATE', defaultValue: '' ,description: 'Please select duplication rate',),
                        string(name: 'NUM_OF_ARTIFACTS', defaultValue: '' ,description: 'Please select num of artifacts to generate',),
                ])
        ])
        currentBuild.result = 'SUCCESS'
        error('Aborting for first time job setup')
    }
}
