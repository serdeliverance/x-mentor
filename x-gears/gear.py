def xlog(*args):
    redisgears.executeCommand('xadd', 'log', '*', 'text', ' '.join(map(str, args)))

def toTimeSeries(x):
	student = x['value']['student']
	durationInSeconds = x['value']['duration']
	timestamp = x['value']['timestamp']

	addStudentCommand = 'TS.ADD studentprogress:{} {} {} RETENTION 0 LABELS student {}'.format(student, timestamp, durationInSeconds, student)
	
	xlog(addStudentCommand)
	
	args = list(addStudentCommand.split(" "))
	
	redisgears.executeCommand(*args)


gb = GearsBuilder('StreamReader')
gb.foreach(toTimeSeries)
gb.register('student-progress-registered', batch=1, duration=0, onFailedPolicy='continue', onFailedRetryInterval=1, trimStream=False)