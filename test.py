from subprocess import *

def jarWrapper(*args):
    process = Popen(['java', '-jar']+list(args), stdout=PIPE, stderr=PIPE)
    ret = []
    while process.poll() is None:
        line = process.stdout.readline()
        if ('SLF4J' not in line) and line.endswith('\n'):
            ret.append(line[:-1])
    stdout, stderr = process.communicate()
    ret += stdout.split('\n')
    if stderr != '':
        ret += stderr.split('\n')
    ret.remove('')
    return ret

args = ['target/graphcontroller-0.0.1-SNAPSHOT.jar', '-q \"g.V().has(\'name\',\'Barack Obama\')\"'] # Any number of args to be passed to the jar file

result = jarWrapper(*args)

print result