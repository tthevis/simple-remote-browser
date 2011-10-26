import groovy.xml.MarkupBuilder
import org.mbte.gretty.httpserver.* 
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND

def DEFAULT_PORT = 8080

def cli = new CliBuilder(usage: 'groovy SimpleRemoteBrowser [options]', header: 'Options:')
cli.h longOpt: 'help', 'Show usage information' 
cli.p longOpt: 'port', args: 1, argName: 'port number', "Specifies the port to run on. Default is ${DEFAULT_PORT}" 
cli.b longOpt: 'baseDir', args: 1, argName: 'directory', 'Base directory of the browser. Default is the working directory.'

def options = cli.parse(args)
if (options.h) {
	cli.usage()
	return
}

File baseDir = new File(options.b ?: '.')

@GrabResolver(name='gretty', 
  root='http://groovypp.artifactoryonline.com/groovypp/libs-releases-local')
@Grab('org.mbte.groovypp:gretty:0.4.279') 
GrettyServer server = [] 
server.groovy = [ 
    localAddress: new InetSocketAddress((options?.p ?: DEFAULT_PORT) as Integer), 
    defaultHandler: { 
        response.redirect "/" 
    }, 
	"/:path": {
		get {
			def file = new File(baseDir, request.parameters['path']) 
			if (!file.exists() || !file.canonicalPath.startsWith(baseDir.canonicalPath)) {
				response.status = NOT_FOUND
				return
			}
			if (file.isFile()) {
				response.responseBody = file
			} else {
				response.html = showDir(new File(baseDir, request.parameters['path']), baseDir)
			}
		}
	} 
] 
server.start()

def showDir(File dir, File base) { 
	def sortByTypeThenName = { a, b ->
		a.isFile() != b.isFile() ? a.isFile() <=> b.isFile() : a.name <=> b.name
	}
	def result = new StringWriter()
	new MarkupBuilder(result).html {
		head {
			title "SimpleRemoteBrowser \u00BB ${base.canonicalFile.name}${(dir.canonicalPath - base.canonicalPath)}"
		}
		body {
			div(id: 'hierarchy') {
				getHierarchy(dir, base).each {
					[a(href: it.path, it.name), b('/')]
				}
			}
			table(id: 'listing') {
				if (dir.list()) {
					tr { [th('Name'), th('Type'), th('Size [bytes]'), 
						th('Last Modified'), th('r/w/x')]	}
				}
				dir.listFiles().sort(sortByTypeThenName).each { file ->
					tr {
					  td {file.canonicalPath.startsWith(base.canonicalPath) ?
						  	a(href: (file.canonicalPath - base.canonicalPath), file.name) : mkp.yield(file.name)}
					  td file.isFile() ? 'file' : 'dir'
					  td file.length()
					  td new Date(file.lastModified()).toString() 
					  td ([file.canRead() ? 'r' : '-', file.canWrite() ? 'w' : '-', file.canExecute() ? 'x' : '-'].join('/')) 
					}
				}
			}
		}
	}
	result.toString()
}

def getHierarchy(File dir, File base) {
	def result = []
	for (File file = dir; file.canonicalPath != base.canonicalPath; file = file.parentFile) {
		result << [name: file.canonicalFile.name, path: (file.canonicalPath - base.canonicalPath)]
	}
	result << [name: base.canonicalFile.name, path:'/']
	result.reverse()
}
