import redisgears

def xlog(*args):
    redisgears.executeCommand('xadd', 'log', '*', 'text', ' '.join(map(str, args)))

def toIA(x):
	xlog('toIA: ', x)

def toTimeSeries(x):
	redisgears.executeCommand('set', 'foo', 'bar')

gearsCtx('StreamReader').\
    foreach(toTimeSeries).\
    register('course-rated:0', trimStream=False)