def server = Artifactory.server "artifactory"
def rtFullUrl = server.url

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
                    sh("docker run --rm -e 'ARTIFACTORY_URL=$rtFullUrl' \
                                        -e 'ARTIFACTORY_USER=$USER' \
                                        -e 'ARTIFACTORY_PASSWORD=$PASSWORD' \
                                        -e 'ARTIFACTORY_REPO=$REPO_NAME' \
                                        -e 'PACKAGE_SIZE_MIN=1' \
                                        -e 'PACKAGE_SIZE_MAX=$NUM_OF_ARTIFACTS' eladhr/$PACKAGE_TYPE-generator:1.0")
                }
            }
        }
    }
}
//
void firstTimeInit() {
    if  (params.NUM_OF_ARTIFACTS == null) {
        properties([
                parameters([
                        string(name: 'PACKAGE_TYPE', defaultValue: '' ,description: 'please select - maven/npm/generic',),
                        string(name: 'REPO_NAME', defaultValue: '' ,description: 'Please select target repo name',),
                        string(name: 'NUM_OF_ARTIFACTS', defaultValue: '' ,description: 'Please select num of artifacts to generate',),
                ])
        ])
        currentBuild.result = 'SUCCESS'
        error('Aborting for first time job setup')
    }
}
