import redisgears

def xlog(*args):
    redisgears.executeCommand('xadd', 'log', '*', 'text', ' '.join(map(str, args)))

def toIA(x):
	# a simple example
	redisgears.executeCommand('set', 'ia', x['value']['student'])

def toTimeSeries(x):
	# a simple example
	redisgears.executeCommand('set', 'timeseries', x['value']['student'])

gearsCtx('StreamReader').\
    foreach(toTimeSeries).\
    foreach(toIA).\
    register('course-rated:0', trimStream=False)