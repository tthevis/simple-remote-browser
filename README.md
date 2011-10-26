About
=====

_SimpleRemoteBrowser_ allows to publish a local directory tree via HTTP and makes it accessible from remote machines. 
It supports a JailRoot like concept which prohibits to access files not contained in the directory tree. 
It is based on [Gretty](https://github.com/groovypp/gretty) and only needs Groovy-1.8 for execution
(tested with Groovy-1.8.2).

Execution
=========

If executed without options the web interface is bound to port 8080 and publishes the current working directory.
Port and directory are configurable via command line options:

    $ groovy SimpleRemoteBrowser -h
    usage: groovy SimpleRemoteBrowser [options]
    Options:
     -b,--baseDir <directory>   Base directory of the browser. Default is the
                                working directory.
     -h,--help                  Show usage information
     -p,--port <port number>    Specifies the port to run on. Default is 8080


