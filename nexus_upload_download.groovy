@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

def artifact_name = System.getProperty('ARTIFACT_NAME')
def repositoryId = System.getProperty('REPOSITORY_ID')
def nexusServer = System.getProperty('NEXUS_SERVER')
def action = System.getProperty('ACTION')
def match = artifact_name =~ /^([^-]+)-(\d+)(\.\S+)$/
def groupId =  "com.epam.${match*.getAt(1)[0]}"
def artifactId = "${match*.getAt(1)[0]}-${match*.getAt(2)[0]}"
def version =  match*.getAt(2)[0]
def extention = match*.getAt(3)[0]
def FilePath = System.getProperty('FILE_PATH')

def get_download_url = { GroupID, RepID, Server, ArtifactID, vers ->
    def http = new HTTPBuilder ( Server )

    http.headers[ 'Authorization' ] = "Basic " + "jenkins:jenkins".getBytes('iso-8859-1').encodeBase64()
    http.request( GET, JSON ) {
        uri.path = "/service/siesta/rest/beta/search"
        uri.query = [ repository: RepID, group: GroupID, name: ArtifactID, version: vers ]
        response.success = { resp, json ->
            return json.items.assets.downloadUrl*.getAt(0)
        }
        response.failure = { resp, json ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}
def browse = { GroupID, RepID, Server ->
    def http = new HTTPBuilder ( Server )

    http.headers[ 'Authorization' ] = "Basic " + "jenkins:jenkins".getBytes('iso-8859-1').encodeBase64()
    http.request( GET, JSON ) {
        uri.path = "/service/siesta/rest/beta/search"
        uri.query = [ repository: RepID, group: GroupID ]
        response.success = { resp, json ->
            def list =[]
            for (i = 0; i < json.items.name.sort().size(); i++) {
                list << "${json.items.name.sort()[i]}.tar.gz"
            }
            return list
        }
        response.failure = { resp, json ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}
def download = { url, filepath, filename ->
    def http = new HTTPBuilder ( url[0] )

    http.headers[ 'Authorization' ] = "Basic " + "jenkins:jenkins".getBytes('iso-8859-1').encodeBase64()
    http.request( GET, BINARY ) {
        response.success = { resp, binary ->
            new File ("${filepath}/${filename}") << binary.bytes
            println "Successfully downloaded"
        }
        response.failure = { resp, json ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}
def upload = { GroupID, RepID, Server, ArtifactID, vers, filepath  ->
    def finder = GroupID =~ /\w+/
    def grouppath = []
    for (i = 0; i < finder.size(); i++) {
        grouppath += "${finder[i]}"
    }
    def http = new HTTPBuilder ( "${Server}repository/${RepID}/${grouppath.join('/')}/${ArtifactID}/${vers}/${ArtifactID}-${vers}${extention}" )

    http.headers[ 'Authorization' ] = "Basic " + "jenkins:jenkins".getBytes('iso-8859-1').encodeBase64()
    http.request( PUT, BINARY ) {
        body = new File ("${filepath}/${ArtifactID}${extention}").bytes
        response.success = { resp, data ->
            println "Successfully uploaded"
        }
        response.failure = { resp, json ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}
switch (action) {
    case 'upload':
        upload (groupId,repositoryId,nexusServer,artifactId,version,FilePath)
        break
    case 'download':
        download (get_download_url(groupId,repositoryId,nexusServer,artifactId,version),FilePath,artifact_name)
        break
}

